package com.example.kairn.data.repository

import android.util.Log
import com.example.kairn.domain.model.Conversation
import com.example.kairn.domain.model.ConversationType
import com.example.kairn.domain.model.Message
import com.example.kairn.domain.model.MessageType
import com.example.kairn.domain.model.User
import com.example.kairn.domain.repository.ChatRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ChatRepository"

/**
 * Chat Repository - Instagram-style architecture
 * 
 * Simple and robust approach:
 * 1. No complex Realtime subscriptions - use explicit refresh
 * 2. Optimistic UI updates after sending messages
 * 3. Clear separation between conversations list and messages
 * 4. Proper handling of current user ID for message alignment
 */
@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val auth: Auth,
    private val postgrest: Postgrest,
    private val realtime: Realtime,
) : ChatRepository {

    // State flows for reactive UI
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())

    // Realtime channels per conversation
    private val realtimeChannels = mutableMapOf<String, RealtimeChannel>()
    private val realtimeScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // Polling jobs as fallback when Realtime doesn't work
    private val pollingJobs = mutableMapOf<String, kotlinx.coroutines.Job>()

    // Cache current user ID to avoid repeated auth calls
    private val currentUserId: String?
        get() = auth.currentUserOrNull()?.id

    // ==================== CONVERSATIONS ====================

    override fun getConversations(): Flow<List<Conversation>> {
        return _conversations
    }

    override suspend fun refreshConversations() {
        Log.d(TAG, "refreshConversations: Starting refresh")
        val userId = currentUserId ?: run {
            Log.e(TAG, "refreshConversations: User not authenticated")
            return
        }

        try {
            // Step 1: Fetch conversations with members (no messages to avoid parsing issues)
            val conversationDtos = postgrest.from("conversations")
                .select(
                    Columns.raw("""
                        id,
                        type,
                        group_id,
                        created_at,
                        updated_at,
                        conversation_members!inner(user_id, last_read_message_id)
                    """.trimIndent())
                ) {
                    filter {
                        eq("conversation_members.user_id", userId)
                    }
                    order("updated_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<ConversationListDto>()

            Log.d(TAG, "refreshConversations: Fetched ${conversationDtos.size} conversations")

            // Step 2: Get all other user IDs for profile fetching
            val otherUserIds = conversationDtos.flatMap { conv ->
                conv.members.map { it.userId }
            }.filter { it != userId }.distinct()

            // Step 3: Fetch profiles for other users
            val profilesMap = if (otherUserIds.isNotEmpty()) {
                postgrest.from("profiles")
                    .select(Columns.raw("id, username, avatar_url")) {
                        filter { isIn("id", otherUserIds) }
                    }
                    .decodeList<ProfileDto>()
                    .associateBy { it.id }
            } else emptyMap()

            // Step 4: Fetch last message for each conversation
            val conversationIds = conversationDtos.map { it.id }
            val lastMessagesMap = if (conversationIds.isNotEmpty()) {
                // Get the last message for each conversation
                val allMessages = postgrest.from("messages")
                    .select(Columns.raw("id, conversation_id, sender_id, body, message_type, created_at")) {
                        filter { isIn("conversation_id", conversationIds) }
                        order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    }
                    .decodeList<SimpleMessageDto>()
                
                // Group by conversation and take the first (most recent) of each
                allMessages.groupBy { it.conversationId }
                    .mapValues { (_, messages) -> messages.first() }
            } else emptyMap()

            // Step 5: Convert to domain models
            _conversations.value = conversationDtos.map { dto ->
                val otherUserId = dto.members.firstOrNull { it.userId != userId }?.userId
                val otherProfile = otherUserId?.let { profilesMap[it] }
                val lastMessage = lastMessagesMap[dto.id]

                dto.toDomain(
                    currentUserId = userId,
                    otherUserProfile = otherProfile,
                    lastMessage = lastMessage
                )
            }

            Log.d(TAG, "refreshConversations: SUCCESS - ${_conversations.value.size} conversations loaded")

        } catch (e: Exception) {
            Log.e(TAG, "refreshConversations: Error", e)
        }
    }

    override suspend fun getOrCreateDirectConversation(userId: String): Result<Conversation> = runCatching {
        Log.d(TAG, "getOrCreateDirectConversation: targetUserId=$userId")
        
        val myUserId = currentUserId 
            ?: throw IllegalStateException("User not authenticated")

        Log.d(TAG, "getOrCreateDirectConversation: currentUserId=$myUserId")

        // Call Supabase RPC function
        val conversationId = postgrest.rpc(
            function = "get_or_create_direct_conversation",
            parameters = buildJsonObject {
                put("user1_id", myUserId)
                put("user2_id", userId)
            }
        ).decodeAs<String>()

        Log.d(TAG, "getOrCreateDirectConversation: conversationId=$conversationId")

        // Fetch conversation details
        val conversation = fetchConversationById(conversationId, myUserId)
        
        // Refresh conversations list
        refreshConversations()
        
        Log.d(TAG, "getOrCreateDirectConversation: SUCCESS - ${conversation.displayName}")
        conversation
    }

    override suspend fun getConversation(conversationId: String): Result<Conversation> = runCatching {
        val myUserId = currentUserId 
            ?: throw IllegalStateException("User not authenticated")
        
        fetchConversationById(conversationId, myUserId)
    }

    private suspend fun fetchConversationById(conversationId: String, myUserId: String): Conversation {
        // Fetch conversation with members
        val dto = postgrest.from("conversations")
            .select(
                Columns.raw("""
                    id,
                    type,
                    group_id,
                    created_at,
                    updated_at,
                    conversation_members(user_id, last_read_message_id)
                """.trimIndent())
            ) {
                filter { eq("id", conversationId) }
            }
            .decodeSingle<ConversationListDto>()

        // Find other user and fetch their profile
        val otherUserId = dto.members.firstOrNull { it.userId != myUserId }?.userId
        
        val otherProfile = if (otherUserId != null) {
            try {
                postgrest.from("profiles")
                    .select(Columns.raw("id, username, avatar_url")) {
                        filter { eq("id", otherUserId) }
                    }
                    .decodeSingle<ProfileDto>()
            } catch (e: Exception) {
                Log.w(TAG, "fetchConversationById: Could not fetch other user profile", e)
                null
            }
        } else null

        return dto.toDomain(myUserId, otherProfile, null)
    }

    // ==================== MESSAGES ====================

    override fun getMessages(conversationId: String): Flow<List<Message>> {
        return _messages.map { messagesMap -> 
            messagesMap[conversationId] ?: emptyList() 
        }
    }

    override suspend fun refreshMessages(conversationId: String) {
        Log.d(TAG, "refreshMessages: conversationId=$conversationId")
        
        val myUserId = currentUserId ?: run {
            Log.e(TAG, "refreshMessages: User not authenticated")
            return
        }

        try {
            val messageDtos = postgrest.from("messages")
                .select(
                    Columns.raw("""
                        id,
                        conversation_id,
                        sender_id,
                        body,
                        message_type,
                        created_at,
                        profiles:sender_id(id, username, avatar_url)
                    """.trimIndent())
                ) {
                    filter { eq("conversation_id", conversationId) }
                    order("created_at", order = io.github.jan.supabase.postgrest.query.Order.ASCENDING)
                }
                .decodeList<MessageDto>()

            Log.d(TAG, "refreshMessages: Fetched ${messageDtos.size} messages")

            val messages = messageDtos.map { it.toDomain(myUserId) }
            _messages.value = _messages.value + (conversationId to messages)

            Log.d(TAG, "refreshMessages: SUCCESS")

        } catch (e: Exception) {
            Log.e(TAG, "refreshMessages: Error", e)
        }
    }

    override suspend fun sendMessage(conversationId: String, body: String): Result<Message> = runCatching {
        Log.d(TAG, "sendMessage: conversationId=$conversationId, body='$body'")
        
        val myUserId = currentUserId 
            ?: throw IllegalStateException("User not authenticated")

        // Get my profile for the message
        val myProfile = try {
            postgrest.from("profiles")
                .select(Columns.raw("id, username, avatar_url")) {
                    filter { eq("id", myUserId) }
                }
                .decodeSingle<ProfileDto>()
        } catch (e: Exception) {
            Log.w(TAG, "sendMessage: Could not fetch own profile", e)
            ProfileDto(id = myUserId, username = "Me", avatarUrl = null)
        }

        // Create optimistic message for immediate UI update
        val optimisticMessage = Message(
            id = "temp_${System.currentTimeMillis()}",
            conversationId = conversationId,
            senderId = myUserId,
            senderName = myProfile.username ?: "Me",
            senderInitials = (myProfile.username ?: "Me").take(2).uppercase(),
            body = body,
            messageType = MessageType.TEXT,
            createdAt = kotlinx.datetime.Clock.System.now(),
            isCurrentUser = true,
        )

        // Optimistic update - show message immediately
        val currentMessages = _messages.value[conversationId] ?: emptyList()
        _messages.value = _messages.value + (conversationId to currentMessages + optimisticMessage)
        Log.d(TAG, "sendMessage: Optimistic update applied")

        // Insert into database
        val insertDto = MessageInsertDto(
            conversationId = conversationId,
            senderId = myUserId,
            body = body,
            messageType = "TEXT"
        )

        val insertedDto = postgrest.from("messages")
            .insert(insertDto) {
                select(Columns.raw("""
                    id,
                    conversation_id,
                    sender_id,
                    body,
                    message_type,
                    created_at
                """.trimIndent()))
            }
            .decodeSingle<SimpleMessageDto>()

        Log.d(TAG, "sendMessage: Inserted with id=${insertedDto.id}")

        // Update conversation's updated_at
        postgrest.from("conversations")
            .update({
                set("updated_at", kotlinx.datetime.Clock.System.now().toString())
            }) {
                filter { eq("id", conversationId) }
            }

        // Replace optimistic message with real one
        val realMessage = Message(
            id = insertedDto.id,
            conversationId = insertedDto.conversationId,
            senderId = insertedDto.senderId,
            senderName = myProfile.username ?: "Me",
            senderInitials = (myProfile.username ?: "Me").take(2).uppercase(),
            body = insertedDto.body,
            messageType = MessageType.valueOf(insertedDto.messageType.uppercase()),
            createdAt = Instant.parse(insertedDto.createdAt),
            isCurrentUser = true,
        )

        // Update with real message (remove optimistic, add real)
        val updatedMessages = (_messages.value[conversationId] ?: emptyList())
            .filterNot { it.id == optimisticMessage.id } + realMessage
        _messages.value = _messages.value + (conversationId to updatedMessages)

        Log.d(TAG, "sendMessage: SUCCESS")
        realMessage
    }

    override suspend fun markAsRead(conversationId: String, messageId: String): Result<Unit> = runCatching {
        val myUserId = currentUserId 
            ?: throw IllegalStateException("User not authenticated")

        postgrest.from("conversation_members")
            .update({
                set("last_read_message_id", messageId)
            }) {
                filter {
                    eq("conversation_id", conversationId)
                    eq("user_id", myUserId)
                }
            }
    }

    // ==================== REALTIME ====================

    override suspend fun subscribeToConversation(conversationId: String) {
        Log.d(TAG, "subscribeToConversation: $conversationId")
        
        // Initial load
        refreshMessages(conversationId)
        
        // Clean up existing channel if any
        unsubscribeFromConversation(conversationId)
        
        // Start Realtime subscription
        try {
            // Create unique channel for this conversation
            val channel = realtime.channel("messages:conversation_id=eq.$conversationId")
            
            // Setup postgres change listener BEFORE subscribing
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "messages"
            }
            
            // Collect changes and refresh messages
            changeFlow.onEach { action ->
                Log.d(TAG, "subscribeToConversation: Realtime event ${action.javaClass.simpleName}")
                refreshMessages(conversationId)
            }.launchIn(realtimeScope)
            
            // Subscribe the channel
            channel.subscribe()
            
            // Store the channel for cleanup
            realtimeChannels[conversationId] = channel
            Log.d(TAG, "subscribeToConversation: Realtime subscribed to $conversationId")
            
        } catch (e: Exception) {
            Log.e(TAG, "subscribeToConversation: Realtime failed for $conversationId", e)
        }
        
        // Also start polling as fallback (every 3 seconds)
        // This ensures messages appear even if Realtime is not configured
        val pollingJob = realtimeScope.launch {
            while (true) {
                delay(3000) // Poll every 3 seconds
                try {
                    refreshMessages(conversationId)
                } catch (e: Exception) {
                    Log.e(TAG, "subscribeToConversation: Polling failed", e)
                }
            }
        }
        pollingJobs[conversationId] = pollingJob
        Log.d(TAG, "subscribeToConversation: Polling started for $conversationId")
    }

    override suspend fun unsubscribeFromConversation(conversationId: String) {
        Log.d(TAG, "unsubscribeFromConversation: $conversationId")
        
        // Stop polling
        pollingJobs[conversationId]?.cancel()
        pollingJobs.remove(conversationId)
        
        // Unsubscribe Realtime channel
        realtimeChannels[conversationId]?.let { channel ->
            try {
                channel.unsubscribe()
                realtimeChannels.remove(conversationId)
                Log.d(TAG, "unsubscribeFromConversation: Realtime unsubscribed from $conversationId")
            } catch (e: Exception) {
                Log.e(TAG, "unsubscribeFromConversation: Realtime failed for $conversationId", e)
            }
        }
    }
    
    // Clean up all channels when repository is destroyed (shouldn't happen with Singleton, but safe)
    fun cleanup() {
        Log.d(TAG, "cleanup: Cleaning up ${realtimeChannels.size} Realtime channels")
        realtimeChannels.keys.toList().forEach { conversationId ->
            realtimeScope.launch {
                unsubscribeFromConversation(conversationId)
            }
        }
        realtimeScope.cancel()
    }
}

// ==================== DTOs ====================

/**
 * DTO for conversation list - no embedded messages to avoid parsing issues
 */
@Serializable
private data class ConversationListDto(
    val id: String,
    val type: String,
    @SerialName("group_id") val groupId: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("conversation_members") val members: List<ConversationMemberDto> = emptyList(),
) {
    fun toDomain(
        currentUserId: String,
        otherUserProfile: ProfileDto?,
        lastMessage: SimpleMessageDto?
    ): Conversation {
        return Conversation(
            id = id,
            type = ConversationType.valueOf(type.uppercase()),
            groupId = groupId,
            lastMessage = lastMessage?.toDomain(currentUserId),
            unreadCount = 0, // TODO: Calculate from last_read_message_id
            otherUser = otherUserProfile?.toDomain(),
            groupName = null,
            createdAt = Instant.parse(createdAt),
            updatedAt = Instant.parse(updatedAt),
        )
    }
}

@Serializable
private data class ConversationMemberDto(
    @SerialName("user_id") val userId: String,
    @SerialName("last_read_message_id") val lastReadMessageId: String? = null,
)

/**
 * Simple message DTO without nested profiles - for list/last message queries
 */
@Serializable
private data class SimpleMessageDto(
    val id: String,
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("sender_id") val senderId: String,
    val body: String,
    @SerialName("message_type") val messageType: String,
    @SerialName("created_at") val createdAt: String,
) {
    fun toDomain(currentUserId: String): Message {
        return Message(
            id = id,
            conversationId = conversationId,
            senderId = senderId,
            senderName = "", // Not available in simple DTO
            senderInitials = "",
            body = body,
            messageType = MessageType.valueOf(messageType.uppercase()),
            createdAt = Instant.parse(createdAt),
            isCurrentUser = senderId == currentUserId,
        )
    }
}

/**
 * Full message DTO with nested profile - for message detail queries
 */
@Serializable
private data class MessageDto(
    val id: String,
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("sender_id") val senderId: String,
    val body: String,
    @SerialName("message_type") val messageType: String,
    @SerialName("created_at") val createdAt: String,
    val profiles: ProfileDto? = null,
) {
    fun toDomain(currentUserId: String): Message {
        val senderName = profiles?.username ?: "Unknown"
        val initials = senderName.take(2).uppercase()

        return Message(
            id = id,
            conversationId = conversationId,
            senderId = senderId,
            senderName = senderName,
            senderInitials = initials,
            body = body,
            messageType = MessageType.valueOf(messageType.uppercase()),
            createdAt = Instant.parse(createdAt),
            isCurrentUser = senderId == currentUserId,
        )
    }
}

@Serializable
private data class MessageInsertDto(
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("sender_id") val senderId: String,
    val body: String,
    @SerialName("message_type") val messageType: String,
)

@Serializable
private data class ProfileDto(
    val id: String,
    val username: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
) {
    fun toDomain() = User(
        id = id,
        email = "",
        username = username,
        avatarUrl = avatarUrl,
        level = 1,
        xp = 0,
        city = null,
        region = null,
        country = null,
    )
}

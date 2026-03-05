package com.example.kairn.data.repository

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
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val auth: Auth,
    private val postgrest: Postgrest,
    private val realtime: Realtime,
) : ChatRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    
    private var conversationChannel: io.github.jan.supabase.realtime.RealtimeChannel? = null
    private var messageChannel: io.github.jan.supabase.realtime.RealtimeChannel? = null

    override fun getConversations(): Flow<List<Conversation>> {
        scope.launch {
            try {
                loadConversations()
                subscribeToConversationsUpdates()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return _conversations.asStateFlow()
    }

    override suspend fun getOrCreateDirectConversation(userId: String): Result<Conversation> = runCatching {
        val currentUserId = auth.currentUserOrNull()?.id 
            ?: throw IllegalStateException("User not authenticated")

        // Call Supabase function to get or create conversation
        val response = postgrest.rpc(
            function = "get_or_create_direct_conversation",
            parameters = buildJsonObject {
                put("user1_id", currentUserId)
                put("user2_id", userId)
            }
        ).decodeAs<String>()

        // Fetch the conversation details
        getConversation(response).getOrThrow()
    }

    override suspend fun getConversation(conversationId: String): Result<Conversation> = runCatching {
        val currentUserId = auth.currentUserOrNull()?.id 
            ?: throw IllegalStateException("User not authenticated")

        val dto = postgrest.from("conversations")
            .select(
                Columns.raw("""
                    *,
                    conversation_members!inner(
                        user_id,
                        last_read_message_id,
                        profiles:user_id(id, username, email)
                    ),
                    messages(*)
                """.trimIndent())
            ) {
                filter {
                    eq("id", conversationId)
                }
            }
            .decodeSingle<ConversationDto>()

        dto.toDomain(currentUserId)
    }

    override fun getMessages(conversationId: String): Flow<List<Message>> {
        scope.launch {
            try {
                loadMessages(conversationId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        return _messages.map { it[conversationId] ?: emptyList() }
    }

    override suspend fun sendMessage(conversationId: String, body: String): Result<Message> = runCatching {
        val currentUserId = auth.currentUserOrNull()?.id 
            ?: throw IllegalStateException("User not authenticated")

        val messageDto = MessageInsertDto(
            conversationId = conversationId,
            senderId = currentUserId,
            body = body,
            messageType = "TEXT"
        )

        val inserted = postgrest.from("messages")
            .insert(messageDto) {
                select(Columns.raw("""
                    *,
                    profiles:sender_id(username, email)
                """.trimIndent()))
            }
            .decodeSingle<MessageDto>()

        // Update conversation's updated_at
        postgrest.from("conversations")
            .update({
                set("updated_at", kotlinx.datetime.Clock.System.now().toString())
            }) {
                filter {
                    eq("id", conversationId)
                }
            }

        inserted.toDomain(currentUserId)
    }

    override suspend fun markAsRead(conversationId: String, messageId: String): Result<Unit> = runCatching {
        val currentUserId = auth.currentUserOrNull()?.id 
            ?: throw IllegalStateException("User not authenticated")

        postgrest.from("conversation_members")
            .update({
                set("last_read_message_id", messageId)
            }) {
                filter {
                    eq("conversation_id", conversationId)
                    eq("user_id", currentUserId)
                }
            }
    }

    override suspend fun subscribeToConversation(conversationId: String) {
        val currentUserId = auth.currentUserOrNull()?.id ?: return

        // Unsubscribe from previous channel if exists
        messageChannel?.unsubscribe()

        // Subscribe to new messages in this conversation
        // Using unique channel name per conversation
        messageChannel = realtime.channel("messages_$conversationId")
        
        val changeFlow = messageChannel!!.postgresChangeFlow<PostgresAction>("public") {
            table = "messages"
            // Filter is not used in Supabase v3 - we'll filter on the client side
        }
        
        scope.launch {
            changeFlow.collect { action ->
                when (action) {
                    is PostgresAction.Insert -> {
                        // Reload messages to get the latest
                        loadMessages(conversationId)
                    }
                    else -> {}
                }
            }
        }

        messageChannel?.subscribe()
    }

    override suspend fun unsubscribeFromConversation(conversationId: String) {
        messageChannel?.unsubscribe()
        messageChannel = null
    }

    private suspend fun loadConversations() {
        val currentUserId = auth.currentUserOrNull()?.id ?: return

        val dtos = postgrest.from("conversations")
            .select(
                Columns.raw("""
                    *,
                    conversation_members!inner(
                        user_id,
                        last_read_message_id,
                        profiles:user_id(id, username, email)
                    ),
                    messages(
                        id,
                        sender_id,
                        body,
                        message_type,
                        created_at,
                        profiles:sender_id(username, email)
                    )
                """.trimIndent())
            ) {
                filter {
                    // Only get conversations where current user is a member
                    eq("conversation_members.user_id", currentUserId)
                }
                order("updated_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            }
            .decodeList<ConversationDto>()

        _conversations.value = dtos.map { it.toDomain(currentUserId) }
    }

    private suspend fun loadMessages(conversationId: String) {
        val currentUserId = auth.currentUserOrNull()?.id ?: return

        val dtos = postgrest.from("messages")
            .select(
                Columns.raw("""
                    *,
                    profiles:sender_id(username, email)
                """.trimIndent())
            ) {
                filter {
                    eq("conversation_id", conversationId)
                }
                order("created_at", order = io.github.jan.supabase.postgrest.query.Order.ASCENDING)
            }
            .decodeList<MessageDto>()

        _messages.value = _messages.value + (conversationId to dtos.map { it.toDomain(currentUserId) })
    }

    private suspend fun subscribeToConversationsUpdates() {
        conversationChannel?.unsubscribe()

        conversationChannel = realtime.channel("conversations")
        
        val changeFlow = conversationChannel!!.postgresChangeFlow<PostgresAction>("public") {
            table = "conversations"
        }
        
        scope.launch {
            changeFlow.collect {
                // Reload conversations when any changes
                loadConversations()
            }
        }

        conversationChannel?.subscribe()
    }
}

// ==================== DTOs ====================

@Serializable
private data class ConversationDto(
    val id: String,
    val type: String,
    @SerialName("group_id") val groupId: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("conversation_members") val members: List<ConversationMemberDto> = emptyList(),
    val messages: List<MessageDto> = emptyList(),
) {
    fun toDomain(currentUserId: String): Conversation {
        val otherMember = members.firstOrNull { it.userId != currentUserId }
        val lastMessage = messages.maxByOrNull { it.createdAt }
        
        // Calculate unread count
        val lastReadMessageId = members.firstOrNull { it.userId == currentUserId }?.lastReadMessageId
        val unreadCount = if (lastReadMessageId == null) {
            messages.count { it.senderId != currentUserId }
        } else {
            val lastReadTime = messages.firstOrNull { it.id == lastReadMessageId }?.createdAt
            if (lastReadTime != null) {
                messages.count { 
                    it.senderId != currentUserId && it.createdAt > lastReadTime 
                }
            } else 0
        }

        return Conversation(
            id = id,
            type = ConversationType.valueOf(type.uppercase()),
            groupId = groupId,
            lastMessage = lastMessage?.toDomain(currentUserId),
            unreadCount = unreadCount,
            otherUser = otherMember?.profile?.toDomain(),
            groupName = null, // Will be populated for group chats
            createdAt = Instant.parse(createdAt),
            updatedAt = Instant.parse(updatedAt),
        )
    }
}

@Serializable
private data class ConversationMemberDto(
    @SerialName("user_id") val userId: String,
    @SerialName("last_read_message_id") val lastReadMessageId: String? = null,
    val profiles: ProfileDto? = null,
) {
    val profile: ProfileDto?
        get() = profiles
}

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
        val senderName = profiles?.username ?: profiles?.email ?: "Unknown"
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
    val email: String,
) {
    fun toDomain() = User(
        id = id,
        email = email,
        username = username,
        avatarUrl = null,
        level = 1,
        xp = 0,
        city = null,
        region = null,
        country = null,
    )
}

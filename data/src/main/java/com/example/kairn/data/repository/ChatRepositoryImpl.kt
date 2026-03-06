package com.example.kairn.data.repository

import android.util.Log
import com.example.kairn.domain.model.Conversation
import com.example.kairn.domain.model.ConversationType
import com.example.kairn.domain.model.Group
import com.example.kairn.domain.model.GroupMember
import com.example.kairn.domain.model.GroupRole
import com.example.kairn.domain.model.GroupVisibility
import com.example.kairn.domain.model.Message
import com.example.kairn.domain.model.MessageType
import com.example.kairn.domain.model.User
import com.example.kairn.domain.repository.ChatRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
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
internal class ChatRepositoryImpl @Inject constructor(
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
                    order("updated_at", order = Order.DESCENDING)
                }
                .decodeList<ConversationListDto>()

            Log.d(TAG, "refreshConversations: Fetched ${conversationDtos.size} conversations")

            // Step 2: For DIRECT conversations, fetch ALL members (the !inner filter above
            // only returns the current user's member row, so we need a separate query to find
            // the other user in each DIRECT conversation)
            val directConversationIds = conversationDtos
                .filter { it.type.uppercase() == "DIRECT" }
                .map { it.id }

            val directConversationMembersMap: Map<String, List<ConversationMemberDto>> =
                if (directConversationIds.isNotEmpty()) {
                    postgrest.from("conversation_members")
                        .select(Columns.raw("conversation_id, user_id, last_read_message_id")) {
                            filter { isIn("conversation_id", directConversationIds) }
                        }
                        .decodeList<ConversationMemberWithConvIdDto>()
                        .groupBy(
                            { it.conversationId },
                            { ConversationMemberDto(userId = it.userId, lastReadMessageId = it.lastReadMessageId) },
                        )
                } else emptyMap()

            val otherUserIds = directConversationMembersMap.values.flatten()
                .map { it.userId }
                .filter { it != userId }
                .distinct()

            // Step 3: Fetch profiles for other users
            val profilesMap = if (otherUserIds.isNotEmpty()) {
                postgrest.from("profiles")
                    .select(Columns.raw("id, username, avatar_url")) {
                        filter { isIn("id", otherUserIds) }
                    }
                    .decodeList<ProfileDto>()
                    .associateBy { it.id }
            } else emptyMap()

            // Step 4: Fetch group names for GROUP conversations
            val groupIds = conversationDtos.mapNotNull { it.groupId }.distinct()
            val groupNamesMap = if (groupIds.isNotEmpty()) {
                postgrest.from("groups")
                    .select(Columns.raw("id, name")) {
                        filter { isIn("id", groupIds) }
                    }
                    .decodeList<GroupIdNameDto>()
                    .associateBy({ it.id }, { it.name })
            } else emptyMap()

            // Step 5: Fetch last message for each conversation
            val conversationIds = conversationDtos.map { it.id }
            val lastMessagesMap = if (conversationIds.isNotEmpty()) {
                // Get the last message for each conversation
                val allMessages = postgrest.from("messages")
                    .select(Columns.raw("id, conversation_id, sender_id, body, message_type, created_at")) {
                        filter { isIn("conversation_id", conversationIds) }
                        order("created_at", order = Order.DESCENDING)
                    }
                    .decodeList<SimpleMessageDto>()
                
                // Group by conversation and take the first (most recent) of each
                allMessages.groupBy { it.conversationId }
                    .mapValues { (_, messages) -> messages.first() }
            } else emptyMap()

            // Step 6: Convert to domain models
            _conversations.value = conversationDtos.map { dto ->
                // Use the full members list from the separate query for DIRECT conversations
                val allMembers = directConversationMembersMap[dto.id] ?: dto.members
                val otherUserId = allMembers.firstOrNull { it.userId != userId }?.userId
                val otherProfile = otherUserId?.let { profilesMap[it] }
                val lastMessage = lastMessagesMap[dto.id]
                val groupName = dto.groupId?.let { groupNamesMap[it] }

                dto.toDomain(
                    currentUserId = userId,
                    otherUserProfile = otherProfile,
                    lastMessage = lastMessage,
                    groupName = groupName,
                    groupMembers = null, // Don't load members in list view for performance
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

        // For DIRECT conversations, find other user and fetch their profile
        val otherUserId = dto.members.firstOrNull { it.userId != myUserId }?.userId
        
        val otherProfile = if (dto.type.uppercase() == "DIRECT" && otherUserId != null) {
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

        // For GROUP conversations, fetch group details and members
        val groupName: String?
        val groupMembers: List<GroupMember>?
        
        if (dto.type.uppercase() == "GROUP" && dto.groupId != null) {
            val groupDetails = try {
                postgrest.from("groups")
                    .select(Columns.raw("name")) {
                        filter { eq("id", dto.groupId) }
                    }
                    .decodeSingle<GroupNameDto>()
            } catch (e: Exception) {
                Log.w(TAG, "fetchConversationById: Could not fetch group details", e)
                null
            }
            
            groupName = groupDetails?.name
            groupMembers = getGroupMembers(dto.groupId).getOrNull()
        } else {
            groupName = null
            groupMembers = null
        }

        return dto.toDomain(myUserId, otherProfile, null, groupName, groupMembers)
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
                    order("created_at", order = Order.ASCENDING)
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
            createdAt = Clock.System.now(),
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
                set("updated_at", Clock.System.now().toString())
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
        
        try {
            // Create unique channel for this conversation
            val channel = realtime.channel("messages-$conversationId")
            
            // Setup postgres change listener BEFORE subscribing
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "messages"
            }
            
            // Collect changes and refresh messages
            changeFlow.onEach { action ->
                Log.d(TAG, "subscribeToConversation: Realtime ${action.javaClass.simpleName} - refreshing messages")
                refreshMessages(conversationId)
            }.launchIn(realtimeScope)
            
            // Subscribe the channel
            channel.subscribe()
            
            // Store the channel for cleanup
            realtimeChannels[conversationId] = channel
            Log.d(TAG, "subscribeToConversation: SUCCESS - Realtime active for $conversationId")
            
        } catch (e: Exception) {
            Log.e(TAG, "subscribeToConversation: FAILED - $conversationId", e)
        }
    }

    override suspend fun unsubscribeFromConversation(conversationId: String) {
        Log.d(TAG, "unsubscribeFromConversation: $conversationId")
        
        realtimeChannels[conversationId]?.let { channel ->
            try {
                channel.unsubscribe()
                realtimeChannels.remove(conversationId)
                Log.d(TAG, "unsubscribeFromConversation: SUCCESS - $conversationId")
            } catch (e: Exception) {
                Log.e(TAG, "unsubscribeFromConversation: FAILED - $conversationId", e)
            }
        }
    }
    
    // ==================== GROUP MANAGEMENT ====================

    override suspend fun createGroup(
        name: String,
        memberUserIds: List<String>,
        description: String?,
        visibility: GroupVisibility,
    ): Result<Conversation> = runCatching {
        Log.d(TAG, "createGroup: name=$name, members=${memberUserIds.size}")
        
        val myUserId = currentUserId 
            ?: throw IllegalStateException("User not authenticated")

        // Step 1: Create the group
        val groupInsertDto = GroupInsertDto(
            ownerId = myUserId,
            name = name,
            description = description,
            visibility = visibility.name,
        )

        val groupDto = postgrest.from("groups")
            .insert(groupInsertDto) {
                select()
            }
            .decodeSingle<GroupDto>()

        Log.d(TAG, "createGroup: Created group with id=${groupDto.id}")

        // Step 2: Create a conversation for the group
        val conversationInsertDto = buildJsonObject {
            put("type", "GROUP")
            put("group_id", groupDto.id)
        }

        val conversationDto = postgrest.from("conversations")
            .insert(conversationInsertDto) {
                select(Columns.raw("id, type, group_id, created_at, updated_at"))
            }
            .decodeSingle<ConversationListDto>()

        Log.d(TAG, "createGroup: Created conversation with id=${conversationDto.id}")

        // Step 3: Add group members (owner + members)
        val allMemberIds = listOf(myUserId) + memberUserIds
        val memberInserts = allMemberIds.map { userId ->
            GroupMemberInsertDto(
                groupId = groupDto.id,
                userId = userId,
                role = if (userId == myUserId) "OWNER" else "MEMBER",
            )
        }

        postgrest.from("group_members")
            .insert(memberInserts)

        Log.d(TAG, "createGroup: Added ${allMemberIds.size} members")

        // Step 4: Add conversation members
        val conversationMemberInserts = allMemberIds.map { userId ->
            buildJsonObject {
                put("conversation_id", conversationDto.id)
                put("user_id", userId)
            }
        }

        postgrest.from("conversation_members")
            .insert(conversationMemberInserts)

        Log.d(TAG, "createGroup: Added ${allMemberIds.size} conversation members")

        // Step 5: Create system message: "User created the group"
        val myProfile = try {
            postgrest.from("profiles")
                .select(Columns.raw("username")) {
                    filter { eq("id", myUserId) }
                }
                .decodeSingle<ProfileDto>()
        } catch (e: Exception) {
            ProfileDto(id = myUserId, username = "Someone")
        }

        val systemMessageBody = "${myProfile.username ?: "Someone"} created the group"
        val systemMessageInsert = MessageInsertDto(
            conversationId = conversationDto.id,
            senderId = myUserId,
            body = systemMessageBody,
            messageType = "SYSTEM",
        )

        postgrest.from("messages")
            .insert(systemMessageInsert)

        Log.d(TAG, "createGroup: Created system message")

        // Step 6: Fetch the complete conversation
        val conversation = fetchConversationById(conversationDto.id, myUserId)
        
        // Refresh conversations list
        refreshConversations()
        
        Log.d(TAG, "createGroup: SUCCESS - ${conversation.displayName}")
        conversation
    }

    override suspend fun getGroupDetails(groupId: String): Result<Group> = runCatching {
        Log.d(TAG, "getGroupDetails: groupId=$groupId")
        
        val groupDto = postgrest.from("groups")
            .select() {
                filter { eq("id", groupId) }
            }
            .decodeSingle<GroupDto>()

        Group(
            id = groupDto.id,
            ownerId = groupDto.ownerId,
            name = groupDto.name,
            description = groupDto.description,
            visibility = GroupVisibility.valueOf(groupDto.visibility.uppercase()),
            createdAt = Instant.parse(groupDto.createdAt),
            updatedAt = Instant.parse(groupDto.updatedAt),
        )
    }

    override suspend fun getGroupMembers(groupId: String): Result<List<GroupMember>> = runCatching {
        Log.d(TAG, "getGroupMembers: groupId=$groupId")
        
        val memberDtos = postgrest.from("group_members")
            .select(
                Columns.raw("""
                    group_id,
                    user_id,
                    role,
                    joined_at,
                    profiles:user_id(id, username, avatar_url)
                """.trimIndent())
            ) {
                filter { eq("group_id", groupId) }
                order("joined_at", order = Order.ASCENDING)
            }
            .decodeList<GroupMemberWithProfileDto>()

        memberDtos.map { dto ->
            GroupMember(
                groupId = dto.groupId,
                userId = dto.userId,
                role = GroupRole.valueOf(dto.role.uppercase()),
                user = dto.profiles?.toDomain(),
                joinedAt = Instant.parse(dto.joinedAt),
            )
        }
    }

    override suspend fun addGroupMembers(groupId: String, userIds: List<String>): Result<Unit> = runCatching {
        Log.d(TAG, "addGroupMembers: groupId=$groupId, userIds=$userIds")
        
        val myUserId = currentUserId 
            ?: throw IllegalStateException("User not authenticated")

        // Get conversation ID for this group
        val conversationDto = postgrest.from("conversations")
            .select(Columns.raw("id")) {
                filter { eq("group_id", groupId) }
            }
            .decodeSingle<ConversationIdDto>()

        // Add group members
        val memberInserts = userIds.map { userId ->
            GroupMemberInsertDto(
                groupId = groupId,
                userId = userId,
                role = "MEMBER",
            )
        }

        postgrest.from("group_members")
            .insert(memberInserts)

        // Add conversation members
        val conversationMemberInserts = userIds.map { userId ->
            buildJsonObject {
                put("conversation_id", conversationDto.id)
                put("user_id", userId)
            }
        }

        postgrest.from("conversation_members")
            .insert(conversationMemberInserts)

        // Create system messages for each added user
        val profiles = postgrest.from("profiles")
            .select(Columns.raw("id, username")) {
                filter { isIn("id", userIds) }
            }
            .decodeList<ProfileDto>()
            .associateBy { it.id }

        val systemMessages = userIds.map { userId ->
            val username = profiles[userId]?.username ?: "Someone"
            MessageInsertDto(
                conversationId = conversationDto.id,
                senderId = myUserId,
                body = "$username joined the group",
                messageType = "SYSTEM",
            )
        }

        postgrest.from("messages")
            .insert(systemMessages)

        Log.d(TAG, "addGroupMembers: SUCCESS")
    }

    override suspend fun removeGroupMember(groupId: String, userId: String): Result<Unit> = runCatching {
        Log.d(TAG, "removeGroupMember: groupId=$groupId, userId=$userId")
        
        val myUserId = currentUserId 
            ?: throw IllegalStateException("User not authenticated")

        // Get conversation ID for this group
        val conversationDto = postgrest.from("conversations")
            .select(Columns.raw("id")) {
                filter { eq("group_id", groupId) }
            }
            .decodeSingle<ConversationIdDto>()

        // Get username before removing
        val profile = try {
            postgrest.from("profiles")
                .select(Columns.raw("username")) {
                    filter { eq("id", userId) }
                }
                .decodeSingle<ProfileDto>()
        } catch (e: Exception) {
            ProfileDto(id = userId, username = "Someone")
        }

        // Remove from group_members
        postgrest.from("group_members")
            .delete {
                filter {
                    eq("group_id", groupId)
                    eq("user_id", userId)
                }
            }

        // Remove from conversation_members
        postgrest.from("conversation_members")
            .delete {
                filter {
                    eq("conversation_id", conversationDto.id)
                    eq("user_id", userId)
                }
            }

        // Create system message
        val systemMessageBody = "${profile.username ?: "Someone"} was removed from the group"
        val systemMessageInsert = MessageInsertDto(
            conversationId = conversationDto.id,
            senderId = myUserId,
            body = systemMessageBody,
            messageType = "SYSTEM",
        )

        postgrest.from("messages")
            .insert(systemMessageInsert)

        Log.d(TAG, "removeGroupMember: SUCCESS")
    }

    override suspend fun leaveGroup(groupId: String): Result<Unit> = runCatching {
        Log.d(TAG, "leaveGroup: groupId=$groupId")
        
        val myUserId = currentUserId 
            ?: throw IllegalStateException("User not authenticated")

        // Get conversation ID for this group
        val conversationDto = postgrest.from("conversations")
            .select(Columns.raw("id")) {
                filter { eq("group_id", groupId) }
            }
            .decodeSingle<ConversationIdDto>()

        // Get my username
        val myProfile = try {
            postgrest.from("profiles")
                .select(Columns.raw("username")) {
                    filter { eq("id", myUserId) }
                }
                .decodeSingle<ProfileDto>()
        } catch (e: Exception) {
            ProfileDto(id = myUserId, username = "Someone")
        }

        // Create system message before leaving
        val systemMessageBody = "${myProfile.username ?: "Someone"} left the group"
        val systemMessageInsert = MessageInsertDto(
            conversationId = conversationDto.id,
            senderId = myUserId,
            body = systemMessageBody,
            messageType = "SYSTEM",
        )

        postgrest.from("messages")
            .insert(systemMessageInsert)

        // Remove from group_members
        postgrest.from("group_members")
            .delete {
                filter {
                    eq("group_id", groupId)
                    eq("user_id", myUserId)
                }
            }

        // Remove from conversation_members
        postgrest.from("conversation_members")
            .delete {
                filter {
                    eq("conversation_id", conversationDto.id)
                    eq("user_id", myUserId)
                }
            }

        Log.d(TAG, "leaveGroup: SUCCESS")
    }

    override suspend fun updateGroup(
        groupId: String,
        name: String?,
        description: String?,
        visibility: GroupVisibility?,
    ): Result<Unit> = runCatching {
        Log.d(TAG, "updateGroup: groupId=$groupId")
        
        val updates = buildJsonObject {
            name?.let { put("name", it) }
            description?.let { put("description", it) }
            visibility?.let { put("visibility", it.name) }
            put("updated_at", Clock.System.now().toString())
        }

        postgrest.from("groups")
            .update(updates) {
                filter { eq("id", groupId) }
            }

        Log.d(TAG, "updateGroup: SUCCESS")
    }

    override suspend fun deleteGroup(groupId: String): Result<Unit> = runCatching {
        Log.d(TAG, "deleteGroup: groupId=$groupId")
        
        // Get conversation ID for this group
        val conversationDto = postgrest.from("conversations")
            .select(Columns.raw("id")) {
                filter { eq("group_id", groupId) }
            }
            .decodeSingle<ConversationIdDto>()

        // Delete messages (cascade should handle this, but being explicit)
        postgrest.from("messages")
            .delete {
                filter { eq("conversation_id", conversationDto.id) }
            }

        // Delete conversation_members
        postgrest.from("conversation_members")
            .delete {
                filter { eq("conversation_id", conversationDto.id) }
            }

        // Delete conversation
        postgrest.from("conversations")
            .delete {
                filter { eq("id", conversationDto.id) }
            }

        // Delete group_members
        postgrest.from("group_members")
            .delete {
                filter { eq("group_id", groupId) }
            }

        // Delete group
        postgrest.from("groups")
            .delete {
                filter { eq("id", groupId) }
            }

        Log.d(TAG, "deleteGroup: SUCCESS")
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
        lastMessage: SimpleMessageDto?,
        groupName: String? = null,
        groupMembers: List<GroupMember>? = null,
    ): Conversation {
        return Conversation(
            id = id,
            type = ConversationType.valueOf(type.uppercase()),
            groupId = groupId,
            lastMessage = lastMessage?.toDomain(currentUserId),
            unreadCount = 0, // TODO: Calculate from last_read_message_id
            otherUser = otherUserProfile?.toDomain(),
            groupName = groupName,
            groupMembers = groupMembers,
            groupAvatar = null, // TODO: Implement group avatars later
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
 * DTO for conversation_members with conversation_id included (for standalone queries)
 */
@Serializable
private data class ConversationMemberWithConvIdDto(
    @SerialName("conversation_id") val conversationId: String,
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

// ==================== GROUP DTOs ====================

@Serializable
private data class GroupDto(
    val id: String,
    @SerialName("owner_id") val ownerId: String,
    val name: String,
    val description: String? = null,
    val visibility: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
private data class GroupInsertDto(
    @SerialName("owner_id") val ownerId: String,
    val name: String,
    val description: String? = null,
    val visibility: String,
)

@Serializable
private data class GroupMemberDto(
    @SerialName("group_id") val groupId: String,
    @SerialName("user_id") val userId: String,
    val role: String,
    @SerialName("joined_at") val joinedAt: String,
)

@Serializable
private data class GroupMemberInsertDto(
    @SerialName("group_id") val groupId: String,
    @SerialName("user_id") val userId: String,
    val role: String,
)

@Serializable
private data class GroupMemberWithProfileDto(
    @SerialName("group_id") val groupId: String,
    @SerialName("user_id") val userId: String,
    val role: String,
    @SerialName("joined_at") val joinedAt: String,
    val profiles: ProfileDto? = null,
)

@Serializable
private data class ConversationIdDto(
    val id: String,
)

@Serializable
private data class GroupNameDto(
    val name: String,
)

@Serializable
private data class GroupIdNameDto(
    val id: String,
    val name: String,
)

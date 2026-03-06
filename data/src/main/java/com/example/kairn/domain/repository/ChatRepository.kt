package com.example.kairn.domain.repository

import com.example.kairn.domain.model.Conversation
import com.example.kairn.domain.model.Group
import com.example.kairn.domain.model.GroupMember
import com.example.kairn.domain.model.GroupVisibility
import com.example.kairn.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    /**
     * Get all conversations for the current user as a Flow
     */
    fun getConversations(): Flow<List<Conversation>>

    /**
     * Refresh conversations from the server
     */
    suspend fun refreshConversations()

    /**
     * Get or create a direct conversation with another user
     */
    suspend fun getOrCreateDirectConversation(userId: String): Result<Conversation>

    /**
     * Get a specific conversation by ID
     */
    suspend fun getConversation(conversationId: String): Result<Conversation>

    /**
     * Get messages for a conversation as a Flow
     */
    fun getMessages(conversationId: String): Flow<List<Message>>

    /**
     * Refresh messages for a conversation from the server
     */
    suspend fun refreshMessages(conversationId: String)

    /**
     * Send a text message in a conversation
     * Uses optimistic UI - message appears immediately before server confirmation
     */
    suspend fun sendMessage(conversationId: String, body: String): Result<Message>

    /**
     * Mark messages as read up to a specific message
     */
    suspend fun markAsRead(conversationId: String, messageId: String): Result<Unit>

    /**
     * Subscribe to real-time updates for a conversation
     * Currently just loads messages, Realtime can be added later
     */
    suspend fun subscribeToConversation(conversationId: String)

    /**
     * Unsubscribe from real-time updates
     */
    suspend fun unsubscribeFromConversation(conversationId: String)

    // Group management methods

    /**
     * Create a new group with members
     * @param name Group name
     * @param memberUserIds List of user IDs to add as members (excluding creator)
     * @param description Optional group description
     * @param visibility Group visibility (default: PRIVATE)
     * @return Created conversation for the group
     */
    suspend fun createGroup(
        name: String,
        memberUserIds: List<String>,
        description: String? = null,
        visibility: GroupVisibility = GroupVisibility.PRIVATE,
    ): Result<Conversation>

    /**
     * Get group details by group ID
     */
    suspend fun getGroupDetails(groupId: String): Result<Group>

    /**
     * Get all members of a group
     */
    suspend fun getGroupMembers(groupId: String): Result<List<GroupMember>>

    /**
     * Add members to an existing group
     * @param groupId Group ID
     * @param userIds List of user IDs to add
     */
    suspend fun addGroupMembers(groupId: String, userIds: List<String>): Result<Unit>

    /**
     * Remove a member from a group (admin/owner only)
     * @param groupId Group ID
     * @param userId User ID to remove
     */
    suspend fun removeGroupMember(groupId: String, userId: String): Result<Unit>

    /**
     * Leave a group (current user leaves)
     * @param groupId Group ID
     */
    suspend fun leaveGroup(groupId: String): Result<Unit>

    /**
     * Update group information (name, description, visibility)
     * @param groupId Group ID
     * @param name New name (optional)
     * @param description New description (optional)
     * @param visibility New visibility (optional)
     */
    suspend fun updateGroup(
        groupId: String,
        name: String? = null,
        description: String? = null,
        visibility: GroupVisibility? = null,
    ): Result<Unit>

    /**
     * Delete a group (owner only)
     * @param groupId Group ID
     */
    suspend fun deleteGroup(groupId: String): Result<Unit>
}

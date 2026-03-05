package com.example.kairn.domain.repository

import com.example.kairn.domain.model.Conversation
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
}

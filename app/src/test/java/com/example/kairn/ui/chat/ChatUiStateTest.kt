package com.example.kairn.ui.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatUiStateTest {

    // --- ChatListUiState ---

    @Test
    fun chatListUiState_loading_isSingleton() {
        assertEquals(ChatListUiState.Loading, ChatListUiState.Loading)
    }

    @Test
    fun chatListUiState_error_containsMessage() {
        val error = ChatListUiState.Error("Network error")
        assertEquals("Network error", error.message)
    }

    @Test
    fun chatListUiState_success_containsConversations() {
        val success = ChatListUiState.Success(emptyList())
        assertTrue(success.conversations.isEmpty())
    }

    // --- ChatUiState ---

    @Test
    fun chatUiState_defaults() {
        val state = ChatUiState()

        assertEquals("", state.conversationId)
        assertNull(state.conversation)
        assertTrue(state.messages.isEmpty())
        assertEquals("", state.messageInput)
        assertFalse(state.isLoading)
        assertFalse(state.isSending)
        assertNull(state.error)
        assertEquals("", state.conversationName)
    }

    @Test
    fun chatUiState_canBeCopiedWithNewInput() {
        val state = ChatUiState(conversationId = "conv-1")
        val updated = state.copy(messageInput = "Hello!")

        assertEquals("Hello!", updated.messageInput)
        assertEquals("conv-1", updated.conversationId)
    }
}

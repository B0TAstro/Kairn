package com.example.kairn.ui.chat

import com.example.kairn.domain.model.Conversation
import com.example.kairn.domain.model.Message

sealed interface ChatListUiState {
    data object Loading : ChatListUiState
    data class Success(val conversations: List<Conversation>) : ChatListUiState
    data class Error(val message: String) : ChatListUiState
}

data class ChatUiState(
    val conversationId: String = "",
    val messages: List<Message> = emptyList(),
    val messageInput: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null,
    val conversationName: String = "",
)

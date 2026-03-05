package com.example.kairn.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairn.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
) : ViewModel() {

    val chatListUiState: StateFlow<ChatListUiState> = chatRepository
        .getConversations()
        .map<_, ChatListUiState> { conversations ->
            if (conversations.isEmpty()) {
                ChatListUiState.Success(emptyList())
            } else {
                ChatListUiState.Success(conversations)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ChatListUiState.Loading
        )

    private val _chatUiState = MutableStateFlow(ChatUiState())
    val chatUiState: StateFlow<ChatUiState> = _chatUiState.asStateFlow()

    fun loadConversation(conversationId: String, conversationName: String) {
        _chatUiState.update { 
            it.copy(
                conversationId = conversationId,
                conversationName = conversationName,
                isLoading = true
            ) 
        }

        viewModelScope.launch {
            // Subscribe to real-time updates
            chatRepository.subscribeToConversation(conversationId)

            // Load messages
            chatRepository.getMessages(conversationId).collect { messages ->
                _chatUiState.update { 
                    it.copy(
                        messages = messages,
                        isLoading = false
                    ) 
                }
            }
        }
    }

    fun onMessageInputChange(input: String) {
        _chatUiState.update { it.copy(messageInput = input) }
    }

    fun sendMessage() {
        val currentState = _chatUiState.value
        if (currentState.messageInput.isBlank() || currentState.isSending) return

        val message = currentState.messageInput
        _chatUiState.update { it.copy(messageInput = "", isSending = true) }

        viewModelScope.launch {
            chatRepository.sendMessage(
                conversationId = currentState.conversationId,
                body = message
            ).onSuccess {
                _chatUiState.update { it.copy(isSending = false) }
            }.onFailure { error ->
                _chatUiState.update { 
                    it.copy(
                        isSending = false,
                        error = error.message,
                        messageInput = message // Restore message on error
                    )
                }
            }
        }
    }

    fun markAsRead() {
        val currentState = _chatUiState.value
        val lastMessage = currentState.messages.lastOrNull() ?: return

        viewModelScope.launch {
            chatRepository.markAsRead(
                conversationId = currentState.conversationId,
                messageId = lastMessage.id
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Unsubscribe from real-time updates
        viewModelScope.launch {
            chatRepository.unsubscribeFromConversation(_chatUiState.value.conversationId)
        }
    }
}

package com.example.kairn.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairn.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ChatViewModel"

/**
 * ViewModel for Chat functionality
 * 
 * Handles both:
 * - Chat list (all conversations)
 * - Individual chat screen (messages in a conversation)
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
) : ViewModel() {

    // Job to track message collection - can be cancelled when leaving chat
    private var messagesJob: Job? = null

    // ==================== CHAT LIST ====================

    val chatListUiState: StateFlow<ChatListUiState> = chatRepository
        .getConversations()
        .map<_, ChatListUiState> { conversations ->
            ChatListUiState.Success(conversations)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ChatListUiState.Loading
        )

    init {
        // Load conversations on init
        refreshConversations()
    }

    fun refreshConversations() {
        viewModelScope.launch {
            chatRepository.refreshConversations()
        }
    }

    // ==================== INDIVIDUAL CHAT ====================

    private val _chatUiState = MutableStateFlow(ChatUiState())
    val chatUiState: StateFlow<ChatUiState> = _chatUiState.asStateFlow()

    fun loadConversation(conversationId: String, conversationName: String) {
        Log.d(TAG, "loadConversation: id=$conversationId, name=$conversationName")
        
        // Unsubscribe from previous conversation if any
        _chatUiState.value.conversationId.takeIf { it.isNotEmpty() }?.let { previousId ->
            viewModelScope.launch {
                chatRepository.unsubscribeFromConversation(previousId)
            }
        }
        
        // Cancel any existing message collection
        messagesJob?.cancel()
        
        // Reset state for new conversation
        _chatUiState.update { 
            ChatUiState(
                conversationId = conversationId,
                conversationName = conversationName,
                isLoading = true,
                messages = emptyList(),
                messageInput = "",
                isSending = false,
                error = null,
            )
        }

        // Fetch conversation details
        viewModelScope.launch {
            chatRepository.getConversation(conversationId).onSuccess { conversation ->
                _chatUiState.update { it.copy(conversation = conversation) }
            }
        }
        
        // Start collecting messages
        messagesJob = viewModelScope.launch {
            // Subscribe to Realtime updates (also does initial refresh)
            chatRepository.subscribeToConversation(conversationId)
            
            // Then collect updates
            chatRepository.getMessages(conversationId).collect { messages ->
                Log.d(TAG, "loadConversation: Received ${messages.size} messages")
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
        if (currentState.messageInput.isBlank() || currentState.isSending) {
            Log.d(TAG, "sendMessage: Skipping - input blank or already sending")
            return
        }

        val message = currentState.messageInput
        Log.d(TAG, "sendMessage: Sending '$message' to ${currentState.conversationId}")
        
        // Clear input immediately for better UX
        _chatUiState.update { it.copy(messageInput = "", isSending = true) }

        viewModelScope.launch {
            val result = chatRepository.sendMessage(
                conversationId = currentState.conversationId,
                body = message
            )
            
            result.onSuccess { sentMessage ->
                Log.d(TAG, "sendMessage: SUCCESS - id=${sentMessage.id}")
                _chatUiState.update { it.copy(isSending = false) }
            }
            
            result.onFailure { error ->
                Log.e(TAG, "sendMessage: FAILED - ${error.message}", error)
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

    fun clearError() {
        _chatUiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        
        // Unsubscribe from current conversation
        _chatUiState.value.conversationId.takeIf { it.isNotEmpty() }?.let { conversationId ->
            viewModelScope.launch {
                chatRepository.unsubscribeFromConversation(conversationId)
            }
        }
        
        messagesJob?.cancel()
        Log.d(TAG, "onCleared: ViewModel destroyed")
    }
}

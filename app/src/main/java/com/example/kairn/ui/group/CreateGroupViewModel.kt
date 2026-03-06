package com.example.kairn.ui.group

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairn.domain.repository.ChatRepository
import com.example.kairn.domain.repository.FriendshipRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "CreateGroupViewModel"

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val friendshipRepository: FriendshipRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateGroupUiState())
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()

    init {
        loadFriends()
    }

    private fun loadFriends() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            friendshipRepository.getFriends().collect { friendships ->
                Log.d(TAG, "loadFriends: Loaded ${friendships.size} friendships")
                _uiState.update {
                    it.copy(
                        availableFriendships = friendships,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun onGroupNameChanged(name: String) {
        _uiState.update { it.copy(groupName = name) }
    }

    fun onMemberToggled(userId: String) {
        _uiState.update { state ->
            val newSelected = if (userId in state.selectedMemberIds) {
                state.selectedMemberIds - userId
            } else {
                state.selectedMemberIds + userId
            }
            state.copy(selectedMemberIds = newSelected)
        }
    }

    fun createGroup(onSuccess: (String) -> Unit) {
        val currentState = _uiState.value
        
        if (!currentState.isValid) {
            Log.w(TAG, "createGroup: Invalid state - name or members missing")
            _uiState.update { it.copy(error = "Please enter a group name and select at least 2 members") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, error = null) }
            
            chatRepository.createGroup(
                name = currentState.groupName.trim(),
                memberUserIds = currentState.selectedMemberIds.toList(),
            ).onSuccess { conversation ->
                Log.d(TAG, "createGroup: SUCCESS - conversationId=${conversation.id}")
                _uiState.update { it.copy(isCreating = false) }
                onSuccess(conversation.id)
            }.onFailure { error ->
                Log.e(TAG, "createGroup: FAILED", error)
                _uiState.update {
                    it.copy(
                        isCreating = false,
                        error = error.message ?: "Failed to create group"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

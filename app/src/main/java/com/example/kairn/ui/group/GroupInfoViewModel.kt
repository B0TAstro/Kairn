package com.example.kairn.ui.group

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairn.domain.model.GroupRole
import com.example.kairn.domain.repository.AuthRepository
import com.example.kairn.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "GroupInfoViewModel"

@HiltViewModel
class GroupInfoViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val groupId: String = checkNotNull(savedStateHandle["groupId"])

    private val _uiState = MutableStateFlow(GroupInfoUiState())
    val uiState: StateFlow<GroupInfoUiState> = _uiState.asStateFlow()

    init {
        loadGroupDetails()
    }

    private fun loadGroupDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val currentUserId = authRepository.currentUser.value?.id
            if (currentUserId == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "User not authenticated"
                    )
                }
                return@launch
            }

            // Load group details
            val groupResult = chatRepository.getGroupDetails(groupId)
            val membersResult = chatRepository.getGroupMembers(groupId)

            if (groupResult.isFailure || membersResult.isFailure) {
                val error = groupResult.exceptionOrNull() ?: membersResult.exceptionOrNull()
                Log.e(TAG, "loadGroupDetails: FAILED", error)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error?.message ?: "Failed to load group details"
                    )
                }
                return@launch
            }

            val group = groupResult.getOrNull()!!
            val members = membersResult.getOrNull()!!
            val currentUserRole = members.find { it.userId == currentUserId }?.role

            Log.d(TAG, "loadGroupDetails: SUCCESS - ${members.size} members")
            _uiState.update {
                it.copy(
                    group = group,
                    members = members,
                    currentUserRole = currentUserRole,
                    isLoading = false,
                )
            }
        }
    }

    fun removeMember(userId: String) {
        val currentState = _uiState.value
        if (!currentState.canManageMembers) {
            Log.w(TAG, "removeMember: User doesn't have permission")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, error = null) }

            chatRepository.removeGroupMember(groupId, userId)
                .onSuccess {
                    Log.d(TAG, "removeMember: SUCCESS")
                    // Reload group details
                    loadGroupDetails()
                }
                .onFailure { error ->
                    Log.e(TAG, "removeMember: FAILED", error)
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            error = error.message ?: "Failed to remove member"
                        )
                    }
                }
        }
    }

    fun leaveGroup(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, error = null) }

            chatRepository.leaveGroup(groupId)
                .onSuccess {
                    Log.d(TAG, "leaveGroup: SUCCESS")
                    _uiState.update { it.copy(isUpdating = false) }
                    onSuccess()
                }
                .onFailure { error ->
                    Log.e(TAG, "leaveGroup: FAILED", error)
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            error = error.message ?: "Failed to leave group"
                        )
                    }
                }
        }
    }

    fun deleteGroup(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        if (!currentState.canDelete) {
            Log.w(TAG, "deleteGroup: User doesn't have permission")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, error = null) }

            chatRepository.deleteGroup(groupId)
                .onSuccess {
                    Log.d(TAG, "deleteGroup: SUCCESS")
                    _uiState.update { it.copy(isUpdating = false) }
                    onSuccess()
                }
                .onFailure { error ->
                    Log.e(TAG, "deleteGroup: FAILED", error)
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            error = error.message ?: "Failed to delete group"
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

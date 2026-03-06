package com.example.kairn.ui.friends

import com.example.kairn.domain.model.Friendship
import com.example.kairn.domain.model.User

sealed interface FriendListUiState {
    data object Loading : FriendListUiState
    data class Success(
        val friends: List<Friendship>,
        val pendingRequests: List<Friendship>
    ) : FriendListUiState
    data class Error(val message: String) : FriendListUiState
}

data class FriendSearchUiState(
    val searchQuery: String = "",
    val searchResults: List<User> = emptyList(),
    val isSearching: Boolean = false,
)

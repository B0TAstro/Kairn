package com.example.kairn.ui.friends

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairn.domain.repository.ChatRepository
import com.example.kairn.domain.repository.FriendshipRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "FriendViewModel"

@HiltViewModel
class FriendViewModel @Inject constructor(
    private val friendshipRepository: FriendshipRepository,
    private val chatRepository: ChatRepository,
) : ViewModel() {

    val friendListUiState: StateFlow<FriendListUiState> = combine(
        friendshipRepository.getFriends(),
        friendshipRepository.getPendingRequests()
    ) { friends, pending ->
        FriendListUiState.Success(
            friends = friends,
            pendingRequests = pending
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FriendListUiState.Loading
    )

    private val _searchUiState = MutableStateFlow(FriendSearchUiState())
    val searchUiState: StateFlow<FriendSearchUiState> = _searchUiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchResults = searchQuery
        .debounce(300) // Wait 300ms after user stops typing
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                flow {
                    Log.d(TAG, "searchResults flow: executing search for '$query'")
                    emit(performSearch(query))
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Initial data load
        viewModelScope.launch {
            friendshipRepository.refreshFriends()
        }
        viewModelScope.launch {
            friendshipRepository.refreshPendingRequests()
        }

        viewModelScope.launch {
            searchQuery.collect { query ->
                _searchUiState.update { it.copy(searchQuery = query) }
            }
        }

        viewModelScope.launch {
            searchResults.collect { results ->
                _searchUiState.update { it.copy(searchResults = results, isSearching = false) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        Log.d(TAG, "onSearchQueryChange: query='$query'")
        searchQuery.value = query
        _searchUiState.update { it.copy(isSearching = query.isNotBlank()) }
    }

    private suspend fun performSearch(query: String): List<com.example.kairn.domain.model.User> {
        Log.d(TAG, "performSearch: query='$query'")
        val result = friendshipRepository.searchUsers(query)
        result.onSuccess { users ->
            Log.d(TAG, "performSearch: SUCCESS - found ${users.size} users")
            users.forEachIndexed { index, user ->
                Log.d(TAG, "  [$index] id=${user.id}, username=${user.username}")
            }
        }
        result.onFailure { error ->
            Log.e(TAG, "performSearch: FAILED - ${error.message}", error)
        }
        return result.getOrElse { emptyList() }
    }

    fun sendFriendRequest(userId: String) {
        Log.d(TAG, "sendFriendRequest: userId=$userId")
        viewModelScope.launch {
            val result = friendshipRepository.sendFriendRequest(userId)
            result.onSuccess {
                Log.d(TAG, "sendFriendRequest: SUCCESS")
            }
            result.onFailure { error ->
                Log.e(TAG, "sendFriendRequest: FAILED - ${error.message}", error)
            }
        }
    }

    fun acceptFriendRequest(friendshipId: String) {
        viewModelScope.launch {
            friendshipRepository.acceptFriendRequest(friendshipId)
        }
    }

    fun declineFriendRequest(friendshipId: String) {
        viewModelScope.launch {
            friendshipRepository.declineFriendRequest(friendshipId)
        }
    }

    fun startConversationWith(userId: String, onSuccess: (String) -> Unit) {
        Log.d(TAG, "startConversationWith: userId=$userId")
        viewModelScope.launch {
            val result = chatRepository.getOrCreateDirectConversation(userId)
            result.onSuccess { conversation ->
                Log.d(TAG, "startConversationWith: SUCCESS - conversationId=${conversation.id}, displayName=${conversation.displayName}")
                onSuccess(conversation.id)
            }
            result.onFailure { error ->
                Log.e(TAG, "startConversationWith: FAILED - ${error.message}", error)
            }
        }
    }
}

package com.example.kairn.ui.friends

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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendViewModel @Inject constructor(
    private val friendshipRepository: FriendshipRepository,
    private val chatRepository: ChatRepository,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
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
                flowOf(performSearch(query))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
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
        searchQuery.value = query
        _searchUiState.update { it.copy(isSearching = query.isNotBlank()) }
    }

    private suspend fun performSearch(query: String): List<com.example.kairn.domain.model.User> {
        return friendshipRepository.searchUsers(query).getOrElse { emptyList() }
    }

    fun sendFriendRequest(userId: String) {
        viewModelScope.launch {
            friendshipRepository.sendFriendRequest(userId)
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
        viewModelScope.launch {
            chatRepository.getOrCreateDirectConversation(userId)
                .onSuccess { conversation ->
                    onSuccess(conversation.id)
                }
        }
    }
}

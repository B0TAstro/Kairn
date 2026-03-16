package com.example.kairn.ui.friends

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FriendUiStateTest {

    // --- FriendListUiState ---

    @Test
    fun friendListUiState_loading_isSingleton() {
        assertEquals(FriendListUiState.Loading, FriendListUiState.Loading)
    }

    @Test
    fun friendListUiState_error_containsMessage() {
        val error = FriendListUiState.Error("Failed to load")
        assertEquals("Failed to load", error.message)
    }

    @Test
    fun friendListUiState_success_containsFriendsAndRequests() {
        val state = FriendListUiState.Success(
            friends = emptyList(),
            pendingRequests = emptyList(),
        )

        assertTrue(state.friends.isEmpty())
        assertTrue(state.pendingRequests.isEmpty())
    }

    // --- FriendSearchUiState ---

    @Test
    fun friendSearchUiState_defaults() {
        val state = FriendSearchUiState()

        assertEquals("", state.searchQuery)
        assertTrue(state.searchResults.isEmpty())
        assertFalse(state.isSearching)
    }

    @Test
    fun friendSearchUiState_canBeCopied() {
        val state = FriendSearchUiState()
        val updated = state.copy(searchQuery = "alice", isSearching = true)

        assertEquals("alice", updated.searchQuery)
        assertTrue(updated.isSearching)
    }
}

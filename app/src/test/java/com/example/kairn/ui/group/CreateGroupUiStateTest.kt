package com.example.kairn.ui.group

import com.example.kairn.domain.model.Friendship
import com.example.kairn.domain.model.FriendshipStatus
import com.example.kairn.domain.model.User
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateGroupUiStateTest {

    private val now = Instant.parse("2025-01-01T00:00:00Z")

    private fun makeFriendship(userId: String, username: String) = Friendship(
        id = "f-$userId",
        requesterId = "me",
        addresseeId = userId,
        status = FriendshipStatus.ACCEPTED,
        friend = User(id = userId, email = "$username@test.com", username = username),
        createdAt = now,
        updatedAt = now,
    )

    private val friendships = listOf(
        makeFriendship("u1", "Alice"),
        makeFriendship("u2", "Bob"),
        makeFriendship("u3", "Charlie"),
    )

    // --- isValid ---

    @Test
    fun isValid_returnsFalse_whenGroupNameIsBlank() {
        val state = CreateGroupUiState(
            groupName = "",
            selectedMemberIds = setOf("u1", "u2"),
        )

        assertFalse(state.isValid)
    }

    @Test
    fun isValid_returnsFalse_whenLessThanTwoMembers() {
        val state = CreateGroupUiState(
            groupName = "My Group",
            selectedMemberIds = setOf("u1"),
        )

        assertFalse(state.isValid)
    }

    @Test
    fun isValid_returnsTrue_whenNamePresentAndAtLeastTwoMembers() {
        val state = CreateGroupUiState(
            groupName = "My Group",
            selectedMemberIds = setOf("u1", "u2"),
        )

        assertTrue(state.isValid)
    }

    @Test
    fun isValid_returnsFalse_whenGroupNameIsSpaces() {
        val state = CreateGroupUiState(
            groupName = "   ",
            selectedMemberIds = setOf("u1", "u2"),
        )

        // isNotBlank checks for whitespace-only
        assertFalse(state.isValid)
    }

    // --- availableFriends ---

    @Test
    fun availableFriends_mapsFromFriendships() {
        val state = CreateGroupUiState(availableFriendships = friendships)

        assertEquals(3, state.availableFriends.size)
        assertEquals("Alice", state.availableFriends[0].username)
        assertEquals("Bob", state.availableFriends[1].username)
        assertEquals("Charlie", state.availableFriends[2].username)
    }

    @Test
    fun availableFriends_returnsEmptyList_whenNoFriendships() {
        val state = CreateGroupUiState()

        assertTrue(state.availableFriends.isEmpty())
    }

    // --- selectedMembers ---

    @Test
    fun selectedMembers_returnsOnlySelectedFriends() {
        val state = CreateGroupUiState(
            availableFriendships = friendships,
            selectedMemberIds = setOf("u1", "u3"),
        )

        assertEquals(2, state.selectedMembers.size)
        assertEquals("Alice", state.selectedMembers[0].username)
        assertEquals("Charlie", state.selectedMembers[1].username)
    }

    @Test
    fun selectedMembers_returnsEmptyList_whenNoneSelected() {
        val state = CreateGroupUiState(
            availableFriendships = friendships,
            selectedMemberIds = emptySet(),
        )

        assertTrue(state.selectedMembers.isEmpty())
    }

    @Test
    fun selectedMembers_ignoresIdsNotInFriendships() {
        val state = CreateGroupUiState(
            availableFriendships = friendships,
            selectedMemberIds = setOf("unknown-id"),
        )

        assertTrue(state.selectedMembers.isEmpty())
    }
}

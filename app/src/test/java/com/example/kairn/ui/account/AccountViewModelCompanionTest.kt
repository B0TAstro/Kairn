package com.example.kairn.ui.account

import com.example.kairn.domain.model.LeaderboardEntry
import com.example.kairn.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountViewModelCompanionTest {

    // ── getInitials ──────────────────────────────────────────────

    @Test
    fun getInitials_returnsPseudoFirstTwoChars_whenPseudoPresent() {
        val user = User(id = "1", email = "test@test.com", pseudo = "JohnHiker")

        assertEquals("JO", AccountViewModel.getInitials(user))
    }

    @Test
    fun getInitials_returnsPseudoUppercase() {
        val user = User(id = "1", email = "test@test.com", pseudo = "alice")

        assertEquals("AL", AccountViewModel.getInitials(user))
    }

    @Test
    fun getInitials_returnsFirstLastInitials_whenNoPseudo() {
        val user = User(
            id = "1",
            email = "test@test.com",
            pseudo = null,
            firstName = "John",
            lastName = "Doe",
        )

        assertEquals("JD", AccountViewModel.getInitials(user))
    }

    @Test
    fun getInitials_returnsFirstNameInitial_whenLastNameIsNull() {
        val user = User(
            id = "1",
            email = "test@test.com",
            pseudo = null,
            firstName = "Alice",
            lastName = null,
        )

        assertEquals("A", AccountViewModel.getInitials(user))
    }

    @Test
    fun getInitials_returnsLastNameInitial_whenFirstNameIsNull() {
        val user = User(
            id = "1",
            email = "test@test.com",
            pseudo = null,
            firstName = null,
            lastName = "Smith",
        )

        assertEquals("S", AccountViewModel.getInitials(user))
    }

    @Test
    fun getInitials_returnsEmailFirstChar_whenAllNamesNull() {
        val user = User(id = "1", email = "zach@test.com")

        assertEquals("Z", AccountViewModel.getInitials(user))
    }

    @Test
    fun getInitials_skipsPseudo_whenBlank() {
        val user = User(
            id = "1",
            email = "test@test.com",
            pseudo = "  ",
            firstName = "Jean",
            lastName = "Martin",
        )

        assertEquals("JM", AccountViewModel.getInitials(user))
    }

    @Test
    fun getInitials_singleCharPseudo_returnsSingleChar() {
        val user = User(id = "1", email = "test@test.com", pseudo = "A")

        assertEquals("A", AccountViewModel.getInitials(user))
    }

    // ── formatMemberSince ────────────────────────────────────────

    @Test
    fun formatMemberSince_formatsIsoDate() {
        assertEquals("MAI 2023", AccountViewModel.formatMemberSince("2023-05-15T10:30:00Z"))
    }

    @Test
    fun formatMemberSince_formatsJanuary() {
        assertEquals("JAN 2024", AccountViewModel.formatMemberSince("2024-01-01T00:00:00Z"))
    }

    @Test
    fun formatMemberSince_formatsDecember() {
        assertEquals("DEC 2022", AccountViewModel.formatMemberSince("2022-12-25T18:00:00Z"))
    }

    @Test
    fun formatMemberSince_returnsDash_whenNull() {
        assertEquals("—", AccountViewModel.formatMemberSince(null))
    }

    @Test
    fun formatMemberSince_returnsDash_whenBlank() {
        assertEquals("—", AccountViewModel.formatMemberSince(""))
    }

    @Test
    fun formatMemberSince_returnsDash_whenInvalidFormat() {
        assertEquals("—", AccountViewModel.formatMemberSince("not-a-date"))
    }

    @Test
    fun formatMemberSince_returnsFallbackDash_whenMonthOutOfRange() {
        // getOrElse returns "—" for out-of-range index, but year is still appended
        assertEquals("— 2023", AccountViewModel.formatMemberSince("2023-13-01T00:00:00Z"))
    }

    @Test
    fun formatMemberSince_returnsFallbackDash_whenMonthIsZero() {
        // monthNum 0 -> index -1, getOrElse returns "—", result is "— 2023"
        assertEquals("— 2023", AccountViewModel.formatMemberSince("2023-00-01T00:00:00Z"))
    }

    // ── windowedLeaderboard ──────────────────────────────────────

    private fun makeEntries(count: Int, currentUserIndex: Int): List<LeaderboardEntry> {
        return (0 until count).map { i ->
            LeaderboardEntry(
                rank = i + 1,
                userId = "u$i",
                username = "User$i",
                avatarUrl = null,
                level = count - i,
                xp = (count - i) * 100L,
                isCurrentUser = i == currentUserIndex,
            )
        }
    }

    @Test
    fun windowedLeaderboard_returnsEmptyList_whenEntriesEmpty() {
        val result = AccountViewModel.windowedLeaderboard(emptyList())

        assertTrue(result.isEmpty())
    }

    @Test
    fun windowedLeaderboard_returnsAllEntries_whenListSmall() {
        val entries = makeEntries(5, currentUserIndex = 2)
        val result = AccountViewModel.windowedLeaderboard(entries)

        assertEquals(5, result.size)
    }

    @Test
    fun windowedLeaderboard_centersAroundCurrentUser_whenInMiddle() {
        val entries = makeEntries(20, currentUserIndex = 10)
        val result = AccountViewModel.windowedLeaderboard(entries, windowSize = 5)

        assertEquals(11, result.size) // 5 + 1 + 5
        assertTrue(result.any { it.isCurrentUser })
        // Current user should be near the middle
        val currentUserIdx = result.indexOfFirst { it.isCurrentUser }
        assertEquals(5, currentUserIdx) // exactly at position 5 (0-indexed)
    }

    @Test
    fun windowedLeaderboard_showsFromTop_whenUserIsNearTop() {
        val entries = makeEntries(20, currentUserIndex = 2)
        val result = AccountViewModel.windowedLeaderboard(entries, windowSize = 5)

        assertEquals(11, result.size)
        assertEquals(1, result.first().rank) // starts from rank 1
    }

    @Test
    fun windowedLeaderboard_showsFromBottom_whenUserIsNearEnd() {
        val entries = makeEntries(20, currentUserIndex = 18)
        val result = AccountViewModel.windowedLeaderboard(entries, windowSize = 5)

        assertEquals(11, result.size)
        assertEquals(20, result.last().rank) // ends at last rank
    }

    @Test
    fun windowedLeaderboard_returnsTopEntries_whenCurrentUserNotInList() {
        val entries = makeEntries(20, currentUserIndex = -1)
            .map { it.copy(isCurrentUser = false) }
        val result = AccountViewModel.windowedLeaderboard(entries, windowSize = 5)

        assertEquals(11, result.size)
        assertEquals(1, result.first().rank)
    }

    @Test
    fun windowedLeaderboard_respectsCustomWindowSize() {
        val entries = makeEntries(30, currentUserIndex = 15)
        val result = AccountViewModel.windowedLeaderboard(entries, windowSize = 3)

        assertEquals(7, result.size) // 3 + 1 + 3
    }

    @Test
    fun windowedLeaderboard_handlesWindowSizeLargerThanList() {
        val entries = makeEntries(5, currentUserIndex = 2)
        val result = AccountViewModel.windowedLeaderboard(entries, windowSize = 10)

        assertEquals(5, result.size) // can't exceed list size
    }
}

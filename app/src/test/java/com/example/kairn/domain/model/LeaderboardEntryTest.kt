package com.example.kairn.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LeaderboardEntryTest {

    @Test
    fun preview_hasExpectedValues() {
        val entry = LeaderboardEntry.preview

        assertEquals(1, entry.rank)
        assertEquals("123", entry.userId)
        assertEquals("JohnHiker", entry.username)
        assertNull(entry.avatarUrl)
        assertEquals(5, entry.level)
        assertEquals(1250L, entry.xp)
        assertTrue(entry.isCurrentUser)
    }

    @Test
    fun previewList_containsSevenEntries() {
        assertEquals(7, LeaderboardEntry.previewList.size)
    }

    @Test
    fun previewList_hasExactlyOneCurrentUser() {
        val currentUsers = LeaderboardEntry.previewList.filter { it.isCurrentUser }

        assertEquals(1, currentUsers.size)
    }

    @Test
    fun previewList_isSortedByRank() {
        val ranks = LeaderboardEntry.previewList.map { it.rank }

        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7), ranks)
    }

    @Test
    fun previewList_isSortedByXpDescending() {
        val xps = LeaderboardEntry.previewList.map { it.xp }

        assertEquals(xps.sortedDescending(), xps)
    }

    @Test
    fun entry_canHaveAvatarUrl() {
        val entry = LeaderboardEntry(
            rank = 1,
            userId = "u1",
            username = "Test",
            avatarUrl = "https://example.com/avatar.jpg",
            level = 1,
            xp = 100,
            isCurrentUser = false,
        )

        assertEquals("https://example.com/avatar.jpg", entry.avatarUrl)
        assertFalse(entry.isCurrentUser)
    }
}

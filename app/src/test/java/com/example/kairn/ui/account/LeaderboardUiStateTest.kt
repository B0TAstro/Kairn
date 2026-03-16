package com.example.kairn.ui.account

import com.example.kairn.domain.model.LeaderboardEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LeaderboardUiStateTest {

    @Test
    fun defaults_areCorrect() {
        val state = LeaderboardUiState()

        assertEquals(LeaderboardScope.REGIONAL, state.selectedScope)
        assertTrue(state.regionalEntries.isEmpty())
        assertTrue(state.nationalEntries.isEmpty())
        assertTrue(state.globalEntries.isEmpty())
        assertFalse(state.isLoading)
        assertFalse(state.hasGeoData)
    }

    @Test
    fun leaderboardScope_hasThreeValues() {
        val scopes = LeaderboardScope.entries
        assertEquals(3, scopes.size)
        assertEquals(LeaderboardScope.REGIONAL, scopes[0])
        assertEquals(LeaderboardScope.NATIONAL, scopes[1])
        assertEquals(LeaderboardScope.GLOBAL, scopes[2])
    }

    @Test
    fun copy_updatesSelectedScope() {
        val state = LeaderboardUiState()
        val updated = state.copy(selectedScope = LeaderboardScope.GLOBAL)

        assertEquals(LeaderboardScope.GLOBAL, updated.selectedScope)
    }

    @Test
    fun copy_updatesEntries() {
        val entries = LeaderboardEntry.previewList
        val state = LeaderboardUiState()
        val updated = state.copy(globalEntries = entries)

        assertEquals(7, updated.globalEntries.size)
        assertTrue(updated.regionalEntries.isEmpty())
    }
}

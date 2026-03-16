package com.example.kairn.ui.home

import com.example.kairn.domain.model.Hike
import com.example.kairn.domain.model.HikeCategory
import com.example.kairn.domain.model.HikeDifficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeUiStateTest {

    private val hikes = Hike.previewList

    // --- filteredHikes: difficulty filter ---

    @Test
    fun filteredHikes_returnsAllHikes_whenNoDifficultySelected() {
        val state = HomeUiState(nearbyHikes = hikes, selectedDifficulty = null)

        assertEquals(hikes, state.filteredHikes)
    }

    @Test
    fun filteredHikes_filtersEasyHikes_whenEasySelected() {
        val state = HomeUiState(nearbyHikes = hikes, selectedDifficulty = HikeDifficulty.EASY)

        assertTrue(state.filteredHikes.all { it.difficulty == HikeDifficulty.EASY })
        assertEquals(2, state.filteredHikes.size) // Mer de Glace + Forêt de Bénévise
    }

    @Test
    fun filteredHikes_filtersExpertHikes_whenExpertSelected() {
        val state = HomeUiState(nearbyHikes = hikes, selectedDifficulty = HikeDifficulty.EXPERT)

        assertTrue(state.filteredHikes.all { it.difficulty == HikeDifficulty.EXPERT })
        assertEquals(1, state.filteredHikes.size)
    }

    @Test
    fun filteredHikes_returnsEmpty_whenNoneMatchDifficulty() {
        val state = HomeUiState(nearbyHikes = hikes, selectedDifficulty = HikeDifficulty.HARD)

        assertTrue(state.filteredHikes.isEmpty())
    }

    // --- filteredHikes: search query ---

    @Test
    fun filteredHikes_filtersByTitle_caseInsensitive() {
        val state = HomeUiState(nearbyHikes = hikes, searchQuery = "lac")

        // Matches: "Lac Blanc", "Mer de Glace" (glace contains "lac"), "Lac Cornu"
        assertEquals(3, state.filteredHikes.size)
        assertTrue(state.filteredHikes.all { it.title.lowercase().contains("lac") })
    }

    @Test
    fun filteredHikes_filtersByLocation() {
        val state = HomeUiState(nearbyHikes = hikes, searchQuery = "Servoz")

        assertEquals(1, state.filteredHikes.size)
        assertEquals("Forêt de Bénévise", state.filteredHikes.first().title)
    }

    @Test
    fun filteredHikes_ignoresLeadingAndTrailingSpaces() {
        val state = HomeUiState(nearbyHikes = hikes, searchQuery = "  lac  ")

        assertEquals(3, state.filteredHikes.size) // same as "lac" search
    }

    @Test
    fun filteredHikes_returnsAll_whenSearchQueryIsBlank() {
        val state = HomeUiState(nearbyHikes = hikes, searchQuery = "   ")

        assertEquals(hikes.size, state.filteredHikes.size)
    }

    // --- filteredHikes: combined filters ---

    @Test
    fun filteredHikes_combinesDifficultyAndSearch() {
        val state = HomeUiState(
            nearbyHikes = hikes,
            selectedDifficulty = HikeDifficulty.MODERATE,
            searchQuery = "Lac",
        )

        assertEquals(2, state.filteredHikes.size) // Lac Blanc + Lac Cornu (both MODERATE)
    }

    @Test
    fun filteredHikes_returnsEmpty_whenCombinedFilterMatchNothing() {
        val state = HomeUiState(
            nearbyHikes = hikes,
            selectedDifficulty = HikeDifficulty.EXPERT,
            searchQuery = "Lac",
        )

        assertTrue(state.filteredHikes.isEmpty())
    }

    // --- default values ---

    @Test
    fun defaultState_hasExpectedDefaults() {
        val state = HomeUiState()

        assertEquals("Hiker", state.username)
        assertEquals(ANNECY_AUSSEDAT_LABEL, state.location)
        assertEquals("", state.searchQuery)
        assertEquals(null, state.selectedDifficulty)
        assertTrue(state.nearbyHikes.isEmpty())
        assertTrue(state.isLoading)
        assertEquals(null, state.errorMessage)
        assertEquals(false, state.isRunActive)
    }
}

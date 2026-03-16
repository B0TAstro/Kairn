package com.example.kairn.ui.explore

import com.example.kairn.domain.model.Hike
import com.example.kairn.domain.model.HikeCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExploreUiStateExtendedTest {

    private val hikes = Hike.previewList

    @Test
    fun filteredHikes_returnsMountainHikes_whenMountainSelected() {
        val state = ExploreUiState(allHikes = hikes, selectedCategory = HikeCategory.MOUNTAIN)

        assertEquals(3, state.filteredHikes.size)
        assertTrue(state.filteredHikes.all { it.category == HikeCategory.MOUNTAIN })
    }

    @Test
    fun filteredHikes_returnsForestHikes_whenForestSelected() {
        val state = ExploreUiState(allHikes = hikes, selectedCategory = HikeCategory.FOREST)

        assertEquals(1, state.filteredHikes.size)
        assertEquals("Forêt de Bénévise", state.filteredHikes.first().title)
    }

    @Test
    fun filteredHikes_returnsEmpty_whenCaveSelected() {
        val state = ExploreUiState(allHikes = hikes, selectedCategory = HikeCategory.CAVE)

        assertTrue(state.filteredHikes.isEmpty())
    }

    @Test
    fun filteredHikes_returnsEmpty_whenHikesListIsEmpty() {
        val state = ExploreUiState(allHikes = emptyList(), selectedCategory = HikeCategory.MOUNTAIN)

        assertTrue(state.filteredHikes.isEmpty())
    }

    @Test
    fun defaults_areCorrect() {
        val state = ExploreUiState()

        assertTrue(state.allHikes.isEmpty())
        assertNull(state.selectedCategory)
        assertTrue(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun filteredHikes_returnsAllHikes_whenEmptyListAndNoCategory() {
        val state = ExploreUiState(allHikes = emptyList(), selectedCategory = null)

        assertTrue(state.filteredHikes.isEmpty())
    }

    @Test
    fun copy_updatesFields() {
        val state = ExploreUiState(allHikes = hikes, isLoading = true)
        val updated = state.copy(isLoading = false, errorMessage = "Error")

        assertFalse(updated.isLoading)
        assertEquals("Error", updated.errorMessage)
        assertEquals(hikes, updated.allHikes)
    }
}

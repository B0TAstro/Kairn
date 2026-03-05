package com.example.kairn.ui.explore

import com.example.kairn.domain.model.Hike
import com.example.kairn.domain.model.HikeCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class ExploreUiStateTest {

    @Test
    fun filteredHikes_returnsAllHikes_whenNoCategorySelected() {
        val hikes = Hike.previewList
        val uiState = ExploreUiState(allHikes = hikes, selectedCategory = null)

        assertEquals(hikes, uiState.filteredHikes)
    }

    @Test
    fun filteredHikes_returnsOnlyMatchingCategory_whenCategorySelected() {
        val hikes = Hike.previewList
        val uiState = ExploreUiState(allHikes = hikes, selectedCategory = HikeCategory.LAKE)

        assertEquals(2, uiState.filteredHikes.size)
        assertEquals(true, uiState.filteredHikes.all { it.category == HikeCategory.LAKE })
    }
}

package com.example.kairn.ui.catalogue

import com.example.kairn.domain.model.Hike
import com.example.kairn.domain.model.HikeCategory

data class CatalogueUiState(
    val allHikes: List<Hike> = emptyList(),
    val selectedCategory: HikeCategory? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val filteredHikes: List<Hike>
        get() = if (selectedCategory == null) allHikes
        else allHikes.filter { it.category == selectedCategory }
}

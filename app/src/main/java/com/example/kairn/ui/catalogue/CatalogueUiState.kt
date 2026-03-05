package com.example.kairn.ui.catalogue

import com.example.kairn.domain.model.Hike
import com.example.kairn.domain.model.HikeCategory

data class CatalogueUiState(
    val allHikes: List<Hike> = Hike.previewList,
    val selectedCategory: HikeCategory? = null,
) {
    val filteredHikes: List<Hike>
        get() = if (selectedCategory == null) allHikes
        else allHikes.filter { it.category == selectedCategory }
}

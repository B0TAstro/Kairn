package com.example.kairn.ui.catalogue

import com.example.kairn.domain.model.Hike
import com.example.kairn.domain.model.HikeDifficulty

data class CatalogueUiState(
    val allHikes: List<Hike> = Hike.previewList,
    val selectedDifficulty: HikeDifficulty? = null,
) {
    val filteredHikes: List<Hike>
        get() = if (selectedDifficulty == null) allHikes
        else allHikes.filter { it.difficulty == selectedDifficulty }
}

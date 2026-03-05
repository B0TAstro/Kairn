package com.example.kairn.ui.home

import com.example.kairn.domain.model.Hike
import com.example.kairn.domain.model.HikeDifficulty

data class HomeUiState(
    val username: String = "Agnes",
    val location: String = "Localisation...",
    val initials: String = "AG",
    val searchQuery: String = "",
    val selectedDifficulty: HikeDifficulty? = null,
    val nearbyHikes: List<Hike> = Hike.previewList,
    val selectedHike: Hike? = null,
    val isBottomSheetExpanded: Boolean = false,
    val userLatitude: Double? = null,
    val userLongitude: Double? = null,
)

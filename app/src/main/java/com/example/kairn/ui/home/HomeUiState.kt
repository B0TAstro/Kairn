package com.example.kairn.ui.home

import com.example.kairn.domain.model.GpxRoute
import com.example.kairn.domain.model.Hike
import com.example.kairn.domain.model.HikeDifficulty

data class MapCity(
    val name: String,
    val latitude: Double,
    val longitude: Double,
)

data class HomeUiState(
    val username: String = "Agnes",
    val location: String = "Localisation...",
    val initials: String = "AG",
    val searchQuery: String = "",
    val selectedDifficulty: HikeDifficulty? = null,
    val nearbyHikes: List<Hike> = emptyList(),
    val selectedHike: Hike? = null,
    val isBottomSheetExpanded: Boolean = false,
    val userLatitude: Double? = null,
    val userLongitude: Double? = null,
    val selectedCity: MapCity? = null,
    val citySuggestions: List<MapCity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val gpxRoutes: List<GpxRoute> = emptyList(),
    val isLoadingGpx: Boolean = false,
    val gpxError: String? = null,
) {
    val filteredHikes: List<Hike>
        get() {
            val normalizedQuery = searchQuery.trim().lowercase()

            return nearbyHikes.filter { hike ->
                val matchesDifficulty = selectedDifficulty == null || hike.difficulty == selectedDifficulty
                val matchesQuery = normalizedQuery.isBlank() ||
                    hike.title.lowercase().contains(normalizedQuery) ||
                    hike.location.orEmpty().lowercase().contains(normalizedQuery)

                matchesDifficulty && matchesQuery
            }
        }
}

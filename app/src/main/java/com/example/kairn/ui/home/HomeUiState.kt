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
    val username: String = "Hiker",
    val location: String = ANNECY_AUSSEDAT_LABEL,
    val searchQuery: String = "",
    val selectedDifficulty: HikeDifficulty? = null,
    val nearbyHikes: List<Hike> = emptyList(),
    val selectedHike: Hike? = null,
    val isBottomSheetExpanded: Boolean = false,
    val userLatitude: Double? = ANNECY_AUSSEDAT_LATITUDE,
    val userLongitude: Double? = ANNECY_AUSSEDAT_LONGITUDE,
    val selectedCity: MapCity? = DEFAULT_HOME_CITY,
    val citySuggestions: List<MapCity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val gpxRoutes: List<GpxRoute> = emptyList(),
    val isLoadingGpx: Boolean = false,
    val gpxError: String? = null,
    val selectedGpxRoute: GpxRoute? = null,
    val isGpxBottomSheetExpanded: Boolean = false,
    val isRunActive: Boolean = false,
    val activeRunTitle: String = "",
    val activeRunProgress: Float = 0f,
    val activeRunDistanceKm: Double = 0.0,
    val activeRunElapsedMinutes: Int = 0,
    val isRunCompleted: Boolean = false,
    val completedRunTitle: String = "",
    val completedRunDistanceKm: Double = 0.0,
    val completedRunElapsedMinutes: Int = 0,
    val completedRunXpGained: Int = 0,
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

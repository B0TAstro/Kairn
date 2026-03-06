package com.example.kairn.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairn.data.location.LocationService
import com.example.kairn.domain.model.Hike
import com.example.kairn.domain.model.HikeDifficulty
import com.example.kairn.domain.repository.HikeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val hikeRepository: HikeRepository,
    private val locationService: LocationService,
) : ViewModel() {

    private val supportedCities = listOf(
        MapCity(name = "Annecy", latitude = 45.899247, longitude = 6.129384),
        MapCity(name = "Chamonix", latitude = 45.923697, longitude = 6.869433),
        MapCity(name = "Lyon", latitude = 45.764043, longitude = 4.835659),
    )

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var locationCollectionJob: Job? = null

    /** Whether the device currently has fine-location permission. */
    val hasLocationPermission: Boolean
        get() = locationService.hasLocationPermission()

    init {
        loadHikes()
        collectLocation()
    }

    private fun loadHikes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            hikeRepository.getHikes()
                .onSuccess { hikes ->
                    _uiState.update { current ->
                        val selectedCity = current.selectedCity ?: supportedCities.first { it.name == "Chamonix" }
                        current.copy(
                            nearbyHikes = hikes,
                            selectedCity = selectedCity,
                            citySuggestions = citySuggestionsForQuery(current.searchQuery),
                            isLoading = false,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Unknown error")
                    }
                }
        }
    }

    /**
     * Starts collecting GPS updates. Called once in init; if permission is not yet
     * granted the flow completes immediately and [onPermissionGranted] re-triggers it.
     */
    private fun collectLocation() {
        if (!locationService.hasLocationPermission()) return
        if (locationCollectionJob?.isActive == true) return

        locationCollectionJob = viewModelScope.launch {
            locationService.locationUpdates().collect { loc ->
                _uiState.update {
                    it.copy(
                        userLatitude = loc.latitude,
                        userLongitude = loc.longitude,
                        location = loc.cityName,
                    )
                }
            }
        }
    }

    /** Called by the UI after the user grants location permission at runtime. */
    fun onPermissionGranted() {
        collectLocation()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                citySuggestions = citySuggestionsForQuery(query),
            )
        }
    }

    fun onCitySelected(cityName: String) {
        val city = supportedCities.firstOrNull { it.name.equals(cityName, ignoreCase = true) } ?: return
        _uiState.update { current ->
            current.copy(
                searchQuery = city.name,
                location = city.name,
                selectedCity = city,
                citySuggestions = emptyList(),
            )
        }
    }

    fun onDifficultySelected(difficulty: HikeDifficulty?) {
        _uiState.update { it.copy(selectedDifficulty = difficulty) }
    }

    fun onHikeSelected(hike: Hike) {
        _uiState.update { it.copy(selectedHike = hike, isBottomSheetExpanded = true) }
    }

    fun onBottomSheetDismissed() {
        _uiState.update { it.copy(selectedHike = null, isBottomSheetExpanded = false) }
    }

    fun retry() {
        loadHikes()
    }

    private fun citySuggestionsForQuery(query: String): List<MapCity> {
        val normalized = query.trim().lowercase()
        if (normalized.isBlank()) return emptyList()
        return supportedCities.filter { it.name.lowercase().contains(normalized) }
    }
}

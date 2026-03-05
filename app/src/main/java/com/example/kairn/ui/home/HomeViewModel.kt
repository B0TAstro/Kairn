package com.example.kairn.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairn.data.repository.HikeRepositoryImpl
import com.example.kairn.domain.model.Hike
import com.example.kairn.domain.model.HikeDifficulty
import com.example.kairn.domain.repository.HikeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val hikeRepository: HikeRepository = HikeRepositoryImpl(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHikes()
    }

    private fun loadHikes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            hikeRepository.getHikes()
                .onSuccess { hikes ->
                    _uiState.update { it.copy(nearbyHikes = hikes, isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Unknown error")
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
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

    fun onUserLocationUpdated(lat: Double, lon: Double, cityName: String) {
        _uiState.update {
            it.copy(
                userLatitude = lat,
                userLongitude = lon,
                location = cityName,
            )
        }
    }

    fun retry() {
        loadHikes()
    }
}

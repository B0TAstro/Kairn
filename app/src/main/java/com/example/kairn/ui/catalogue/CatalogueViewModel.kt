package com.example.kairn.ui.catalogue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairn.data.repository.HikeRepositoryImpl
import com.example.kairn.domain.model.HikeCategory
import com.example.kairn.domain.repository.HikeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CatalogueViewModel(
    private val hikeRepository: HikeRepository = HikeRepositoryImpl(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogueUiState())
    val uiState: StateFlow<CatalogueUiState> = _uiState.asStateFlow()

    init {
        loadHikes()
    }

    private fun loadHikes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            hikeRepository.getHikes()
                .onSuccess { hikes ->
                    _uiState.update { it.copy(allHikes = hikes, isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Unknown error")
                    }
                }
        }
    }

    fun onCategorySelected(category: HikeCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun retry() {
        loadHikes()
    }
}

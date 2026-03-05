package com.example.kairn.ui.catalogue

import androidx.lifecycle.ViewModel
import com.example.kairn.domain.model.HikeCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CatalogueViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogueUiState())
    val uiState: StateFlow<CatalogueUiState> = _uiState.asStateFlow()

    fun onCategorySelected(category: HikeCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }
}

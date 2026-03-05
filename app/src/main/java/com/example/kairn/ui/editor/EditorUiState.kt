package com.example.kairn.ui.editor

import com.example.kairn.ui.editor.model.EditorPoint
import org.osmdroid.util.GeoPoint

sealed interface EditorUiState {
    data object Loading : EditorUiState
    data class Ready(
        val points: List<EditorPoint> = emptyList(),
        val routes: List<EditorRoute> = emptyList(),
        val isLoadingRoute: Boolean = false,
        val error: String? = null,
        val selectedPointId: String? = null,
    ) : EditorUiState
    
    data class Error(val message: String) : EditorUiState
}

data class EditorRoute(
    val id: String,
    val startPointId: String,
    val endPointId: String,
    val points: List<GeoPoint>,
)
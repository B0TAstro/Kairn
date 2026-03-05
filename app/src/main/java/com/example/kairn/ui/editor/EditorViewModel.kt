package com.example.kairn.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairn.data.repository.GpxStorageService
import com.example.kairn.ui.editor.model.EditorPoint
import com.example.kairn.ui.editor.routing.OsrmService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val gpxStorageService: GpxStorageService,
    private val osrmService: OsrmService,
    private val dispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow<EditorUiState>(EditorUiState.Ready())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    fun addPoint(lat: Double, lon: Double) {
        val currentState = _uiState.value as? EditorUiState.Ready ?: return
        val order = currentState.points.size

        val newPoint = EditorPoint(
            id = UUID.randomUUID().toString(),
            order = order,
            latitude = lat,
            longitude = lon,
        )

        _uiState.update { oldState ->
            when (oldState) {
                is EditorUiState.Ready -> oldState.copy(
                    points = oldState.points + newPoint,
                    selectedPointId = newPoint.id,
                )
                else -> oldState
            }
        }

        calculateRoutesIfNeeded()
    }

    fun removePoint(pointId: String) {
        _uiState.update { oldState ->
            when (oldState) {
                is EditorUiState.Ready -> {
                    val updatedPoints = oldState.points
                        .filter { it.id != pointId }
                        .mapIndexed { index, point -> point.copy(order = index) }

                    val routesToRemove = oldState.routes.filter {
                        it.startPointId == pointId || it.endPointId == pointId
                    }

                    val updatedRoutes = oldState.routes.filterNot { it.id in routesToRemove.map { r -> r.id } }

                    oldState.copy(
                        points = updatedPoints,
                        routes = updatedRoutes,
                        selectedPointId = null,
                    )
                }
                else -> oldState
            }
        }

        calculateRoutesIfNeeded()
    }

    fun reorderPoints(newOrder: List<EditorPoint>) {
        val updatedPoints = newOrder.mapIndexed { index, point -> point.copy(order = index) }

        _uiState.update { oldState ->
            when (oldState) {
                is EditorUiState.Ready -> oldState.copy(
                    points = updatedPoints,
                    routes = emptyList(),
                )
                else -> oldState
            }
        }

        calculateRoutesIfNeeded()
    }

    fun selectPoint(pointId: String?) {
        _uiState.update { oldState ->
            when (oldState) {
                is EditorUiState.Ready -> oldState.copy(selectedPointId = pointId)
                else -> oldState
            }
        }
    }

    fun clearError() {
        _uiState.update { oldState ->
            when (oldState) {
                is EditorUiState.Ready -> oldState.copy(error = null)
                else -> oldState
            }
        }
    }

    private fun calculateRoutesIfNeeded() {
        val currentState = _uiState.value as? EditorUiState.Ready ?: return
        val points = currentState.points

        if (points.size < 2) return

        viewModelScope.launch {
            _uiState.update { oldState ->
                when (oldState) {
                    is EditorUiState.Ready -> oldState.copy(isLoadingRoute = true, error = null)
                    else -> oldState
                }
            }

            val newRoutes = mutableListOf<EditorRoute>()

            for (i in 0 until points.size - 1) {
                val start = points[i]
                val end = points[i + 1]

                try {
                    val routePoints = withContext(dispatcher) {
                        osrmService.calculateRoute(
                            start.latitude,
                            start.longitude,
                            end.latitude,
                            end.longitude,
                        )
                    }

                    val routeId = "${start.id}_${end.id}"
                    val route = EditorRoute(
                        id = routeId,
                        startPointId = start.id,
                        endPointId = end.id,
                        points = routePoints,
                    )
                    newRoutes.add(route)
                } catch (e: Exception) {
                    _uiState.update { oldState ->
                        when (oldState) {
                            is EditorUiState.Ready -> oldState.copy(
                                error = "Failed to calculate route between points ${i + 1} and ${i + 2}: ${e.message}",
                            )
                            else -> oldState
                        }
                    }
                    break
                }
            }

            _uiState.update { oldState ->
                when (oldState) {
                    is EditorUiState.Ready -> oldState.copy(
                        routes = newRoutes,
                        isLoadingRoute = false,
                    )
                    else -> oldState
                }
            }
        }
    }

    fun getRouteForSegment(startPointId: String, endPointId: String): List<GeoPoint>? {
        val currentState = _uiState.value as? EditorUiState.Ready ?: return null
        return currentState.routes.find { it.startPointId == startPointId && it.endPointId == endPointId }?.points
    }

    fun getAllRoutePoints(): List<GeoPoint> {
        val currentState = _uiState.value as? EditorUiState.Ready ?: return emptyList()
        return currentState.routes.flatMap { it.points }
    }

    fun saveToSupabase() {
        val currentState = _uiState.value as? EditorUiState.Ready ?: return
        val points = currentState.points
        val routes = currentState.routes.map { it.points }

        if (points.isEmpty()) return

        _uiState.update { oldState ->
            when (oldState) {
                is EditorUiState.Ready -> oldState.copy(
                    isSaving = true,
                    saveError = null,
                    saveSuccess = false,
                )
                else -> oldState
            }
        }

        viewModelScope.launch {
            try {
                val result = withContext(dispatcher) {
                    gpxStorageService.uploadGpx(points, routes)
                }

                _uiState.update { oldState ->
                    when (oldState) {
                        is EditorUiState.Ready -> oldState.copy(
                            isSaving = false,
                            saveSuccess = result.isSuccess,
                            saveError = if (result.isFailure) result.exceptionOrNull()?.message else null,
                        )
                        else -> oldState
                    }
                }
            } catch (e: Exception) {
                _uiState.update { oldState ->
                    when (oldState) {
                        is EditorUiState.Ready -> oldState.copy(
                            isSaving = false,
                            saveSuccess = false,
                            saveError = e.message ?: "Unknown error",
                        )
                        else -> oldState
                    }
                }
            }
        }
    }

    fun clearSaveStatus() {
        _uiState.update { oldState ->
            when (oldState) {
                is EditorUiState.Ready -> oldState.copy(
                    saveSuccess = false,
                    saveError = null,
                )
                else -> oldState
            }
        }
    }
}

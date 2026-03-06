package com.example.kairn.ui.editor.components

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.kairn.ui.editor.EditorViewModel
import com.example.kairn.ui.editor.EditorUiState
import com.example.kairn.ui.editor.map.MapProvider

@Composable
fun EditorMap(
    viewModel: EditorViewModel,
    mapProvider: MapProvider,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    val uiState by viewModel.uiState.collectAsState()
    val readyState = uiState as? EditorUiState.Ready
    Log.d("EditorMap", "uiState type: ${uiState::class.simpleName}, readyState: $readyState")

    val mapViewWrapper = remember { mapProvider.createMapView(context) }
    val mapView = mapViewWrapper.view

var displayedPointIds by remember { mutableStateOf(setOf<String>()) }

    Log.d("EditorMap", "Map provider created: ${mapProvider::class.simpleName}")

DisposableEffect(mapProvider) {
        mapProvider.setOnMapClickListener { lat, lon ->
            viewModel.addPoint(lat, lon)
        }
        onDispose { mapProvider.setOnMapClickListener { _, _ -> Unit } }
    }

    // Effet pour l'initialisation des points existants
    LaunchedEffect(readyState) {
        val state = readyState ?: return@LaunchedEffect
        if (state.points.isNotEmpty() && displayedPointIds.isEmpty()) {
            Log.d("EditorMap", "Initializing points on first load")
            state.points.forEach { point ->
                mapProvider.addPoint(point.latitude, point.longitude, point.id)
            }
            displayedPointIds = state.points.map { it.id }.toSet()
        }
    }

    LaunchedEffect(readyState?.points, readyState?.routes) {
        val state = readyState ?: return@LaunchedEffect
        Log.d("EditorMap", "LaunchedEffect triggered with ${state.points.size} points")

        val currentPointIds = state.points.map { it.id }.toSet()
        val removedPointIds = displayedPointIds - currentPointIds
        removedPointIds.forEach { id -> 
            Log.d("EditorMap", "Removing point: $id")
            mapProvider.removePoint(id) 
        }

        val addedPoints = state.points.filterNot { it.id in displayedPointIds }
        addedPoints.forEach { point ->
            Log.d("EditorMap", "Adding point: ${point.id} at ${point.latitude}, ${point.longitude}")
            mapProvider.addPoint(point.latitude, point.longitude, point.id)
        }

        displayedPointIds = currentPointIds

        val allRoutePoints = state.routes.flatMap { it.points }
        if (allRoutePoints.isNotEmpty()) {
            Log.d("EditorMap", "Drawing route with ${allRoutePoints.size} points")
            mapProvider.drawRoute(allRoutePoints, "full_route")
        } else {
            Log.d("EditorMap", "Clearing routes")
            mapProvider.clearRoutes()
        }

        state.points.lastOrNull()?.let { lastPoint ->
            Log.d("EditorMap", "Centering map on last point")
            mapProvider.centerMap(lastPoint.latitude, lastPoint.longitude)
        }
    }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapProvider.onStart()
                Lifecycle.Event.ON_RESUME -> mapProvider.onResume()
                Lifecycle.Event.ON_PAUSE -> mapProvider.onPause()
                Lifecycle.Event.ON_STOP -> mapProvider.onStop()
                Lifecycle.Event.ON_DESTROY -> mapProvider.onDestroy()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            if (lifecycle.currentState != Lifecycle.State.DESTROYED) {
                mapProvider.onStop()
                mapProvider.onDestroy()
            }
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
    )
}

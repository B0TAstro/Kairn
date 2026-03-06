package com.example.kairn.ui.editor.components

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

    val mapViewWrapper = remember { mapProvider.createMapView(context) }
    val mapView = mapViewWrapper.view

    var displayedPointIds by remember { mutableStateOf(setOf<String>()) }

    DisposableEffect(mapProvider) {
        mapProvider.setOnMapClickListener { lat, lon ->
            viewModel.addPoint(lat, lon)
        }
        onDispose { mapProvider.setOnMapClickListener { _, _ -> Unit } }
    }

    LaunchedEffect(readyState?.points, readyState?.routes) {
        val state = readyState ?: return@LaunchedEffect

        val currentPointIds = state.points.map { it.id }.toSet()
        val removedPointIds = displayedPointIds - currentPointIds
        removedPointIds.forEach { id -> mapProvider.removePoint(id) }

        val addedPoints = state.points.filterNot { it.id in displayedPointIds }
        addedPoints.forEach { point ->
            mapProvider.addPoint(point.latitude, point.longitude, point.id)
        }

        displayedPointIds = currentPointIds

        val allRoutePoints = state.routes.flatMap { it.points }
        if (allRoutePoints.isNotEmpty()) {
            mapProvider.drawRoute(allRoutePoints, "full_route")
        } else {
            mapProvider.clearRoutes()
        }

        state.points.lastOrNull()?.let { lastPoint ->
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

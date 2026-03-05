package com.example.kairn.ui.editor.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kairn.ui.editor.EditorViewModel
import com.example.kairn.ui.editor.map.MapProvider
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun EditorMap(
    viewModel: EditorViewModel,
    mapProvider: MapProvider,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    
    val mapViewWrapper = remember { mapProvider.createMapView(context) }
    val mapView = mapViewWrapper.view as MapView
    
    mapProvider.setOnMapClickListener { lat, lon ->
        viewModel.addPoint(lat, lon)
    }
    
    LaunchedEffect(viewModel.uiState) {
        val state = viewModel.uiState.value
        if (state is com.example.kairn.ui.editor.EditorUiState.Ready) {
            val allRoutePoints = viewModel.getAllRoutePoints()
            if (allRoutePoints.isNotEmpty()) {
                mapProvider.drawRoute(allRoutePoints, "full_route")
            }
        }
    }
    
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    
    AndroidView(
        factory = { mapView },
        modifier = modifier,
    )
}
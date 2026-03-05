package com.example.kairn.ui.home

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.kairn.BuildConfig
import androidx.compose.material3.Text
import com.mapbox.common.MapboxOptions
import com.mapbox.maps.AnnotatedFeature
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.ViewAnnotationOptions

@Composable
fun MapboxPocMapView(
    modifier: Modifier = Modifier,
    userLatitude: Double? = null,
    userLongitude: Double? = null,
    selectedCity: MapCity? = null,
    markers: List<HikeMapMarker> = emptyList(),
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val token = BuildConfig.MAPBOX_ACCESS_TOKEN.trim()
    var mapInitError by remember { mutableStateOf<String?>(null) }
    var styleReady by remember { mutableStateOf(false) }

    if (token.isBlank()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Mapbox token missing. Set MAPBOX_ACCESS_TOKEN in local.properties")
        }
        return
    }

    val mapView = remember {
        MapboxOptions.accessToken = token
        runCatching {
            createMapboxMapView(context) {
                styleReady = true
            }
        }
            .onFailure { mapInitError = it.message ?: "Mapbox initialization failed" }
            .getOrNull()
    }

    if (mapInitError != null || mapView == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Mapbox init failed: ${mapInitError ?: "unknown error"}")
        }
        return
    }

    LaunchedEffect(userLatitude, userLongitude, selectedCity, mapView) {
        if (selectedCity != null) {
            mapView.mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(com.mapbox.geojson.Point.fromLngLat(selectedCity.longitude, selectedCity.latitude))
                    .zoom(13.6)
                    .pitch(68.0)
                    .bearing(25.0)
                    .build(),
            )
            return@LaunchedEffect
        }

        if (userLatitude != null && userLongitude != null) {
            mapView.mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(com.mapbox.geojson.Point.fromLngLat(userLongitude, userLatitude))
                    .zoom(14.8)
                    .pitch(68.0)
                    .bearing(20.0)
                    .build(),
            )
        }
    }

    LaunchedEffect(mapView, markers, styleReady) {
        if (!styleReady) return@LaunchedEffect

        val viewAnnotationManager = mapView.viewAnnotationManager
        viewAnnotationManager.removeAllViewAnnotations()
        if (markers.isEmpty()) return@LaunchedEffect

        markers.forEach { marker ->
            val markerView = createMarkerView(context)
            val options = ViewAnnotationOptions.Builder()
                .annotatedFeature(
                    AnnotatedFeature.valueOf(
                        com.mapbox.geojson.Point.fromLngLat(marker.longitude, marker.latitude),
                    ),
                )
                .allowOverlap(true)
                .build()
            viewAnnotationManager.addViewAnnotation(markerView, options)
        }
    }

    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
    )
}

private fun createMapboxMapView(
    context: Context,
    onStyleReady: () -> Unit,
): MapView {
    return MapView(context).apply {
        mapboxMap.loadStyle("mapbox://styles/mapbox/outdoors-v12") {
            onStyleReady()
            mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(com.mapbox.geojson.Point.fromLngLat(6.9850, 45.8900))
                    .zoom(12.8)
                    .pitch(68.0)
                    .bearing(28.0)
                    .build(),
            )
        }
    }
}

private fun createMarkerView(context: Context): View {
    val sizePx = (14 * context.resources.displayMetrics.density).toInt()
    return View(context).apply {
        layoutParams = android.view.ViewGroup.LayoutParams(sizePx, sizePx)
        background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(android.graphics.Color.parseColor("#587B6C"))
            setStroke((2 * context.resources.displayMetrics.density).toInt(), android.graphics.Color.parseColor("#ECE7DF"))
        }
    }
}

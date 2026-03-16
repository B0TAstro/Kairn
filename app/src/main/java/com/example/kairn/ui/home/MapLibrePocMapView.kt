package com.example.kairn.ui.home

import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.kairn.domain.model.GpxRoute
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.annotations.PolylineOptions

private val DEFAULT_TARGET = LatLng(45.899247, 6.129384)
private const val CAMERA_ZOOM_3D = 13.8
private const val CAMERA_TILT_3D = 62.0
private const val CAMERA_BEARING_3D = 22.0
private const val ROUTE_CLICK_THRESHOLD = 0.0012

private const val TERRAIN_STYLE_JSON = """
{
  "version": 8,
  "sources": {
    "osm": {
      "type": "raster",
      "tiles": ["https://a.tile.openstreetmap.org/{z}/{x}/{y}.png"],
      "tileSize": 256,
      "attribution": "© OpenStreetMap Contributors",
      "maxzoom": 19
    },
    "terrainSource": {
      "type": "raster-dem",
      "url": "https://demotiles.maplibre.org/terrain-tiles/tiles.json",
      "tileSize": 256
    },
    "hillshadeSource": {
      "type": "raster-dem",
      "url": "https://demotiles.maplibre.org/terrain-tiles/tiles.json",
      "tileSize": 256
    }
  },
  "layers": [
    {
      "id": "osm",
      "type": "raster",
      "source": "osm"
    },
    {
      "id": "hills",
      "type": "hillshade",
      "source": "hillshadeSource",
      "paint": {
        "hillshade-shadow-color": "#473B24"
      }
    }
  ],
    "terrain": {
      "source": "terrainSource",
      "exaggeration": 1.6
    },
  "sky": {}
}
"""

@Composable
fun MapLibrePocMapView(
    modifier: Modifier = Modifier,
    userLatitude: Double? = null,
    userLongitude: Double? = null,
    selectedCity: MapCity? = null,
    gpxRoutes: List<GpxRoute> = emptyList(),
    selectedGpxRoute: GpxRoute? = null,
    onGpxRouteClick: (GpxRoute) -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    val currentRoutes by rememberUpdatedState(gpxRoutes)
    val currentOnRouteClick by rememberUpdatedState(onGpxRouteClick)

    val mapView = remember {
        createMapView(context).apply {
            getMapAsync { mapLibreMap ->
                map = mapLibreMap
                mapLibreMap.setStyle(
                    Style.Builder().fromJson(TERRAIN_STYLE_JSON),
                ) {
                    mapLibreMap.cameraPosition = CameraPosition.Builder()
                        .target(DEFAULT_TARGET)
                        .zoom(CAMERA_ZOOM_3D)
                        .tilt(CAMERA_TILT_3D)
                        .bearing(CAMERA_BEARING_3D)
                        .build()
                }
            }
        }
    }

    LaunchedEffect(map, userLatitude, userLongitude, selectedCity) {
        val mapLibreMap = map ?: return@LaunchedEffect
        val target = when {
            selectedCity != null -> LatLng(selectedCity.latitude, selectedCity.longitude)
            userLatitude != null && userLongitude != null -> LatLng(userLatitude, userLongitude)
            else -> null
        } ?: return@LaunchedEffect

        mapLibreMap.cameraPosition = CameraPosition.Builder()
            .target(target)
            .zoom(CAMERA_ZOOM_3D)
            .tilt(CAMERA_TILT_3D)
            .bearing(CAMERA_BEARING_3D)
            .build()
    }

    LaunchedEffect(gpxRoutes, map, selectedGpxRoute) {
        val mapLibreMap = map ?: return@LaunchedEffect

        for (annotation in mapLibreMap.annotations) {
            annotation.remove()
        }

        for (route in gpxRoutes) {
            if (route.points.size >= 2) {
                val isSelected = selectedGpxRoute?.fileName == route.fileName
                val points = route.points.map { LatLng(it.latitude, it.longitude) }
                val polylineOptions = PolylineOptions()
                    .addAll(points)
                    .color(android.graphics.Color.parseColor(if (isSelected) "#BA8C5E" else "#587B6C"))
                    .width(if (isSelected) 8f else 5f)
                mapLibreMap.addPolyline(polylineOptions)
            }
        }
    }

    DisposableEffect(map) {
        val mapLibreMap = map
        if (mapLibreMap == null) {
            onDispose { }
        } else {
            val clickListener = MapLibreMap.OnMapClickListener { clickPoint ->
                val route = currentRoutes.firstOrNull { candidate ->
                    candidate.points.size >= 2 && isPointNearRoute(
                        clickLatitude = clickPoint.latitude,
                        clickLongitude = clickPoint.longitude,
                        route = candidate,
                        threshold = ROUTE_CLICK_THRESHOLD,
                    )
                }
                if (route != null) {
                    currentOnRouteClick(route)
                    true
                } else {
                    false
                }
            }

            mapLibreMap.addOnMapClickListener(clickListener)
            onDispose {
                mapLibreMap.removeOnMapClickListener(clickListener)
            }
        }
    }

    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
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

private fun createMapView(context: Context): MapView {
    MapLibre.getInstance(context)
    return MapView(context).apply {
        onCreate(Bundle())
    }
}

private fun isPointNearRoute(
    clickLatitude: Double,
    clickLongitude: Double,
    route: GpxRoute,
    threshold: Double,
): Boolean {
    return route.points.any { point ->
        kotlin.math.abs(point.latitude - clickLatitude) <= threshold &&
            kotlin.math.abs(point.longitude - clickLongitude) <= threshold
    }
}

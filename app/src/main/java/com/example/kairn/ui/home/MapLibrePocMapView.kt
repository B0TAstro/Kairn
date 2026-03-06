package com.example.kairn.ui.home

import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

private val DEFAULT_TARGET = LatLng(47.27574, 11.39085)

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
    "exaggeration": 1.4
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
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var map by remember { mutableStateOf<MapLibreMap?>(null) }

    val mapView = remember {
        createMapView(context).apply {
            getMapAsync { mapLibreMap ->
                map = mapLibreMap
                mapLibreMap.setStyle(
                    Style.Builder().fromJson(TERRAIN_STYLE_JSON),
                ) {
                    mapLibreMap.cameraPosition = CameraPosition.Builder()
                        .target(DEFAULT_TARGET)
                        .zoom(13.6)
                        .tilt(72.0)
                        .bearing(25.0)
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
            .zoom(13.6)
            .tilt(72.0)
            .bearing(25.0)
            .build()
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

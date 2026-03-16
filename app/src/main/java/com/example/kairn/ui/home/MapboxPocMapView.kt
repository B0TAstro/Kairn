package com.example.kairn.ui.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
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
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.get
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.addLayerBelow
import com.mapbox.maps.extension.style.layers.generated.fillExtrusionLayer
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.rasterDemSource
import com.mapbox.maps.extension.style.terrain.generated.setTerrain
import com.mapbox.maps.extension.style.terrain.generated.terrain
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotation
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.example.kairn.domain.model.GpxRoute
import android.util.Log
import kotlin.math.abs

private const val TAG = "MapboxPocMapView"
private const val TERRAIN_SOURCE_ID = "kairn-terrain-dem"
private const val BUILDINGS_LAYER_ID = "kairn-3d-buildings"
private const val USER_MARKER_IMAGE_ID = "kairn-user-location-marker"
private const val USER_MARKER_ICON_SCALE = 1.9
private const val USER_MARKER_FOCUS_ZOOM = 17.2
private const val USER_MARKER_CLICK_THRESHOLD = 0.0011

@Composable
fun MapboxPocMapView(
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
    val token = BuildConfig.MAPBOX_ACCESS_TOKEN.trim()
    var mapInitError by remember { mutableStateOf<String?>(null) }
    var polylineManager by remember { mutableStateOf<PolylineAnnotationManager?>(null) }
    var userPointManager by remember { mutableStateOf<PointAnnotationManager?>(null) }
    val polylines = remember { mutableMapOf<String, PolylineAnnotation>() }
    var userLocationPoint by remember { mutableStateOf<PointAnnotation?>(null) }

    if (token.isBlank()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Mapbox token missing. Set MAPBOX_ACCESS_TOKEN in local.properties")
        }
        return
    }

    val mapView = remember {
        MapboxOptions.accessToken = token
        runCatching {
            createMapboxMapView(context) { lineManager, pointManager ->
                polylineManager = lineManager
                userPointManager = pointManager
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
                    .center(Point.fromLngLat(selectedCity.longitude, selectedCity.latitude))
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
                    .center(Point.fromLngLat(userLongitude, userLatitude))
                    .zoom(14.8)
                    .pitch(68.0)
                    .bearing(20.0)
                    .build(),
            )
        }
    }

    LaunchedEffect(userLatitude, userLongitude, userPointManager) {
        val manager = userPointManager ?: return@LaunchedEffect
        val latitude = userLatitude ?: return@LaunchedEffect
        val longitude = userLongitude ?: return@LaunchedEffect
        val nextPoint = Point.fromLngLat(longitude, latitude)

        val existing = userLocationPoint
        if (existing == null) {
            userLocationPoint = manager.create(
                PointAnnotationOptions()
                    .withPoint(nextPoint)
                    .withIconImage(USER_MARKER_IMAGE_ID)
                    .withIconSize(USER_MARKER_ICON_SCALE)
                    .withIconAnchor(IconAnchor.BOTTOM),
            )
        } else {
            existing.point = nextPoint
            manager.update(existing)
        }
    }

    LaunchedEffect(gpxRoutes, polylineManager, mapView, selectedGpxRoute) {
        if (polylineManager == null || mapView == null) return@LaunchedEffect
        
        Log.d(TAG, "GPX routes updated: ${gpxRoutes.size} routes, selected: ${selectedGpxRoute?.fileName}")
        
        polylines.values.forEach { polylineManager!!.delete(it) }
        polylines.clear()
        
        for ((index, route) in gpxRoutes.withIndex()) {
            if (route.points.size >= 2) {
                val isSelected = selectedGpxRoute?.fileName == route.fileName
                val key = route.fileName ?: "route_$index"
                val polyline = polylineManager!!.create(
                    PolylineAnnotationOptions()
                        .withPoints(route.points.map { Point.fromLngLat(it.longitude, it.latitude) })
                        .withLineColor(if (isSelected) "#BA8C5E" else "#587B6C")
                        .withLineWidth(if (isSelected) 8.0 else 5.0),
                )
                polylines[key] = polyline
                Log.d(TAG, "Added polyline for ${route.name} with ${route.points.size} points, selected: $isSelected")
            }
        }
    }

    LaunchedEffect(polylineManager, mapView, gpxRoutes, userLatitude, userLongitude) {
        if (polylineManager == null || mapView == null) return@LaunchedEffect
        
        val clickListener = OnMapClickListener { point ->
            val clickLng = point.longitude()
            val clickLat = point.latitude()
            val threshold = 0.001

            if (userLatitude != null && userLongitude != null) {
                val clickedUserMarker =
                    abs(userLongitude - clickLng) < USER_MARKER_CLICK_THRESHOLD &&
                        abs(userLatitude - clickLat) < USER_MARKER_CLICK_THRESHOLD
                if (clickedUserMarker) {
                    mapView.mapboxMap.setCamera(
                        CameraOptions.Builder()
                            .center(Point.fromLngLat(userLongitude, userLatitude))
                            .zoom(USER_MARKER_FOCUS_ZOOM)
                            .pitch(74.0)
                            .bearing(20.0)
                            .build(),
                    )
                    return@OnMapClickListener true
                }
            }
            
            for ((fileName, _) in polylines) {
                val route = gpxRoutes.find { it.fileName == fileName }
                if (route != null) {
                    val isClose = route.points.any { p ->
                        Math.abs(p.longitude - clickLng) < threshold && 
                        Math.abs(p.latitude - clickLat) < threshold
                    }
                    if (isClose) {
                        Log.d(TAG, "Clicked on route: $fileName")
                        onGpxRouteClick(route)
                        return@OnMapClickListener true
                    }
                }
            }
            
            Log.d(TAG, "Clicked but no route found at point: $point")
            false
        }
        
        mapView.gestures.addOnMapClickListener(clickListener)
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
    onStyleReady: (PolylineAnnotationManager, PointAnnotationManager) -> Unit,
): MapView {
    return MapView(context).apply {
        mapboxMap.loadStyle("mapbox://styles/mapbox/outdoors-v12") {
            configureMapbox3d(style = it)
            configureUserLocationMarkerStyle(style = it)
            val polylineManager = this@apply.annotations.createPolylineAnnotationManager()
            val pointManager = this@apply.annotations.createPointAnnotationManager()
            onStyleReady(polylineManager, pointManager)
            mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(Point.fromLngLat(ANNECY_AUSSEDAT_LONGITUDE, ANNECY_AUSSEDAT_LATITUDE))
                    .zoom(14.8)
                    .pitch(74.0)
                    .bearing(30.0)
                    .build(),
            )
        }
    }
}

private fun configureUserLocationMarkerStyle(style: Style) {
    style.addImage(USER_MARKER_IMAGE_ID, createUserMarkerBitmap())
}

private fun createUserMarkerBitmap(): Bitmap {
    val sizePx = 152
    val center = sizePx / 2f
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val pinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#DB3B2B")
        style = Paint.Style.FILL
    }

    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#FFFFFF")
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#FFFFFF")
        style = Paint.Style.FILL
    }

    val headRadius = 18f
    val headCenterY = 30f
    val tipX = center
    val tipY = 78f

    val pinPath = Path().apply {
        addCircle(center, headCenterY, headRadius, Path.Direction.CW)
        moveTo(center - 10f, headCenterY + 12f)
        lineTo(center + 10f, headCenterY + 12f)
        lineTo(tipX, tipY)
        close()
    }

    canvas.drawPath(pinPath, pinPaint)
    canvas.drawPath(pinPath, borderPaint)
    canvas.drawCircle(center, headCenterY, 6.5f, corePaint)
    return bitmap
}

private fun configureMapbox3d(style: Style) {
    if (!style.styleSourceExists(TERRAIN_SOURCE_ID)) {
        style.addSource(
            rasterDemSource(TERRAIN_SOURCE_ID) {
                url("mapbox://mapbox.mapbox-terrain-dem-v1")
                tileSize(514L)
            },
        )
    }

    style.setTerrain(
        terrain(TERRAIN_SOURCE_ID) {
            exaggeration(1.7)
        },
    )

    if (!style.styleLayerExists(BUILDINGS_LAYER_ID)) {
        val buildingsLayer = fillExtrusionLayer(BUILDINGS_LAYER_ID, "composite") {
            sourceLayer("building")
            fillExtrusionColor("#C9B9A1")
            fillExtrusionOpacity(0.42)
            fillExtrusionHeight(get("height"))
            fillExtrusionBase(get("min_height"))
        }

        if (style.styleLayerExists("road-label")) {
            style.addLayerBelow(buildingsLayer, "road-label")
        } else {
            style.addLayer(buildingsLayer)
        }
    }
}

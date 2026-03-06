package com.example.kairn.ui.home

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kairn.domain.model.GpxRoute
import com.example.kairn.domain.model.HikeDifficulty
import android.util.Log
import com.example.kairn.ui.components.HikeBottomSheetContent
import com.example.kairn.ui.components.UserAvatar
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    // ── Location permission handling ──────────────────────────────────────
    var locationPermissionGranted by remember {
        mutableStateOf(viewModel.hasLocationPermission)
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        locationPermissionGranted = granted
        if (granted) viewModel.onPermissionGranted()
    }

    LaunchedEffect(Unit) {
        if (!locationPermissionGranted) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // ── UI ────────────────────────────────────────────────────────────────
    Box(modifier = modifier.fillMaxSize()) {

        // ── OSM Map fills entire screen ───────────────────────────────────
        OsmMapView(
            modifier = Modifier.fillMaxSize(),
            locationPermissionGranted = locationPermissionGranted,
            userLatitude = uiState.userLatitude,
            userLongitude = uiState.userLongitude,
            gpxRoutes = uiState.gpxRoutes,
        )

        // ── Liquid glass panel overlay ────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .liquidGlass(
                    cornerRadius = 40.dp,
                    backgroundColor = MaterialTheme.colorScheme.background.copy(alpha = 0.80f),
                    borderColor = Color.White.copy(alpha = 0.25f),
                    shadowColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f),
                    shadowRadius = 24.dp,
                )
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 20.dp),
        ) {
            HomeHeader(
                username = uiState.username,
                location = uiState.location,
                initials = uiState.initials,
            )
            Spacer(modifier = Modifier.size(14.dp))
            HomeSearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
            )
            Spacer(modifier = Modifier.size(12.dp))
            DifficultyChipsRow(
                selectedDifficulty = uiState.selectedDifficulty,
                onDifficultySelected = viewModel::onDifficultySelected,
            )
        }
    }

    if (uiState.isBottomSheetExpanded && uiState.selectedHike != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onBottomSheetDismissed() },
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        ) {
            HikeBottomSheetContent(
                hike = uiState.selectedHike!!,
                onStartTrip = { viewModel.onBottomSheetDismissed() },
            )
        }
    }
}

// ─── Liquid glass modifier ────────────────────────────────────────────────────

fun Modifier.liquidGlass(
    cornerRadius: Dp = 24.dp,
    backgroundColor: Color = Color.White.copy(alpha = 0.75f),
    borderColor: Color = Color.White.copy(alpha = 0.4f),
    shadowColor: Color = Color.Black.copy(alpha = 0.08f),
    shadowRadius: Dp = 16.dp,
): Modifier = this
    .drawBehind {
        // Soft drop shadow beneath the panel
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                asFrameworkPaint().apply {
                    isAntiAlias = true
                    color = android.graphics.Color.TRANSPARENT
                    setShadowLayer(
                        shadowRadius.toPx(),
                        0f,
                        shadowRadius.toPx() / 2f,
                        shadowColor.copy(alpha = 0.18f).toArgb(),
                    )
                }
            }
            val r = cornerRadius.toPx()
            canvas.drawRoundRect(
                left = 0f,
                top = 0f,
                right = size.width,
                bottom = size.height,
                radiusX = r,
                radiusY = r,
                paint = paint,
            )
        }
    }
    .clip(RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius))
    .background(backgroundColor)
    .border(
        width = 1.dp,
        color = borderColor,
        shape = RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius),
    )

// ─── Header ───────────────────────────────────────────────────────────────────

@Composable
private fun HomeHeader(
    username: String,
    location: String,
    initials: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hello, $username",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(13.dp),
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        UserAvatar(initials = initials, size = 44.dp)
    }
}

// ─── Search bar ───────────────────────────────────────────────────────────────

@Composable
private fun HomeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.40f))
            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = "Search",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onBackground,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            singleLine = true,
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text(
                        text = "Search a hike, location...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                inner()
            },
        )
    }
}

// ─── Difficulty chips ─────────────────────────────────────────────────────────

@Composable
private fun DifficultyChipsRow(
    selectedDifficulty: HikeDifficulty?,
    onDifficultySelected: (HikeDifficulty?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(0.dp),
    ) {
        items(HikeDifficulty.entries) { difficulty ->
            val isSelected = difficulty == selectedDifficulty
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.tertiaryContainer
                        else Color.White.copy(alpha = 0.35f),
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(24.dp),
                    )
                    .clickable { onDifficultySelected(if (isSelected) null else difficulty) }
                    .padding(horizontal = 18.dp, vertical = 8.dp),
            ) {
                Text(
                    text = difficulty.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onTertiaryContainer
                    else MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

// ─── OSMDroid Map ─────────────────────────────────────────────────────────────

@Composable
private fun OsmMapView(
    modifier: Modifier = Modifier,
    locationPermissionGranted: Boolean = false,
    userLatitude: Double? = null,
    userLongitude: Double? = null,
    gpxRoutes: List<GpxRoute> = emptyList(),
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    val mapView = remember { buildOsmMapView(context, locationPermissionGranted, gpxRoutes) }

    LaunchedEffect(gpxRoutes) {
        updateGpxOverlays(mapView, gpxRoutes)
    }

    // Center map on user's real position as soon as it becomes available
    LaunchedEffect(userLatitude, userLongitude) {
        if (userLatitude != null && userLongitude != null) {
            mapView.controller.animateTo(GeoPoint(userLatitude, userLongitude))
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

private const val TAG = "HomeScreen"

private fun updateGpxOverlays(mapView: MapView, gpxRoutes: List<GpxRoute>) {
    Log.d(TAG, "updateGpxOverlays: received ${gpxRoutes.size} routes")
    mapView.overlays.removeAll { it is org.osmdroid.views.overlay.Polyline }

    for (route in gpxRoutes) {
        Log.d(TAG, "updateGpxOverlays: adding route ${route.name} with ${route.points.size} points")
        if (route.points.size >= 2) {
            val polyline = org.osmdroid.views.overlay.Polyline().apply {
                setPoints(route.points)
                outlinePaint.color = android.graphics.Color.parseColor("#587b6c")
                outlinePaint.strokeWidth = 8f
            }
            mapView.overlays.add(polyline)
        }
    }

    mapView.invalidate()
}

private fun buildOsmMapView(
    context: Context,
    locationPermissionGranted: Boolean,
    gpxRoutes: List<GpxRoute> = emptyList(),
): MapView {
    Configuration.getInstance().userAgentValue = context.packageName
    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
        controller.setZoom(14.0)
        // Default center until GPS kicks in (centre de la France)
        controller.setCenter(GeoPoint(46.603354, 1.888334))

        if (locationPermissionGranted) {
            val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
            locationOverlay.enableMyLocation()
            overlays.add(locationOverlay)
        }

        for (route in gpxRoutes) {
            if (route.points.size >= 2) {
                val polyline = org.osmdroid.views.overlay.Polyline().apply {
                    setPoints(route.points)
                    outlinePaint.color = android.graphics.Color.parseColor("#587b6c")
                    outlinePaint.strokeWidth = 8f
                }
                overlays.add(polyline)
            }
        }
    }
}

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.kairn.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kairn.domain.model.GpxRoute
import com.example.kairn.domain.model.HikeDifficulty
import android.util.Log
import com.example.kairn.ui.util.localizedLabel
import com.example.kairn.ui.components.HikeBottomSheetContent
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
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
        // ── 3D map fills entire screen (provider strategy) ────────────────
        StrategyMapView(
            provider = MapProvider.MAPBOX,
            cameraState = MapCameraState(
                userLatitude = uiState.userLatitude,
                userLongitude = uiState.userLongitude,
                selectedCity = uiState.selectedCity,
                gpxRoutes = uiState.gpxRoutes,
                selectedGpxRoute = uiState.selectedGpxRoute,
            ),
            modifier = Modifier.fillMaxSize(),
            onGpxRouteClick = viewModel::onGpxRouteSelected,
        )

        // ── Liquid glass panel overlay ────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 20.dp),
        ) {
            HomeHeader(
                username = uiState.username,
                location = uiState.location,
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

            if (uiState.searchQuery.isNotBlank() && uiState.citySuggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.size(12.dp))
                SearchResultsPanel(
                    cities = uiState.citySuggestions,
                    onCityClick = viewModel::onCitySelected,
                )
            }
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

    if (uiState.isGpxBottomSheetExpanded && uiState.selectedGpxRoute != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onGpxBottomSheetDismissed() },
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        ) {
            GpxRouteBottomSheet(
                gpxRoute = uiState.selectedGpxRoute!!,
                onStartTrip = { /* TODO: Implement start feature */ },
                onEditInEditor = { /* TODO: Implement edit feature */ },
            )
        }
    }
}

@Composable
private fun SearchResultsPanel(
    cities: List<MapCity>,
    onCityClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.55f))
            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .heightIn(max = 220.dp)
            .padding(vertical = 6.dp),
    ) {
        if (cities.isEmpty()) {
            Text(
                text = stringResource(R.string.search_empty_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            )
            return
        }

        LazyColumn {
            items(cities) { city ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCityClick(city.name) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Text(
                        text = city.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = stringResource(R.string.show_hikes_around, city.name),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.45f))
            }
        }
    }
}

// ─── Header ───────────────────────────────────────────────────────────────────

@Composable
private fun HomeHeader(
    username: String,
    location: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.greeting, username),
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
            contentDescription = stringResource(R.string.search_cd),
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
                        text = stringResource(R.string.search_placeholder),
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
                    text = difficulty.localizedLabel(),
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
    var userLocationMarker by remember { mutableStateOf<Marker?>(null) }

    LaunchedEffect(gpxRoutes) {
        updateGpxOverlays(mapView, gpxRoutes)
    }

    // Center map on user's real position as soon as it becomes available
    LaunchedEffect(userLatitude, userLongitude) {
        if (userLatitude != null && userLongitude != null) {
            val userPoint = GeoPoint(userLatitude, userLongitude)
            mapView.controller.animateTo(userPoint)

            userLocationMarker?.let { existingMarker ->
                mapView.overlays.remove(existingMarker)
            }
            userLocationMarker = Marker(mapView).apply {
                position = userPoint
                title = "Tu es la"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            mapView.overlays.add(userLocationMarker)
            mapView.invalidate()
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
        controller.setCenter(GeoPoint(ANNECY_AUSSEDAT_LATITUDE, ANNECY_AUSSEDAT_LONGITUDE))

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

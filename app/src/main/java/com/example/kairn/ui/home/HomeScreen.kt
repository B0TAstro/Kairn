package com.example.kairn.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kairn.domain.model.Hike
import com.example.kairn.domain.model.HikeCategory
import com.example.kairn.ui.components.DifficultyBadge
import com.example.kairn.ui.components.HikeBottomSheetContent
import com.example.kairn.ui.components.UserAvatar
import com.example.kairn.ui.theme.Background
import com.example.kairn.ui.theme.CardBackground
import com.example.kairn.ui.theme.ChipBackground
import com.example.kairn.ui.theme.ChipSelectedBackground
import com.example.kairn.ui.theme.Primary
import com.example.kairn.ui.theme.TextPrimary
import com.example.kairn.ui.theme.TextSecondary
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    Column(modifier = modifier.fillMaxSize()) {
        // ── Top panel (Background color, rounded bottom corners) ──────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Background,
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                )
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 20.dp),
        ) {
            // Hello + avatar
            HomeHeader(
                username = uiState.username,
                location = uiState.location,
                initials = uiState.initials,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search bar
            HomeSearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Category chips
            CategoryChipsRow(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = viewModel::onCategorySelected,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Featured hike card
            uiState.nearbyHikes.firstOrNull()?.let { hike ->
                FeaturedHikeCard(
                    hike = hike,
                    onClick = { viewModel.onHikeSelected(hike) },
                )
            }
        }

        // ── MapBox fills remaining space ───────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            MapBoxView(modifier = Modifier.fillMaxSize())
        }
    }

    // Bottom sheet when a hike is selected
    if (uiState.isBottomSheetExpanded && uiState.selectedHike != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onBottomSheetDismissed() },
            sheetState = bottomSheetState,
            containerColor = CardBackground,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            HikeBottomSheetContent(
                hike = uiState.selectedHike!!,
                onStartTrip = { viewModel.onBottomSheetDismissed() },
            )
        }
    }
}

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
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Where would you like to go?",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 13.sp,
            )
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
            .clip(RoundedCornerShape(14.dp))
            .background(CardBackground)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = TextPrimary,
                fontSize = 14.sp,
            ),
            cursorBrush = SolidColor(Primary),
            singleLine = true,
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text(
                        text = "Chamonix, France",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 14.sp,
                    )
                }
                inner()
            },
        )
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = "Search",
            tint = TextSecondary,
            modifier = Modifier.size(18.dp),
        )
    }
}

// ─── Category chips ───────────────────────────────────────────────────────────

@Composable
private fun CategoryChipsRow(
    selectedCategory: HikeCategory?,
    onCategorySelected: (HikeCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 0.dp),
    ) {
        items(HikeCategory.entries) { category ->
            val isSelected = category == selectedCategory
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) ChipSelectedBackground else ChipBackground)
                    .clickable {
                        onCategorySelected(if (isSelected) null else category)
                    }
                    .padding(horizontal = 18.dp, vertical = 8.dp),
            ) {
                Text(
                    text = category.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) Color.White else TextSecondary,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

// ─── Featured hike card ───────────────────────────────────────────────────────

@Composable
private fun FeaturedHikeCard(
    hike: Hike,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Primary.copy(alpha = 0.25f),
                        Primary.copy(alpha = 0.7f),
                    ),
                ),
            )
            .clickable(onClick = onClick),
    ) {
        // Top-right arrow icon
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "↗", color = Color.White, fontSize = 14.sp)
        }

        // Bottom info
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp),
        ) {
            Text(
                text = hike.name,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
            )
            Text(
                text = "${hike.formattedElevation} meters",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                HikeStatChip(icon = "⏱", value = hike.formattedDuration, label = "Duration")
                HikeStatChip(icon = "↔", value = hike.formattedDistance, label = "Distance")
                HikeStatChip(icon = "⭐", value = hike.difficulty.label, label = "Level")
            }
        }
    }
}

@Composable
private fun HikeStatChip(
    icon: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(text = icon, fontSize = 13.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
            )
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 10.sp,
            )
        }
    }
}

// ─── MapBox ───────────────────────────────────────────────────────────────────

@Composable
private fun MapBoxView(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    val mapView = remember {
        MapView(context).apply {
            mapboxMap.loadStyle(Style.MAPBOX_STREETS)
            mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(com.mapbox.geojson.Point.fromLngLat(6.8694, 45.9237)) // Chamonix
                    .zoom(10.0)
                    .build(),
            )
        }
    }

    DisposableEffect(lifecycle) {
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
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
    )
}

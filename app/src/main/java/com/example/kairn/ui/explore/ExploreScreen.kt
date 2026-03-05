package com.example.kairn.ui.explore

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.kairn.domain.model.Hike
import com.example.kairn.domain.model.HikeCategory
import com.example.kairn.ui.theme.Background
import com.example.kairn.ui.theme.ChipSelectedBackground
import com.example.kairn.ui.theme.Primary
import com.example.kairn.ui.theme.TextPrimary
import com.example.kairn.ui.theme.TextSecondary
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ExploreScreen(
    onHikeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExploreViewModel = viewModel(),
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background),
    ) {
        // ── Header ────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 12.dp),
        ) {
            Text(
                text = "Explore",
                style = MaterialTheme.typography.displayLarge,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 36.sp,
            )
            Text(
                text = "${uiState.filteredHikes.size} hikes available",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontSize = 14.sp,
            )
        }

        // ── Category filter chips ─────────────────────────────────────────
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        ) {
            item {
                ExploreChip(
                    label = "All",
                    isSelected = uiState.selectedCategory == null,
                    onClick = { viewModel.onCategorySelected(null) },
                )
            }
            items(HikeCategory.entries) { category ->
                ExploreChip(
                    label = category.label,
                    isSelected = uiState.selectedCategory == category,
                    onClick = { viewModel.onCategorySelected(category) },
                )
            }
        }

        // ── Hike list ─────────────────────────────────────────────────────
        LazyColumn(
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                bottom = 100.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(uiState.filteredHikes, key = { it.id }) { hike ->
                ExploreHikeCard(
                    hike = hike,
                    onClick = {
                        viewModel.onHikeSelected(hike)
                        onHikeClick(hike.id)
                    },
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                )
            }
        }
    }
}

// ─── Chip ─────────────────────────────────────────────────────────────────────

@Composable
private fun ExploreChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = if (isSelected) ChipSelectedBackground else Color.White.copy(alpha = 0.0f)
    val borderColor = if (isSelected) Color.Transparent else TextSecondary.copy(alpha = 0.35f)
    val textColor = if (isSelected) Color.White else TextSecondary

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            fontSize = 13.sp,
        )
    }
}

// ─── Hike card ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ExploreHikeCard(
    hike: Hike,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val hazeState = remember { HazeState() }
    val cardShape = RoundedCornerShape(24.dp)

    with(sharedTransitionScope) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(440.dp)
                // sharedBounds sur le conteneur entier : interpole shape + position + taille
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "hike-card-${hike.id}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    clipInOverlayDuringTransition = OverlayClip(cardShape),
                )
                .clip(cardShape)
                .clickable(onClick = onClick),
        ) {
            // ── Background: image ou gradient ────────────────────────────
            if (hike.imageUrl != null) {
                AsyncImage(
                    model = hike.imageUrl,
                    contentDescription = hike.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .haze(hazeState),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color(0xFF111a16).copy(alpha = 0.25f),
                                    1.0f to Color(0xFF111a16).copy(alpha = 0.70f),
                                ),
                            ),
                        ),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .haze(hazeState)
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Primary.copy(alpha = 0.55f),
                                    0.5f to Primary.copy(alpha = 0.30f),
                                    1.0f to Color(0xFF1a2520),
                                ),
                            ),
                        ),
                )
            }

            // ── Arrow icon ────────────────────────────────────────────────
            Icon(
                imageVector = Icons.Filled.ArrowOutward,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(22.dp),
            )

            // ── Title + elevation ─────────────────────────────────────────
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 18.dp, top = 18.dp),
            ) {
                Text(
                    text = hike.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 26.sp,
                )
                Text(
                    text = hike.formattedElevation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.80f),
                    fontSize = 14.sp,
                )
            }

            // ── Liquid glass stats panel ──────────────────────────────────
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .hazeChild(
                        state = hazeState,
                        style = HazeStyle(
                            backgroundColor = Color(0xFF111a16).copy(alpha = 0.45f),
                            blurRadius = 20.dp,
                            tints = listOf(
                                HazeTint(color = Primary.copy(alpha = 0.18f)),
                                HazeTint(color = Color.White.copy(alpha = 0.06f)),
                            ),
                            noiseFactor = 0.04f,
                        ),
                    )
                    .border(
                        width = 0.5.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.25f),
                                Color.White.copy(alpha = 0.05f),
                            ),
                        ),
                        shape = RoundedCornerShape(16.dp),
                    )
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CardStatItem(Icons.Outlined.Schedule, hike.formattedDuration, "Duration")
                CardStatItem(Icons.Outlined.Route, hike.formattedDistance, "Distance")
                CardStatItem(Icons.Outlined.StarBorder, hike.difficulty.label, "Level")
            }
        }
    }
}

@Composable
private fun CardStatItem(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.75f),
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.60f),
                fontSize = 10.sp,
            )
        }
    }
}

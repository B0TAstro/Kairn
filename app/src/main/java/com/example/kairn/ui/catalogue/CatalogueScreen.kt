package com.example.kairn.ui.catalogue

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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.kairn.domain.model.Hike
import com.example.kairn.domain.model.HikeCategory
import com.example.kairn.ui.theme.Background
import com.example.kairn.ui.theme.ChipSelectedBackground
import com.example.kairn.ui.theme.KairnTheme
import com.example.kairn.ui.theme.Primary
import com.example.kairn.ui.theme.TextPrimary
import com.example.kairn.ui.theme.TextSecondary

@Composable
fun CatalogueScreen(
    onHikeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CatalogueViewModel = viewModel(),
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
            // "All" chip
            item {
                CatalogueChip(
                    label = "All",
                    isSelected = uiState.selectedCategory == null,
                    onClick = { viewModel.onCategorySelected(null) },
                )
            }
            items(HikeCategory.entries) { category ->
                CatalogueChip(
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
                bottom = 100.dp, // space for bottom nav
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(uiState.filteredHikes, key = { it.id }) { hike ->
                CatalogueHikeCard(
                    hike = hike,
                    onClick = { onHikeClick(hike.id) },
                )
            }
        }
    }
}

// ─── Catalogue chip ───────────────────────────────────────────────────────────

@Composable
private fun CatalogueChip(
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

// ─── Hike card (maquette style: large image + overlay text + stats row) ───────

@Composable
fun CatalogueHikeCard(
    hike: Hike,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
    ) {
        // Hike photo (or gradient fallback if no URL)
        if (hike.imageUrl != null) {
            AsyncImage(
                model = hike.imageUrl,
                contentDescription = hike.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
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

        // Dark overlay at bottom for readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xCC111a16)),
                    ),
                ),
        )

        // Top-right arrow icon
        Icon(
            imageVector = Icons.Filled.ArrowOutward,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(22.dp),
        )

        // Title + elevation (top-left)
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 18.dp, top = 18.dp),
        ) {
            Text(
                text = hike.name,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 26.sp,
            )
            Text(
                text = "${hike.elevationMeters} meters",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.80f),
                fontSize = 14.sp,
            )
        }

        // Stats row at the bottom
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CardStatItem(
                icon = Icons.Outlined.Schedule,
                value = hike.displayDuration,
                label = "Duration",
            )
            CardStatItem(
                icon = Icons.Outlined.Route,
                value = hike.formattedDistance,
                label = "Distance",
            )
            CardStatItem(
                icon = Icons.Outlined.StarBorder,
                value = hike.difficulty.label,
                label = "Level",
            )
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
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

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun CatalogueScreenPreview() {
    KairnTheme {
        CatalogueScreen(onHikeClick = {})
    }
}

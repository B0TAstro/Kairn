package com.example.kairn.ui.catalogue

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import coil.compose.AsyncImage
import com.example.kairn.domain.model.Hike
import com.example.kairn.ui.components.KairnButton
import com.example.kairn.ui.components.KairnTabRow
import com.example.kairn.ui.theme.KairnTheme
import com.example.kairn.ui.theme.Primary
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

// Valeur du chevauchement du panel sur l'image hero
private val PANEL_OVERLAP = 48.dp

@Composable
fun HikeDetailScreen(
    hike: Hike,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val hazeState = remember { HazeState() }

    Box(modifier = modifier.fillMaxSize()) {

        // ── Scrollable content ────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
        ) {
            // Hero image area — source du blur
            HeroImageArea(hike = hike, hazeState = hazeState)

            // Content panel — remonte de PANEL_OVERLAP sur la photo
            DetailPanel(
                hike = hike,
                hazeState = hazeState,
                modifier = Modifier.offset(y = -PANEL_OVERLAP),
            )
        }

        // ── Sticky top actions (back + bookmark) ──────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ActionButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                onClick = onBack,
            )
            ActionButton(
                icon = Icons.Outlined.BookmarkBorder,
                contentDescription = "Save",
                onClick = {},
            )
        }
    }
}

// ─── Hero image ───────────────────────────────────────────────────────────────

@Composable
private fun HeroImageArea(
    hike: Hike,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(420.dp),
    ) {
        // Hike photo — source du blur haze
        if (hike.imageUrl != null) {
            AsyncImage(
                model = hike.imageUrl,
                contentDescription = hike.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .haze(hazeState),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .haze(hazeState)
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Primary.copy(alpha = 0.45f),
                                0.4f to Primary.copy(alpha = 0.25f),
                                1.0f to Color(0xFF111a16),
                            ),
                        ),
                    ),
            )
        }

        // Fondu bas léger pour adoucir la transition vers le panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0x66111a16)),
                    ),
                ),
        )
    }
}

// ─── Detail content panel (liquid glass) ─────────────────────────────────────

@Composable
private fun DetailPanel(
    hike: Hike,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val panelShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(panelShape)
            // Liquid glass : blur de la photo qui se trouve derrière
            .hazeChild(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = Color(0xFF0e1712).copy(alpha = 0.72f),
                    blurRadius = 28.dp,
                    tints = listOf(
                        HazeTint(color = Primary.copy(alpha = 0.22f)),
                        HazeTint(color = Color.White.copy(alpha = 0.04f)),
                    ),
                    noiseFactor = 0.03f,
                ),
            )
            // Bordure top frosted
            .border(
                width = 0.8.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.22f),
                        Color.White.copy(alpha = 0.04f),
                    ),
                ),
                shape = panelShape,
            )
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 120.dp), // bottom space for FAB
    ) {
        // Drag handle
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.28f))
                .align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Name + elevation
        Text(
            text = hike.name,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${hike.elevationMeters} meters",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 15.sp,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            DetailStatItem(
                icon = Icons.Outlined.Schedule,
                value = hike.displayDuration,
                label = "Duration",
                modifier = Modifier.weight(1f),
            )
            DetailStatItem(
                icon = Icons.Outlined.Route,
                value = hike.formattedDistance,
                label = "Distance",
                modifier = Modifier.weight(1f),
            )
            DetailStatItem(
                icon = Icons.Outlined.StarBorder,
                value = hike.difficulty.label,
                label = "Level",
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Tabs
        KairnTabRow(
            tabs = listOf("Details", "Rout List", "Reviews"),
            selectedIndex = selectedTab,
            onTabSelected = { selectedTab = it },
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tab content
        when (selectedTab) {
            0 -> DetailsTabContent(hike = hike)
            1 -> PlaceholderTabContent(text = "Route waypoints coming soon.")
            2 -> PlaceholderTabContent(text = "Reviews coming soon.")
        }
    }
}

// ─── Tab content ──────────────────────────────────────────────────────────────

@Composable
private fun DetailsTabContent(
    hike: Hike,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Hiking to ${hike.name}",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Panoramic views of Mont Blanc and the Alps",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 13.sp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = hike.description.ifEmpty {
                "Experience one of the most breathtaking adventures as you hike toward the iconic summit. This trail offers unparalleled views of the surrounding peaks, glaciers, and majestic scenery that will leave you speechless."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.70f),
            fontSize = 14.sp,
            lineHeight = 22.sp,
        )
    }
}

@Composable
private fun PlaceholderTabContent(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White.copy(alpha = 0.50f),
        modifier = modifier,
    )
}

// ─── Stat item ────────────────────────────────────────────────────────────────

@Composable
private fun DetailStatItem(
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
            tint = Primary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.50f),
                fontSize = 11.sp,
            )
        }
    }
}

// ─── Round action button (back / bookmark) ────────────────────────────────────

@Composable
private fun ActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(0xFF1e2d27).copy(alpha = 0.75f))
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.18f),
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(20.dp),
        )
    }
}

// ─── Floating CTA (overlaid at bottom) ───────────────────────────────────────

@Composable
fun HikeDetailCta(
    onStartTrip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color(0xE6111a16)),
                ),
            )
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        KairnButton(
            text = "Start your trip",
            onClick = onStartTrip,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// ─── Composed screen with CTA overlay ────────────────────────────────────────

@Composable
fun HikeDetailScreenWithCta(
    hike: Hike,
    onBack: () -> Unit,
    onStartTrip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        HikeDetailScreen(hike = hike, onBack = onBack)

        HikeDetailCta(
            onStartTrip = onStartTrip,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun HikeDetailScreenPreview() {
    KairnTheme {
        HikeDetailScreenWithCta(
            hike = Hike.preview,
            onBack = {},
            onStartTrip = {},
        )
    }
}

package com.example.kairn.ui.explore

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kairn.domain.model.Hike
import com.example.kairn.ui.components.KairnButton
import com.example.kairn.ui.components.KairnTabRow
import com.example.kairn.ui.theme.Background
import com.example.kairn.ui.theme.Primary
import com.example.kairn.ui.theme.TextPrimary
import com.example.kairn.ui.theme.TextSecondary

private val PANEL_OVERLAP = 48.dp

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HikeDetailScreen(
    hike: Hike,
    onBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    with(sharedTransitionScope) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Background)
                // sharedBounds sur le conteneur entier — même clé que la carte
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "hike-card-${hike.id}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    clipInOverlayDuringTransition = OverlayClip(RectangleShape),
                ),
        ) {
            // ── Scrollable content ────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
            ) {
                HeroImageArea(hike = hike)

                DetailPanel(
                    hike = hike,
                    animatedVisibilityScope = animatedVisibilityScope,
                    modifier = Modifier.offset(y = -PANEL_OVERLAP),
                )
            }

            // ── Sticky top actions ────────────────────────────────────────
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
}

// ─── Hero image ───────────────────────────────────────────────────────────────

@Composable
private fun HeroImageArea(
    hike: Hike,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(420.dp),
    ) {
        if (hike.imageUrl != null) {
            AsyncImage(
                model = hike.imageUrl,
                contentDescription = hike.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color(0xFF111a16).copy(alpha = 0.15f),
                                0.7f to Color(0xFF111a16).copy(alpha = 0.10f),
                                1.0f to Color(0xFF111a16).copy(alpha = 0.55f),
                            ),
                        ),
                    ),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
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
    }
}

// ─── Detail content panel ─────────────────────────────────────────────────────

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun DetailPanel(
    hike: Hike,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val panelShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(panelShape)
            .background(Background)
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 120.dp)
            .then(
                with(animatedVisibilityScope) {
                    Modifier.animateEnterExit(
                        enter = fadeIn(animationSpec = tween(durationMillis = 200)),
                    )
                }
            ),
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(TextSecondary.copy(alpha = 0.25f))
                .align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = hike.title,
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = hike.formattedElevation,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            fontSize = 15.sp,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            DetailStatItem(Icons.Outlined.Schedule, hike.formattedDuration, "Duration", Modifier.weight(1f))
            DetailStatItem(Icons.Outlined.Route, hike.formattedDistance, "Distance", Modifier.weight(1f))
            DetailStatItem(Icons.Outlined.StarBorder, hike.difficulty.label, "Level", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(28.dp))

        KairnTabRow(
            tabs = listOf("Details", "Route List", "Reviews"),
            selectedIndex = selectedTab,
            onTabSelected = { selectedTab = it },
        )

        Spacer(modifier = Modifier.height(24.dp))

        when (selectedTab) {
            0 -> DetailsTabContent(hike = hike)
            1 -> PlaceholderTabContent(text = "Route waypoints coming soon.")
            2 -> PlaceholderTabContent(text = "Reviews coming soon.")
        }
    }
}

// ─── Tab content ──────────────────────────────────────────────────────────────

@Composable
private fun DetailsTabContent(hike: Hike, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = "Hiking to ${hike.title}",
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = hike.description.orEmpty().ifEmpty {
                "Experience one of the most breathtaking adventures as you hike toward the iconic summit. This trail offers unparalleled views of the surrounding peaks, glaciers, and majestic scenery that will leave you speechless."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary.copy(alpha = 0.75f),
            fontSize = 14.sp,
            lineHeight = 22.sp,
        )
    }
}

@Composable
private fun PlaceholderTabContent(text: String, modifier: Modifier = Modifier) {
    Text(text = text, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = modifier)
}

// ─── Stat item ────────────────────────────────────────────────────────────────

@Composable
private fun DetailStatItem(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Icon(imageVector = icon, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = value, style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 11.sp)
        }
    }
}

// ─── Round action button ──────────────────────────────────────────────────────

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
            .border(0.5.dp, Color.White.copy(alpha = 0.18f), CircleShape)
            .clickable(onClick = onClick),
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription, tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

// ─── Floating CTA ─────────────────────────────────────────────────────────────

@Composable
fun HikeDetailCta(
    onStartTrip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Background)
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .padding(bottom = 20.dp),
    ) {
        KairnButton(text = "Start your trip", onClick = onStartTrip, modifier = Modifier.fillMaxWidth())
    }
}

// ─── Composed screen with CTA overlay ────────────────────────────────────────

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HikeDetailScreenWithCta(
    hike: Hike,
    onBack: () -> Unit,
    onStartTrip: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        HikeDetailScreen(
            hike = hike,
            onBack = onBack,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )
        HikeDetailCta(
            onStartTrip = onStartTrip,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

package com.example.kairn.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kairn.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kairn.domain.model.Hike
import com.example.kairn.domain.model.LeaderboardEntry
import com.example.kairn.domain.model.User
import com.example.kairn.ui.components.KairnTabRow
import com.example.kairn.ui.util.localizedLabel
import com.example.kairn.ui.theme.KairnTheme
import com.example.kairn.ui.theme.OverlayDark

@Composable
fun AccountScreen(
    onSignOut: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToHikeDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val completedHikes by viewModel.completedHikes.collectAsStateWithLifecycle()
    val leaderboardState by viewModel.leaderboardState.collectAsStateWithLifecycle()

    AccountContent(
        user = currentUser,
        completedHikes = completedHikes,
        longestTrailKm = viewModel.longestTrailKm(),
        leaderboardState = leaderboardState,
        onLeaderboardScopeChange = viewModel::onLeaderboardScopeChange,
        onSignOut = {
            viewModel.signOut()
            onSignOut()
        },
        onEditProfile = onNavigateToEditProfile,
        onHikeClick = onNavigateToHikeDetail,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountContent(
    user: User?,
    completedHikes: List<Hike>,
    longestTrailKm: Double,
    leaderboardState: LeaderboardUiState,
    onLeaderboardScopeChange: (LeaderboardScope) -> Unit,
    onSignOut: () -> Unit,
    onEditProfile: () -> Unit,
    onHikeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAllTrailsSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        if (user != null) {
            // --- Avatar with white border ---
            ProfileAvatar(
                initials = AccountViewModel.getInitials(user),
                avatarUrl = user.avatarUrl,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // --- Name ---
            Text(
                text = user.pseudo ?: "${user.firstName.orEmpty()} ${user.lastName.orEmpty()}".trim()
                    .ifEmpty { stringResource(R.string.anonymous_user) },
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            // --- Bio ---
            val bio = user.bio
            if (!bio.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // --- Stats row: Member Since / Trails Completed / Longest Trail ---
            StatsRow(user = user, longestTrailKm = longestTrailKm)

            Spacer(modifier = Modifier.height(28.dp))

            // --- Leaderboard ---
            LeaderboardSection(
                state = leaderboardState,
                onScopeChange = onLeaderboardScopeChange,
            )

            Spacer(modifier = Modifier.height(28.dp))

            // --- Completed Hikes ---
            if (completedHikes.isNotEmpty()) {
                CompletedHikesSection(
                    hikes = completedHikes,
                    onHikeClick = onHikeClick,
                    onViewAllClick = { showAllTrailsSheet = true },
                )
                Spacer(modifier = Modifier.height(28.dp))
            }

            // --- Settings menu ---
            SettingsSection(onEditProfile = onEditProfile)

            Spacer(modifier = Modifier.height(32.dp))

            // --- Sign out ---
            SignOutButton(onClick = onSignOut)
        } else {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.not_connected),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // --- "View All Trails" bottom sheet ---
    if (showAllTrailsSheet && completedHikes.isNotEmpty()) {
        AllTrailsBottomSheet(
            hikes = completedHikes,
            onHikeClick = { hikeId ->
                showAllTrailsSheet = false
                onHikeClick(hikeId)
            },
            onDismiss = { showAllTrailsSheet = false },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Avatar with white border
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileAvatar(
    initials: String,
    avatarUrl: String?,
    modifier: Modifier = Modifier,
) {
    val borderWidth = 3.dp

    if (!avatarUrl.isNullOrBlank()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(avatarUrl)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.cd_avatar),
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(120.dp)
                .border(borderWidth, Color.White, CircleShape)
                .padding(borderWidth)
                .clip(CircleShape),
        )
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .size(120.dp)
                .border(borderWidth, Color.White, CircleShape)
                .padding(borderWidth)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
        ) {
            Text(
                text = initials,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 40.sp,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.displaySmall,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Stats Row — Member Since / Trails Completed / Longest Trail
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatsRow(
    user: User,
    longestTrailKm: Double,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 20.dp)
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatItem(
            value = AccountViewModel.formatMemberSince(user.createdAt),
            label = stringResource(R.string.stat_member_since),
            modifier = Modifier.weight(1f),
        )

        VerticalStatDivider()

        StatItem(
            value = "${user.hikesCompleted}",
            label = stringResource(R.string.stat_hikes_done),
            modifier = Modifier.weight(1f),
        )

        VerticalStatDivider()

        StatItem(
            value = if (longestTrailKm > 0) "${"%.1f".format(longestTrailKm)} KM" else "—",
            label = stringResource(R.string.stat_best_hike),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun VerticalStatDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Completed Hikes Section
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CompletedHikesSection(
    hikes: List<Hike>,
    onHikeClick: (String) -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.completed_hikes_header),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (hikes.size > 3) {
                Text(
                    text = stringResource(R.string.view_all_trails),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onViewAllClick),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Show up to 3 hike cards
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            hikes.take(3).forEach { hike ->
                CompletedHikeCard(
                    hike = hike,
                    onClick = { onHikeClick(hike.id) },
                )
            }
        }
    }
}

@Composable
private fun CompletedHikeCard(
    hike: Hike,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(cardShape)
            .clickable(onClick = onClick),
    ) {
        // Background image or gradient
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
                                0.0f to OverlayDark.copy(alpha = 0.15f),
                                0.5f to OverlayDark.copy(alpha = 0.30f),
                                1.0f to OverlayDark.copy(alpha = 0.75f),
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
                                0.0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                                0.5f to MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                                1.0f to OverlayDark.copy(alpha = 0.75f),
                            ),
                        ),
                    ),
            )
        }

        // Title + stats overlay at bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp),
        ) {
            Text(
                text = hike.title,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CompletedHikeStatChip(
                    icon = Icons.Outlined.Route,
                    text = hike.formattedDistance,
                )
                CompletedHikeStatChip(
                    icon = Icons.Outlined.Schedule,
                    text = hike.formattedDuration,
                )
                CompletedHikeStatChip(
                    icon = Icons.Outlined.StarBorder,
                    text = hike.difficulty.localizedLabel(),
                )
            }
        }
    }
}

@Composable
private fun CompletedHikeStatChip(
    icon: ImageVector,
    text: String,
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
            modifier = Modifier.size(14.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.85f),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// "View All Trails" Bottom Sheet
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllTrailsBottomSheet(
    hikes: List<Hike>,
    onHikeClick: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.all_trails_sheet_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.all_trails_sheet_subtitle, hikes.size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 24.dp,
                end = 24.dp,
                bottom = 40.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(hikes, key = { it.id }) { hike ->
                CompletedHikeCard(
                    hike = hike,
                    onClick = { onHikeClick(hike.id) },
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Leaderboard Section
// ─────────────────────────────────────────────────────────────────────────────

// leaderboard tabs are now composed dynamically via stringResource

@Composable
private fun LeaderboardSection(
    state: LeaderboardUiState,
    onScopeChange: (LeaderboardScope) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.leaderboard_header),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Icon(
                imageVector = Icons.Outlined.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tab row
        val leaderboardTabs = listOf(
            stringResource(R.string.leaderboard_tab_regional),
            stringResource(R.string.leaderboard_tab_national),
            stringResource(R.string.leaderboard_tab_global),
        )
        KairnTabRow(
            tabs = leaderboardTabs,
            selectedIndex = state.selectedScope.ordinal,
            onTabSelected = { index ->
                onScopeChange(LeaderboardScope.entries[index])
            },
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 12.dp),
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp,
                        )
                    }
                }

                // No geo data for regional/national
                !state.hasGeoData && state.selectedScope != LeaderboardScope.GLOBAL -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                                text = stringResource(R.string.leaderboard_no_geo),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                else -> {
                    val entries = when (state.selectedScope) {
                        LeaderboardScope.REGIONAL -> state.regionalEntries
                        LeaderboardScope.NATIONAL -> state.nationalEntries
                        LeaderboardScope.GLOBAL -> state.globalEntries
                    }

                    if (entries.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.leaderboard_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    } else {
                        val windowed = AccountViewModel.windowedLeaderboard(entries)
                        windowed.forEachIndexed { index, entry ->
                            LeaderboardEntryRow(entry = entry)
                            if (index < windowed.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardEntryRow(
    entry: LeaderboardEntry,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (entry.isCurrentUser) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    } else {
        Color.Transparent
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Rank
        Text(
            text = "${entry.rank}.",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = if (entry.isCurrentUser) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onBackground
            },
            modifier = Modifier.width(32.dp),
        )

        // Avatar
        if (!entry.avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(entry.avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
            )
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            ) {
                Text(
                    text = entry.username.take(2).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Name + stats
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.username,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (entry.isCurrentUser) FontWeight.Bold else FontWeight.Medium,
                ),
                color = if (entry.isCurrentUser) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onBackground
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(R.string.leaderboard_entry_subtitle, entry.level, entry.xp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Settings Section
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsSection(
    onEditProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface),
    ) {
        SettingsItem(
            icon = Icons.Outlined.Person,
            label = stringResource(R.string.settings_edit_profile),
            onClick = onEditProfile,
        )
        SettingsDivider()
        SettingsItem(
            icon = Icons.Outlined.Notifications,
            label = stringResource(R.string.settings_notifications),
            onClick = { /* TODO */ },
        )
        SettingsDivider()
        SettingsItem(
            icon = Icons.Outlined.Lock,
            label = stringResource(R.string.settings_privacy),
            onClick = { /* TODO */ },
        )
        SettingsDivider()
        SettingsItem(
            icon = Icons.Outlined.Info,
            label = stringResource(R.string.settings_about),
            onClick = { /* TODO */ },
        )
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun SettingsDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(horizontal = 20.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Sign Out
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SignOutButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.sign_out),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.error,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun AccountScreenPreview() {
    KairnTheme {
        AccountContent(
            user = User.preview,
            completedHikes = Hike.previewList.take(4),
            longestTrailKm = 21.4,
            leaderboardState = LeaderboardUiState(
                selectedScope = LeaderboardScope.REGIONAL,
                regionalEntries = LeaderboardEntry.previewList,
                nationalEntries = LeaderboardEntry.previewList,
                globalEntries = LeaderboardEntry.previewList,
                hasGeoData = true,
            ),
            onLeaderboardScopeChange = {},
            onSignOut = {},
            onEditProfile = {},
            onHikeClick = {},
        )
    }
}

package com.example.kairn.ui.editor

import android.content.Context
import android.location.Geocoder
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kairn.BuildConfig
import com.example.kairn.R
import com.example.kairn.ui.editor.components.EditorMap
import com.example.kairn.ui.editor.components.PointsListOverlay
import com.example.kairn.ui.editor.map.MapProvider
import com.example.kairn.ui.editor.map.MapboxMapProvider
import com.example.kairn.ui.editor.map.OsmMapProvider
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

@Composable
fun EditorScreen(
    viewModel: EditorViewModel = hiltViewModel(),
) {
    val bottomOverlayOffset = 96.dp
    var isPointsPanelExpanded by rememberSaveable { mutableStateOf(true) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pointsCount = (uiState as? EditorUiState.Ready)?.points?.size ?: 0
    val context = LocalContext.current
    val mapProvider: MapProvider = remember {
        if (BuildConfig.MAPBOX_ACCESS_TOKEN.isBlank()) {
            OsmMapProvider()
        } else {
            MapboxMapProvider()
        }
    }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var searchErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState) {
        val readyState = uiState as? EditorUiState.Ready ?: return@LaunchedEffect
        when {
            readyState.saveSuccess -> {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.editor_save_success),
                )
                viewModel.clearSaveStatus()
            }
            readyState.saveError != null -> {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.editor_save_error, readyState.saveError),
                )
                viewModel.clearSaveStatus()
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            EditorMap(
                viewModel = viewModel,
                mapProvider = mapProvider,
                modifier = Modifier.fillMaxSize(),
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 10.dp)
                    .padding(horizontal = 16.dp),
            ) {
                EditorMapSearchBar(
                    query = searchQuery,
                    onQueryChange = {
                        searchQuery = it
                        searchErrorMessage = null
                    },
                    onSearch = {
                        val trimmedQuery = searchQuery.trim()
                        if (trimmedQuery.isBlank()) {
                            return@EditorMapSearchBar
                        }

                        scope.launch {
                            val result = searchLocation(context, trimmedQuery)
                            if (result != null) {
                                searchErrorMessage = null
                                mapProvider.centerMap(result.first, result.second, zoom = 13.8)
                            } else {
                                searchErrorMessage = context.getString(
                                    R.string.editor_search_no_results,
                                    trimmedQuery,
                                )
                            }
                        }
                    },
                )

                if (searchErrorMessage != null) {
                    Text(
                        text = searchErrorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 6.dp, start = 4.dp),
                    )
                }

                if (pointsCount == 0) {
                    EditorEmptyStateCard(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .fillMaxWidth(),
                    )
                }
            }

            if (isPointsPanelExpanded && pointsCount > 0) {
                PointsListOverlay(
                    viewModel = viewModel,
                    onCollapse = { isPointsPanelExpanded = false },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = bottomOverlayOffset + 76.dp),
                )
            }

            FilledTonalIconButton(
                onClick = { isPointsPanelExpanded = !isPointsPanelExpanded },
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 10.dp, start = 16.dp)
                    .align(Alignment.TopStart)
                    .size(44.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = stringResource(R.string.cd_show_points),
                )
            }

            AssistChip(
                onClick = { isPointsPanelExpanded = !isPointsPanelExpanded },
                label = {
                    Text(stringResource(R.string.editor_points_count_chip, pointsCount))
                },
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 10.dp, start = 68.dp)
                    .align(Alignment.TopStart),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                ),
            )

            val readyState = uiState as? EditorUiState.Ready
            val isSaving = readyState?.isSaving == true
            val canSave = readyState != null && readyState.points.isNotEmpty() && !isSaving

            ExtendedFloatingActionButton(
                onClick = { if (canSave) viewModel.saveToSupabase() },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = bottomOverlayOffset)
                    .align(Alignment.BottomEnd),
                icon = {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = stringResource(R.string.cd_save_gpx),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
                text = {
                    Text(
                        text = if (isSaving) {
                            stringResource(R.string.editor_saving)
                        } else {
                            stringResource(R.string.editor_save_gpx)
                        },
                    )
                },
            )

            if (pointsCount == 0) {
                AssistChip(
                    onClick = { },
                    label = { Text(stringResource(R.string.editor_tap_map_hint)) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = bottomOverlayOffset + 12.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    ),
                )
            }
        }
    }
}

@Composable
private fun EditorMapSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.93f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                placeholder = {
                    Text(text = stringResource(R.string.editor_search_placeholder))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            )
            IconButton(
                onClick = onSearch,
                modifier = Modifier
                    .padding(start = 6.dp)
                    .width(44.dp)
                    .height(44.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.editor_search_cd),
                )
            }
        }
    }
}

@Composable
private fun EditorEmptyStateCard(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                text = stringResource(R.string.editor_empty_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = stringResource(R.string.editor_empty_hint),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

private suspend fun searchLocation(context: Context, query: String): Pair<Double, Double>? {
    return withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            val geocoder = Geocoder(context, Locale.FRANCE)
            geocoder.getFromLocationName(query, 1, object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<android.location.Address>) {
                    if (continuation.isActive) {
                        continuation.resume(addresses.firstOrNull()?.let { it.latitude to it.longitude })
                    }
                }

                override fun onError(errorMessage: String?) {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
            })
        }
    }
}

package com.example.kairn.ui.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kairn.ui.components.CompassWidget
import com.example.kairn.ui.components.EmergencyAlertButton
import com.example.kairn.ui.editor.components.EditorMap
import com.example.kairn.ui.editor.components.PointsListOverlay
import com.example.kairn.ui.editor.map.MapProvider
import com.example.kairn.ui.editor.map.OsmMapProvider

@Composable
fun EditorScreen(
    viewModel: EditorViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val mapProvider: MapProvider = remember { OsmMapProvider() }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        val readyState = uiState as? EditorUiState.Ready ?: return@LaunchedEffect
        when {
            readyState.saveSuccess -> {
                snackbarHostState.showSnackbar("GPX saved successfully!")
                viewModel.clearSaveStatus()
            }
            readyState.saveError != null -> {
                snackbarHostState.showSnackbar("Failed to save GPX: ${readyState.saveError}")
                viewModel.clearSaveStatus()
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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

            PointsListOverlay(
                viewModel = viewModel,
                modifier = Modifier.padding(16.dp),
            )

            CompassWidget(
                degrees = 0f,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopEnd),
            )

            EmergencyAlertButton(
                onClick = {},
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomStart),
            )

            val readyState = uiState as? EditorUiState.Ready
            val isSaving = readyState?.isSaving == true
            val canSave = readyState != null && readyState.points.isNotEmpty() && !isSaving

            ExtendedFloatingActionButton(
                onClick = { if (canSave) viewModel.saveToSupabase() },
                modifier = Modifier
                    .padding(16.dp)
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
                            contentDescription = "Save GPX to Supabase",
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
                text = {
                    Text(text = if (isSaving) "Saving..." else "Save GPX")
                },
            )
        }
    }
}

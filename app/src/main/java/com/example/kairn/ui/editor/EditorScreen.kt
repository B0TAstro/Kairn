package com.example.kairn.ui.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kairn.ui.components.CompassWidget
import com.example.kairn.ui.components.EmergencyAlertButton
import com.example.kairn.ui.editor.map.MapProvider
import com.example.kairn.ui.editor.map.OsmMapProvider
import com.example.kairn.ui.editor.components.EditorMap
import com.example.kairn.ui.editor.components.PointsListOverlay
import kotlinx.coroutines.launch

@Composable
fun EditorScreen(
    viewModel: EditorViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val mapProvider: MapProvider = remember { OsmMapProvider() }
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(uiState) {
        if (uiState is com.example.kairn.ui.editor.EditorUiState.Ready) {
            val readyState = uiState as com.example.kairn.ui.editor.EditorUiState.Ready
            if (readyState.saveSuccess) {
                snackbarHostState.showSnackbar("GPX saved successfully!")
                viewModel.clearSaveStatus()
            } else if (readyState.saveError != null) {
                snackbarHostState.showSnackbar("Failed to save GPX: ${readyState.saveError}")
                viewModel.clearSaveStatus()
            }
        }
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopEnd),
            )
            
            EmergencyAlertButton(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomStart),
            )
            
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.saveToSupabase()
                },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd),
                enabled = uiState is com.example.kairn.ui.editor.EditorUiState.Ready && 
                         (uiState as? com.example.kairn.ui.editor.EditorUiState.Ready)?.points?.isNotEmpty() == true &&
                         (uiState as? com.example.kairn.ui.editor.EditorUiState.Ready)?.isSaving != true,
            ) {
                if ((uiState as? com.example.kairn.ui.editor.EditorUiState.Ready)?.isSaving == true) {
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
                Text(
                    text = if ((uiState as? com.example.kairn.ui.editor.EditorUiState.Ready)?.isSaving == true) 
                        "Saving..." else "Save GPX",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}
package com.example.kairn.ui.editor

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import com.example.kairn.ui.editor.gpx.GpxExporter
import com.example.kairn.ui.editor.map.MapProvider
import com.example.kairn.ui.editor.map.OsmMapProvider
import com.example.kairn.ui.editor.components.EditorMap
import com.example.kairn.ui.editor.components.PointsListOverlay

@Composable
fun EditorScreen(
    viewModel: EditorViewModel = viewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val mapProvider: MapProvider = remember { OsmMapProvider() }
    val gpxExporter = remember { GpxExporter(context) }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
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
                    if (uiState is com.example.kairn.ui.editor.EditorUiState.Ready) {
                        val readyState = uiState as com.example.kairn.ui.editor.EditorUiState.Ready
                        val points = readyState.points
                        val routes = readyState.routes.map { it.points }
                        if (points.isNotEmpty()) {
                            val gpxUri = gpxExporter.exportToFile(points, routes)
                            gpxUri?.let { uri ->
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/gpx+xml"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(Intent.EXTRA_SUBJECT, "Kairn Route Export")
                                    putExtra(Intent.EXTRA_TEXT, "Route created with Kairn hiking app")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Export GPX"))
                            }
                        }
                    }
                },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd),
                enabled = uiState is com.example.kairn.ui.editor.EditorUiState.Ready && 
                         (uiState as? com.example.kairn.ui.editor.EditorUiState.Ready)?.points?.isNotEmpty() == true,
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Export GPX",
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = "Export GPX",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}
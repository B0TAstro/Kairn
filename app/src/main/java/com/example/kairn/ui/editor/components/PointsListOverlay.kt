package com.example.kairn.ui.editor.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairn.ui.editor.EditorViewModel
import com.example.kairn.ui.editor.model.EditorPoint
import com.example.kairn.ui.theme.Accent
import com.example.kairn.ui.theme.Background
import com.example.kairn.ui.theme.Primary
import com.example.kairn.ui.theme.TextPrimary
import kotlinx.coroutines.flow.StateFlow

@Composable
fun PointsListOverlay(
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val points by remember {
        derivedStateOf {
            (uiState as? com.example.kairn.ui.editor.EditorUiState.Ready)?.points ?: emptyList()
        }
    }

    if (points.isEmpty()) return

    Card(
        modifier = modifier
            .width(280.dp)
            .fillMaxHeight(0.7f)
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Background.copy(alpha = 0.95f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Points (${points.size})",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            DragDropReorderList(
                items = points,
                key = { it.id },
                onReorder = { newOrder ->
                    viewModel.reorderPoints(newOrder)
                }
            ) { point, isDragging, onDragStart ->
                PointListItem(
                    point = point,
                    isSelected = point.id == (uiState as? com.example.kairn.ui.editor.EditorUiState.Ready)?.selectedPointId,
                    isDragging = isDragging,
                    onDragStart = onDragStart,
                    onRemove = { viewModel.removePoint(point.id) },
                    onSelect = { viewModel.selectPoint(point.id) },
                    modifier = Modifier.animateContentSize(),
                )
            }

            if (points.size >= 2) {
                Text(
                    text = "Long press and drag handles to reorder points",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun PointListItem(
    point: EditorPoint,
    isSelected: Boolean,
    isDragging: Boolean,
    onDragStart: () -> Unit,
    onRemove: () -> Unit,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
        Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 2.dp,
                color = when {
                    isDragging -> Accent.copy(alpha = 0.8f)
                    isSelected -> Accent
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(8.dp),
            )
            .background(
                color = when {
                    isDragging -> Accent.copy(alpha = 0.15f)
                    isSelected -> Accent.copy(alpha = 0.1f)
                    else -> Primary.copy(alpha = 0.05f)
                },
                shape = RoundedCornerShape(8.dp),
            )
            .clickable { onSelect() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Accent.copy(alpha = if (isSelected) 0.9f else 0.6f))
                    .border(1.dp, Accent.copy(alpha = 0.8f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${point.order + 1}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            Column {
                Text(
                    text = point.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${"%.6f".format(point.latitude)}, ${"%.6f".format(point.longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary.copy(alpha = 0.6f),
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onDragStart,
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag to reorder",
                    tint = TextPrimary.copy(alpha = 0.5f),
                )
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove point",
                    tint = Color.Red.copy(alpha = 0.8f),
                )
            }
        }
    }
}


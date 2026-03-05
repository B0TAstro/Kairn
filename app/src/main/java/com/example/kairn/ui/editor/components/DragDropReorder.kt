package com.example.kairn.ui.editor.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun <T> DragDropReorderList(
    items: List<T>,
    key: (T) -> Any,
    onReorder: (List<T>) -> Unit,
    content: @Composable (T, Boolean, () -> Unit) -> Unit,
) {
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(items) {
        if (draggedItemIndex != null) {
            draggedItemIndex = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        items.forEachIndexed { index, item ->
            val isDragging = index == draggedItemIndex

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(key(item)) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { offset ->
                                draggedItemIndex = index
                                dragOffset = offset
                            },
                            onDrag = { _, dragAmount ->
                                dragOffset += dragAmount
                                val targetIndex = calculateTargetIndex(
                                    items = items,
                                    draggedIndex = index,
                                    dragOffset = dragOffset,
                                    itemHeight = 80f,
                                )
                                if (targetIndex != null && targetIndex != index) {
                                    val newList = items.toMutableList().apply {
                                        val removed = removeAt(index)
                                        add(targetIndex, removed)
                                    }
                                    onReorder(newList)
                                    draggedItemIndex = targetIndex
                                }
                            },
                            onDragEnd = {
                                draggedItemIndex = null
                                dragOffset = Offset.Zero
                            },
                        )
                    },
            ) {
                content(item, isDragging) { draggedItemIndex = index }
            }
        }
    }
}

private fun <T> calculateTargetIndex(
    items: List<T>,
    draggedIndex: Int,
    dragOffset: Offset,
    itemHeight: Float,
): Int? {
    if (items.isEmpty()) return null

    val direction = when {
        dragOffset.y < -itemHeight / 2 -> -1
        dragOffset.y > itemHeight / 2 -> 1
        else -> return null
    }

    val targetIndex = draggedIndex + direction
    return if (targetIndex in items.indices) targetIndex else null
}

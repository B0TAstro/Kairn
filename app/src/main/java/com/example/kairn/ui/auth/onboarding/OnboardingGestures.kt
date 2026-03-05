package com.example.kairn.ui.auth.onboarding

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

internal fun Modifier.onboardingVerticalGestures(
    expanded: Boolean,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
): Modifier {
    return pointerInput(expanded) {
        detectVerticalDragGestures { _, dragAmount ->
            if (!expanded && dragAmount < VerticalSwipeThreshold) {
                onExpand()
            } else if (expanded && dragAmount < VerticalSwipeThreshold) {
                onCollapse()
            }
        }
    }
}

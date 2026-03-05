package com.example.kairn.ui.auth.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

@Composable
internal fun OnboardingBackground(
    imageAssetPath: String?,
    revealProgress: Float,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val imageBitmap = remember(imageAssetPath) {
        loadAssetBitmap(context, imageAssetPath)
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val imageScaleY = 1f / BackgroundVisibleFraction
        val verticalOverflowPx = constraints.maxHeight * (imageScaleY - 1f)
        val imageTranslationY = -verticalOverflowPx * revealProgress

        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(0.5f, 0f)
                        scaleY = imageScaleY
                        translationY = imageTranslationY
                    },
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(0.5f, 0f)
                        scaleY = imageScaleY
                        translationY = imageTranslationY
                    }
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.44f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.88f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
                            ),
                        ),
                    ),
            )
        }
    }
}

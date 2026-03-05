package com.example.kairn.ui.auth

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

private fun loadAssetBitmap(
    context: Context,
    path: String?,
): ImageBitmap? {
    if (path.isNullOrBlank()) return null
    return runCatching {
        context.assets.open(path).use { input ->
            BitmapFactory.decodeStream(input)?.asImageBitmap()
        }
    }.getOrNull()
}

/**
 * Full-screen background with a random onboarding image and a gradient overlay
 * that goes from dark at top to dark at bottom (to match the onboarding style).
 */
@Composable
internal fun AuthBackground(
    imageAssetPath: String?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val imageBitmap = remember(imageAssetPath) {
        loadAssetBitmap(context, imageAssetPath)
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Black.copy(alpha = 0.25f),
                            0.30f to Color.Black.copy(alpha = 0.10f),
                            0.45f to Color.Transparent,
                            0.60f to Color.Black.copy(alpha = 0.15f),
                            0.80f to Color.Black.copy(alpha = 0.40f),
                            1.0f to Color.Black.copy(alpha = 0.65f),
                        ),
                    ),
                ),
        )
    }
}

/**
 * Semi-transparent text field matching the onboarding/auth visual style.
 */
@Composable
internal fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    enabled: Boolean = true,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.6f),
            )
        },
        visualTransformation = visualTransformation,
        enabled = enabled,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = Color.White,
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White.copy(alpha = 0.25f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.20f),
            disabledContainerColor = Color.White.copy(alpha = 0.12f),
            cursorColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
    )
}

package com.example.kairn.ui.auth.onboarding

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

internal fun pickRandomOnboardingImage(context: Context): String? {
    val files = runCatching {
        context.assets.list(OnboardingAssetDirectory).orEmpty().toList()
    }.getOrDefault(emptyList())
        .filter { file ->
            file.endsWith(".jpg", ignoreCase = true) ||
                file.endsWith(".jpeg", ignoreCase = true) ||
                file.endsWith(".png", ignoreCase = true) ||
                file.endsWith(".webp", ignoreCase = true)
        }

    if (files.isEmpty()) return null
    return "$OnboardingAssetDirectory/${files.random()}"
}

internal fun loadAssetBitmap(
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

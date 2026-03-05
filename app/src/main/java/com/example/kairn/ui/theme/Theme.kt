package com.example.kairn.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val KairnLightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = TextAccent,
    onPrimaryContainer = TextPrimary,
    secondary = Secondary,
    onSecondary = TextPrimary,
    secondaryContainer = SurfaceVariant,       // chip background
    onSecondaryContainer = TextPrimary,         // chip text
    tertiary = Accent,
    onTertiary = Background,
    tertiaryContainer = Primary,               // chip selected background
    onTertiaryContainer = Color.White,         // chip selected text
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,                          // card background
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    surfaceContainerHighest = Surface,          // extra surface for cards
    error = ErrorRed,
    onError = Color.White,
    outline = DividerColor,
    outlineVariant = DividerColor,
)

@Composable
fun KairnTheme(
    content: @Composable () -> Unit,
) {
    val colorScheme = KairnLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}

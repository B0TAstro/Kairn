package com.example.kairn.ui.auth.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingScreen(
    onNavigateToSignUp: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val imageAssetPath = remember { pickRandomOnboardingImage(context) }
    var expanded by rememberSaveable { mutableStateOf(false) }
    val state = rememberOnboardingMotionState(expanded = expanded)

    val bgColor = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .onboardingVerticalGestures(
                expanded = expanded,
                onExpand = { expanded = true },
                onCollapse = { expanded = false },
            ),
    ) {
        OnboardingBackground(
            imageAssetPath = imageAssetPath,
            revealProgress = state.backgroundRevealProgress,
        )

        // Gradient overlay: opaque beige at top fading to transparent,
        // then transparent in middle, then dark tint at bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to bgColor.copy(alpha = 0.92f),
                            0.25f to bgColor.copy(alpha = 0.75f),
                            0.42f to bgColor.copy(alpha = 0.25f),
                            0.55f to Color.Transparent,
                            0.75f to MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            1.0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                        ),
                    ),
                ),
        )

        IntroContent(
            alpha = state.introAlpha,
            modifier = Modifier.align(Alignment.TopStart),
        )

        GoPrompt(
            alpha = state.introAlpha,
            bounceOffsetDp = state.goBounceOffsetDp,
            onGo = { expanded = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
        )

        CtaContent(
            alpha = state.ctaAlpha,
            offsetDp = state.ctaOffsetDp,
            onNavigateToSignUp = onNavigateToSignUp,
            onNavigateToSignIn = onNavigateToSignIn,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 28.dp),
        )
    }
}

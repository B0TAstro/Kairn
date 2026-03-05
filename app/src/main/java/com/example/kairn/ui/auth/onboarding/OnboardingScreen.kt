package com.example.kairn.ui.auth.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingScreen(
    imageAssetPath: String?,
    onNavigateToSignUp: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Black.copy(alpha = 0.35f),
                            0.30f to Color.Black.copy(alpha = 0.10f),
                            0.50f to Color.Transparent,
                            0.70f to MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            1.0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                        ),
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(state.ctaAlpha)
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.40f to Color.Black.copy(alpha = 0.10f),
                            0.65f to Color.Black.copy(alpha = 0.30f),
                            1.0f to Color.Black.copy(alpha = 0.55f),
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
                .navigationBarsPadding()
                .padding(bottom = 40.dp),
        )

        CtaContent(
            alpha = state.ctaAlpha,
            offsetDp = state.ctaOffsetDp,
            onNavigateToSignUp = onNavigateToSignUp,
            onNavigateToSignIn = onNavigateToSignIn,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 28.dp),
        )
    }
}

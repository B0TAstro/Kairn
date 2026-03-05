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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.72f),
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
                .padding(bottom = 42.dp),
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

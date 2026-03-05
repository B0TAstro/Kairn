package com.example.kairn.ui.auth.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

internal data class OnboardingMotionState(
    val backgroundRevealProgress: Float,
    val introAlpha: Float,
    val ctaAlpha: Float,
    val ctaOffsetDp: Float,
    val goBounceOffsetDp: Float,
)

@Composable
internal fun rememberOnboardingMotionState(
    expanded: Boolean,
): OnboardingMotionState {
    val backgroundRevealProgress by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "onboarding-bg-reveal-progress",
    )
    val introAlpha by animateFloatAsState(
        targetValue = if (expanded) 0f else 1f,
        animationSpec = tween(durationMillis = 280),
        label = "onboarding-intro-alpha",
    )
    val ctaAlpha by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(durationMillis = 420, delayMillis = 120),
        label = "onboarding-cta-alpha",
    )
    val ctaOffsetDp by animateFloatAsState(
        targetValue = if (expanded) 0f else 36f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "onboarding-cta-offset",
    )

    val bounceTransition = rememberInfiniteTransition(label = "go-bounce")
    val goBounceOffsetDp by bounceTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 760, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "go-bounce-offset",
    )

    return OnboardingMotionState(
        backgroundRevealProgress = backgroundRevealProgress,
        introAlpha = introAlpha,
        ctaAlpha = ctaAlpha,
        ctaOffsetDp = ctaOffsetDp,
        goBounceOffsetDp = goBounceOffsetDp,
    )
}

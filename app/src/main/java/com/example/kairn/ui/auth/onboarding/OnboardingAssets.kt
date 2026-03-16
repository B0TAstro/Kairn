package com.example.kairn.ui.auth.onboarding

import androidx.annotation.DrawableRes
import com.example.kairn.R

private val onboardingImages = listOf(
    R.drawable.mountain1,
    R.drawable.mountain2,
    R.drawable.mountain3,
    R.drawable.mountain4,
    R.drawable.mountain5,
)

@DrawableRes
internal fun pickRandomOnboardingImage(): Int = onboardingImages.random()

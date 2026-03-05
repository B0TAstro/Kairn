package com.example.kairn.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.kairn.R

private val ClashDisplay = FontFamily(
    Font(R.font.clash_display_variable, FontWeight.Medium),
    Font(R.font.clash_display_variable, FontWeight.SemiBold),
)

private val Satoshi = FontFamily(
    Font(R.font.satoshi_regular, FontWeight.Normal),
    Font(R.font.satoshi_medium, FontWeight.Medium),
    Font(R.font.satoshi_bold, FontWeight.Bold),
)

val Typography = Typography(
    // ── Display ──────────────────────────────────────────────
    // displayLarge  = 44sp – Screen titles, major headings (AGENTS.md "title" token)
    displayLarge = TextStyle(
        fontFamily = ClashDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 44.sp,
        lineHeight = 48.sp,
        letterSpacing = 0.sp,
    ),
    // displayMedium = 36sp – Large screen titles (e.g. Explore header)
    displayMedium = TextStyle(
        fontFamily = ClashDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    // displaySmall  = 28sp – Stat highlights, profile stat numbers
    displaySmall = TextStyle(
        fontFamily = ClashDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),

    // ── Headline ─────────────────────────────────────────────
    // headlineLarge = 28sp – Kept at same size as headlineMedium with SemiBold for emphasis
    headlineLarge = TextStyle(
        fontFamily = ClashDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),
    // headlineMedium = 28sp – Section headers, card titles (AGENTS.md "heading" token)
    headlineMedium = TextStyle(
        fontFamily = ClashDisplay,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),
    // headlineSmall = 26sp – Card titles (large variant)
    headlineSmall = TextStyle(
        fontFamily = ClashDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
    ),

    // ── Title ────────────────────────────────────────────────
    // titleLarge = 24sp – Card titles, bottom sheet headings
    titleLarge = TextStyle(
        fontFamily = ClashDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    // titleMedium = 22sp – Smaller card titles
    titleMedium = TextStyle(
        fontFamily = ClashDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp,
    ),
    // titleSmall = 20sp – Section headers, stat values
    titleSmall = TextStyle(
        fontFamily = ClashDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),

    // ── Body ─────────────────────────────────────────────────
    // bodyLarge = 16sp – Body text, labels, descriptions (AGENTS.md "text" token)
    bodyLarge = TextStyle(
        fontFamily = Satoshi,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
    ),
    // bodyMedium = 14sp – Default body text, descriptions, button labels
    bodyMedium = TextStyle(
        fontFamily = Satoshi,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    // bodySmall = 13sp – Small body, chip labels, location text
    bodySmall = TextStyle(
        fontFamily = Satoshi,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
    ),

    // ── Label ────────────────────────────────────────────────
    // labelLarge = 18sp – Sub-headings, emphasized body
    labelLarge = TextStyle(
        fontFamily = Satoshi,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    // labelMedium = 12sp – Badge text, small stat labels
    labelMedium = TextStyle(
        fontFamily = Satoshi,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
    ),
    // labelSmall = 11sp – Captions, metadata, timestamps
    labelSmall = TextStyle(
        fontFamily = Satoshi,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.sp,
    ),
)

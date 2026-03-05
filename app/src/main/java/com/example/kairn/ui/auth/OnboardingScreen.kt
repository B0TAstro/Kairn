package com.example.kairn.ui.auth

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

private const val OnboardingAssetDirectory = "onboarding/images"
private const val LoginTag = "login"

@Composable
fun OnboardingScreen(
    onNavigateToSignUp: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val imageAssetPath = remember { pickRandomOnboardingImage(context) }
    var expanded by rememberSaveable { mutableStateOf(false) }

    val backgroundShift by animateFloatAsState(
        targetValue = if (expanded) -0.18f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "onboarding-bg-shift",
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
    val ctaOffset by animateFloatAsState(
        targetValue = if (expanded) 0f else 36f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "onboarding-cta-offset",
    )

    val bounceTransition = rememberInfiniteTransition(label = "go-bounce")
    val bounceOffset by bounceTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 760, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "go-bounce-offset",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInputWhenCollapsed(expanded = expanded) { expanded = true },
    ) {
        OnboardingBackground(
            imageAssetPath = imageAssetPath,
            shiftFraction = backgroundShift,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background.copy(alpha = 0.10f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.64f),
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 28.dp, top = 78.dp)
                .alpha(introAlpha),
        ) {
            Text(
                text = "Welcome to",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "KAIRN",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Text(
            text = "Escape your routine and discover breathtaking trails near you.",
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
                .alpha(introAlpha),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 42.dp)
                .offset(y = bounceOffset.dp)
                .alpha(introAlpha)
                .clickable(enabled = !expanded) { expanded = true },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                modifier = Modifier.height(16.dp),
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                modifier = Modifier
                    .height(16.dp)
                    .offset(y = (-8).dp),
            )
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .height(56.dp)
                    .fillMaxWidth(0.16f),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(enabled = !expanded) { expanded = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "GO",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        val signInText = remember {
            buildAnnotatedString {
                append("Deja un compte ? ")
                pushStringAnnotation(tag = LoginTag, annotation = LoginTag)
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                    ),
                ) {
                    append("Connectez-vous")
                }
                pop()
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 28.dp)
                .offset(y = ctaOffset.dp)
                .alpha(ctaAlpha),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Rejoignez une communaute qui partage ses randonnees et ses balades.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Button(
                onClick = onNavigateToSignUp,
                modifier = Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                    text = "S'inscrire",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

            ClickableText(
                text = signInText,
                modifier = Modifier.padding(top = 14.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                ),
                onClick = { offset ->
                    signInText
                        .getStringAnnotations(tag = LoginTag, start = offset, end = offset)
                        .firstOrNull()
                        ?.let { onNavigateToSignIn() }
                },
            )
        }
    }
}

@Composable
private fun OnboardingBackground(
    imageAssetPath: String?,
    shiftFraction: Float,
) {
    val context = LocalContext.current
    val imageBitmap = remember(imageAssetPath) {
        loadAssetBitmap(context, imageAssetPath)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val shiftPixels = constraints.maxHeight * shiftFraction

        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { translationY = shiftPixels },
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { translationY = shiftPixels }
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

private fun pickRandomOnboardingImage(context: Context): String? {
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

private fun Modifier.pointerInputWhenCollapsed(
    expanded: Boolean,
    onSwipeUp: () -> Unit,
): Modifier {
    if (expanded) return this

    return pointerInput(Unit) {
        detectVerticalDragGestures { _, dragAmount ->
            if (dragAmount < -14f) {
                onSwipeUp()
            }
        }
    }
}

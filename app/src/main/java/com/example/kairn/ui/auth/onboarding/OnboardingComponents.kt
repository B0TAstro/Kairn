package com.example.kairn.ui.auth.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun IntroContent(
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    val darkTextColor = MaterialTheme.colorScheme.onBackground

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(alpha),
    ) {
        // Top section: "Welcome to" + "KAIRN"
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 32.dp, end = 32.dp, top = 80.dp)
                .fillMaxWidth(),
        ) {
            Text(
                text = "Welcome to",
                style = TextStyle(
                    fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 26.sp,
                    letterSpacing = 0.sp,
                ),
                color = darkTextColor,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "KAIRN",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = darkTextColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Middle-lower section: tagline phrases
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
                .offset(y = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Echappez a votre routine et decouvrez des sentiers a couper le souffle pres de chez vous.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Bougez, respirez, et racontez ensuite votre aventure.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
internal fun GoPrompt(
    alpha: Float,
    bounceOffsetDp: Float,
    onGo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier
            .alpha(alpha)
            .offset(y = bounceOffsetDp.dp)
            .clickable(enabled = alpha > 0f, onClick = onGo),
    ) {
        // Background pill with vertical fade
        Box(
            modifier = Modifier
                .width(72.dp)
                .height(110.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.35f to Color.White.copy(alpha = 0.10f),
                            0.6f to Color.White.copy(alpha = 0.18f),
                            1.0f to Color.White.copy(alpha = 0.25f),
                        ),
                    ),
                ),
        )

        // Chevrons + GO button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            // Small chevrons
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp),
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(18.dp)
                    .offset(y = (-6).dp),
            )

            Spacer(modifier = Modifier.height(2.dp))

            // GO circle button
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.size(56.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(enabled = alpha > 0f, onClick = onGo),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "GO",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }
    }
}

@Composable
internal fun CtaContent(
    alpha: Float,
    offsetDp: Float,
    onNavigateToSignUp: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lightTextColor = MaterialTheme.colorScheme.onPrimary
    val signInText = remember {
        buildAnnotatedString {
            append("Deja un compte ? ")
            pushStringAnnotation(tag = LoginAnnotationTag, annotation = LoginAnnotationTag)
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
        modifier = modifier
            .alpha(alpha)
            .offset(y = offsetDp.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Rejoignez une communaute qui partage ses randonnees et ses balades.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium,
            color = lightTextColor,
        )
        Button(
            onClick = onNavigateToSignUp,
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
            ),
        ) {
            Text(
                text = "S'inscrire",
                style = MaterialTheme.typography.headlineMedium,
            )
        }
        ClickableText(
            text = signInText,
            modifier = Modifier.padding(top = 14.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = lightTextColor,
                textAlign = TextAlign.Center,
            ),
            onClick = { offset ->
                signInText
                    .getStringAnnotations(
                        tag = LoginAnnotationTag,
                        start = offset,
                        end = offset,
                    )
                    .firstOrNull()
                    ?.let { onNavigateToSignIn() }
            },
        )
    }
}

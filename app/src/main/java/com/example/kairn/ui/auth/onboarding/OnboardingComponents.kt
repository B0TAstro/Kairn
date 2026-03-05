package com.example.kairn.ui.auth.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@Composable
internal fun IntroContent(
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    val lightTextColor = MaterialTheme.colorScheme.onPrimary

    Column(
        modifier = modifier
            .alpha(alpha)
            .padding(start = 32.dp, top = 80.dp),
    ) {
        Text(
            text = "Welcome to",
            style = MaterialTheme.typography.bodyLarge,
            color = lightTextColor,
        )
        Text(
            text = "KAIRN",
            style = MaterialTheme.typography.displayLarge,
            color = lightTextColor,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Echappez a votre routine et decouvrez des sentiers a couper le souffle pres de chez vous.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = lightTextColor,
        )
        Text(
            text = "Bougez, respirez, et racontez ensuite votre aventure.",
            modifier = Modifier.padding(top = 8.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = lightTextColor,
        )
    }
}

@Composable
internal fun GoPrompt(
    alpha: Float,
    bounceOffsetDp: Float,
    onGo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lightTextColor = MaterialTheme.colorScheme.onPrimary

    Column(
        modifier = modifier
            .alpha(alpha)
            .offset(y = bounceOffsetDp.dp)
            .clickable(enabled = alpha > 0f, onClick = onGo),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowUp,
            contentDescription = null,
            tint = lightTextColor,
            modifier = Modifier.height(16.dp),
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowUp,
            contentDescription = null,
            tint = lightTextColor,
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
                    .clickable(enabled = alpha > 0f, onClick = onGo),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "GO",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
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

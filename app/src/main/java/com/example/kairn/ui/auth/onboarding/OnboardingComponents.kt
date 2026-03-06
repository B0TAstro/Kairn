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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairn.R

@Composable
internal fun IntroContent(
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    val titleTextColor = Color.White

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(alpha),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 32.dp, end = 32.dp, top = 100.dp)
                .fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.onboarding_welcome_to),
                style = TextStyle(
                    fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 22.sp,
                    letterSpacing = 0.sp,
                ),
                color = titleTextColor,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "KAIRN",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = titleTextColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 28.dp)
                .offset(y = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.onboarding_tagline),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.onboarding_sub_tagline),
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
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(90.dp)
                .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.35f to Color.White.copy(alpha = 0.08f),
                            0.6f to Color.White.copy(alpha = 0.15f),
                            1.0f to Color.White.copy(alpha = 0.22f),
                        ),
                    ),
                ),
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 6.dp),
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(14.dp),
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(14.dp)
                    .offset(y = (-4).dp),
            )

            Spacer(modifier = Modifier.height(2.dp))

            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 6.dp,
                modifier = Modifier.size(44.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(enabled = alpha > 0f, onClick = onGo),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_go),
                        style = MaterialTheme.typography.titleSmall.copy(
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
    val alreadyAccount = stringResource(R.string.onboarding_already_account)
    val loginLink = stringResource(R.string.onboarding_login_link)
    val signInText = remember(alreadyAccount, loginLink) {
        buildAnnotatedString {
            append(alreadyAccount)
            pushStringAnnotation(tag = LoginAnnotationTag, annotation = LoginAnnotationTag)
            withStyle(
                SpanStyle(
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                ),
            ) {
                append(loginLink)
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
            text = stringResource(R.string.onboarding_cta_description),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Normal,
            ),
            color = lightTextColor,
        )
        Button(
            onClick = onNavigateToSignUp,
            modifier = Modifier
                .padding(top = 40.dp)
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(
                text = stringResource(R.string.signup_button),
                style = MaterialTheme.typography.titleSmall,
            )
        }
        ClickableText(
            text = signInText,
            modifier = Modifier.padding(top = 12.dp),
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

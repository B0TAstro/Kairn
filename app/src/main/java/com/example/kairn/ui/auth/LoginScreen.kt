package com.example.kairn.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LoginScreen(
    imageAssetPath: String?,
    onNavigateToSignUp: () -> Unit,
    onSignInSuccess: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onSignInSuccess()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AuthBackground(imageAssetPath = imageAssetPath)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
        ) {
            // Back arrow
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }

            // Title
            Column(
                modifier = Modifier
                    .padding(horizontal = 28.dp)
                    .padding(top = 16.dp),
            ) {
                Text(
                    text = "Bon retour",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = Color.White,
                )
                Text(
                    text = "PARMI NOUS !",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Color.White,
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            // Form + button + separator block — all bottom-aligned
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
            ) {
                // Error message
                val errorMessage = when {
                    uiState is AuthUiState.Error -> (uiState as AuthUiState.Error).message
                    validationError != null -> validationError
                    else -> null
                }
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                // Fields
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AuthTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "Email",
                        enabled = uiState !is AuthUiState.Loading,
                    )

                    AuthTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "Mot de passe",
                        visualTransformation = PasswordVisualTransformation(),
                        enabled = uiState !is AuthUiState.Loading,
                    )
                }
                // Remember me + Forgot password
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                uncheckedColor = Color.White.copy(alpha = 0.6f),
                                checkmarkColor = Color.White,
                            ),
                        )
                        Text(
                            text = "Se souvenir de moi",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f),
                        )
                    }
                    Text(
                        text = "Mot de passe oublie ?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.clickable { /* TODO */ },
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Log In button
                if (uiState is AuthUiState.Loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(48.dp),
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            when {
                                email.isBlank() -> validationError = "Veuillez entrer votre email"
                                password.isBlank() -> validationError = "Veuillez entrer votre mot de passe"
                                else -> {
                                    validationError = null
                                    viewModel.signIn(email.trim(), password)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(
                            text = "Se connecter",
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                }

                // Separator
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .padding(end = 16.dp)
                            .background(Color.White.copy(alpha = 0.3f)),
                    )
                    Text(
                        text = "ou",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f),
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .padding(start = 16.dp)
                            .background(Color.White.copy(alpha = 0.3f)),
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Navigate to Sign Up
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Pas encore de compte ? ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                    )
                    Text(
                        text = "S'inscrire",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = Color.White,
                        modifier = Modifier.clickable { onNavigateToSignUp() },
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

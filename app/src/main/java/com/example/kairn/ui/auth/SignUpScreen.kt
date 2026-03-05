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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SignUpScreen(
    onNavigateToSignIn: () -> Unit,
    onSignUpSuccess: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var pseudo by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val imageAssetPath = remember { pickRandomAuthImage(context) }

    val passwordsMatch = password == confirmPassword
    val canSignUp = firstName.isNotBlank() &&
        lastName.isNotBlank() &&
        pseudo.isNotBlank() &&
        email.isNotBlank() &&
        password.isNotBlank() &&
        passwordsMatch

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onSignUpSuccess()
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
                    text = "Creer un",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = Color.White,
                )
                Text(
                    text = "COMPTE",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Color.White,
                )
            }

            // Push form + button + separator to the bottom
            Spacer(modifier = Modifier.weight(1f))

            // Form + button + separator block — all bottom-aligned
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
            ) {
                // Error message
                if (uiState is AuthUiState.Error) {
                    Text(
                        text = (uiState as AuthUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }

                // Fields
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Name + Lastname side by side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        AuthTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            placeholder = "Prenom",
                            enabled = uiState !is AuthUiState.Loading,
                            modifier = Modifier.weight(1f),
                        )
                        AuthTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            placeholder = "Nom",
                            enabled = uiState !is AuthUiState.Loading,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    AuthTextField(
                        value = pseudo,
                        onValueChange = { pseudo = it },
                        placeholder = "Pseudo",
                        enabled = uiState !is AuthUiState.Loading,
                    )

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

                    AuthTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = "Confirmer le mot de passe",
                        visualTransformation = PasswordVisualTransformation(),
                        enabled = uiState !is AuthUiState.Loading,
                    )
                }

                // Terms of service
                Text(
                    text = "En continuant, j'accepte les Conditions d'utilisation et la Politique de confidentialite de Kairn",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 6.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sign Up button
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
                            viewModel.signUp(
                                email = email.trim(),
                                password = password,
                                firstName = firstName.trim(),
                                lastName = lastName.trim(),
                                pseudo = pseudo.trim(),
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        enabled = canSignUp,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text(
                            text = "S'inscrire",
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

                // Navigate to Log In
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Deja un compte ? ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                    )
                    Text(
                        text = "Se connecter",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = Color.White,
                        modifier = Modifier.clickable { onNavigateToSignIn() },
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

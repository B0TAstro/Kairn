package com.example.kairn.ui.auth

import androidx.compose.foundation.background
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
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(28.dp),
                )
            }

            // Title section
            Column(
                modifier = Modifier
                    .padding(horizontal = 28.dp)
                    .padding(top = 16.dp),
            ) {
                Text(
                    text = "Create account",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = Color.White,
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Form fields
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Error message
                if (uiState is AuthUiState.Error) {
                    Text(
                        text = (uiState as AuthUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }

                // Name + Lastname side by side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AuthTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        placeholder = "Name",
                        enabled = uiState !is AuthUiState.Loading,
                        modifier = Modifier.weight(1f),
                    )
                    AuthTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        placeholder = "Lastname",
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
                    placeholder = "Password",
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = uiState !is AuthUiState.Loading,
                )

                AuthTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = "Confirm Password",
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = uiState !is AuthUiState.Loading,
                )

                // Terms of service text
                Text(
                    text = "By continuing, I agree to Kairn's Terms of Service and acknowledge the Privacy Policy",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sign Up button
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(48.dp),
                    )
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
                            text = "Sign Up",
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                }

                // Separator "or"
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .padding(end = 16.dp)
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.3f)),
                    )
                    Text(
                        text = "or",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f),
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .padding(start = 16.dp)
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.3f)),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

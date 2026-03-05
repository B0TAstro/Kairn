package com.example.kairn.ui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

import com.example.kairn.domain.model.User
import com.example.kairn.ui.auth.AuthViewModel
import com.example.kairn.ui.theme.KairnTheme

@Composable
fun AccountScreen(
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (currentUser != null) {
            val user = currentUser!!
            ProfileContent(user = user)
        } else {
            Text(
                text = "Not signed in",
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                authViewModel.signOut()
                onSignOut()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Sign Out")
        }
    }
}

@Composable
private fun ProfileContent(user: User) {
    Text(
        text = user.username ?: "Anonymous",
        style = MaterialTheme.typography.titleLarge,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = user.email,
        style = MaterialTheme.typography.bodyLarge,
    )

    Text(
        text = "Level ${user.level} — ${user.xp} XP",
        style = MaterialTheme.typography.bodyLarge,
    )

    if (user.city != null) {
        Text(
            text = "${user.city}, ${user.country.orEmpty()}",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AccountScreenPreview() {
    KairnTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(24.dp))
            ProfileContent(user = User.preview)
        }
    }
}

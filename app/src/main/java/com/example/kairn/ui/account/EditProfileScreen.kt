package com.example.kairn.ui.account

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kairn.domain.model.User
import com.example.kairn.ui.theme.KairnTheme

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val editState by viewModel.editState.collectAsStateWithLifecycle()

    // Initialise form fields when the screen opens
    LaunchedEffect(currentUser) {
        currentUser?.let { viewModel.initEditForm(it) }
    }

    // Navigate back on successful save
    LaunchedEffect(editState.saveSuccess) {
        if (editState.saveSuccess) {
            viewModel.clearSaveSuccess()
            onBack()
        }
    }

    val context = LocalContext.current

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let {
            val bytes = context.contentResolver.openInputStream(it)?.use { stream ->
                stream.readBytes()
            }
            if (bytes != null) {
                viewModel.uploadAvatar(bytes)
            }
        }
    }

    EditProfileContent(
        editState = editState,
        initials = currentUser?.let { AccountViewModel.getInitials(it) } ?: "?",
        onBack = onBack,
        onPseudoChange = viewModel::onPseudoChange,
        onBioChange = viewModel::onBioChange,
        onCityChange = viewModel::onCityChange,
        onAvatarClick = { imagePickerLauncher.launch("image/*") },
        onSave = viewModel::saveProfile,
        modifier = modifier,
    )
}

@Composable
private fun EditProfileContent(
    editState: EditProfileUiState,
    initials: String,
    onBack: () -> Unit,
    onPseudoChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onAvatarClick: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // --- Top bar ---
        TopBar(onBack = onBack)

        Spacer(modifier = Modifier.height(32.dp))

        // --- Avatar with camera overlay ---
        AvatarEditor(
            avatarUrl = editState.avatarUrl,
            initials = initials,
            onClick = onAvatarClick,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Changer la photo",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onAvatarClick),
        )

        Spacer(modifier = Modifier.height(28.dp))

        // --- Form fields ---
        ProfileTextField(
            value = editState.pseudo,
            onValueChange = onPseudoChange,
            label = "Pseudo",
        )

        Spacer(modifier = Modifier.height(16.dp))

        ProfileTextField(
            value = editState.bio,
            onValueChange = onBioChange,
            label = "Bio",
            singleLine = false,
            minLines = 3,
        )

        Spacer(modifier = Modifier.height(16.dp))

        ProfileTextField(
            value = editState.city,
            onValueChange = onCityChange,
            label = "Ville",
        )

        // --- Error message ---
        editState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Save button ---
        SaveButton(
            isSaving = editState.isSaving,
            onClick = onSave,
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top Bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Retour",
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
        Text(
            text = "Modifier le profil",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Avatar Editor
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AvatarEditor(
    avatarUrl: String?,
    initials: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(120.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
            )
        } else {
            // Initials fallback
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            ) {
                Text(
                    text = initials,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.displaySmall,
                )
            }
        }

        // Camera overlay
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.3f)),
        ) {
            Icon(
                imageVector = Icons.Filled.CameraAlt,
                contentDescription = "Changer la photo",
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Profile Text Field
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(6.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            minLines = minLines,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onBackground,
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Save Button
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SaveButton(
    isSaving: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(enabled = !isSaving, onClick = onClick)
            .padding(vertical = 16.dp),
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = "Enregistrer",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    KairnTheme {
        EditProfileContent(
            editState = EditProfileUiState(
                pseudo = "JohnHiker",
                bio = "Passionné de randonnée depuis toujours",
                city = "Paris",
            ),
            initials = "JO",
            onBack = {},
            onPseudoChange = {},
            onBioChange = {},
            onCityChange = {},
            onAvatarClick = {},
            onSave = {},
        )
    }
}

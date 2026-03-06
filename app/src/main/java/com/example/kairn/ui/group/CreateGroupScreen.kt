package com.example.kairn.ui.group

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kairn.R
import com.example.kairn.domain.model.User
import com.example.kairn.ui.components.UserAvatar
import com.example.kairn.ui.theme.Accent
import com.example.kairn.ui.theme.Background
import com.example.kairn.ui.theme.CardBackground
import com.example.kairn.ui.theme.KairnTheme
import com.example.kairn.ui.theme.Primary
import com.example.kairn.ui.theme.TextPrimary
import com.example.kairn.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    onNavigateBack: () -> Unit,
    onGroupCreated: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateGroupViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.create_group_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.createGroup(onGroupCreated) },
                        enabled = uiState.isValid && !uiState.isCreating
                    ) {
                        if (uiState.isCreating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Primary,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.cd_create),
                                tint = if (uiState.isValid) Accent else TextSecondary,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary,
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = CardBackground,
                    contentColor = TextPrimary,
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Group name input
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.group_name_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            GroupNameTextField(
                value = uiState.groupName,
                onValueChange = viewModel::onGroupNameChanged,
                modifier = Modifier.fillMaxWidth()
            )

            // Member selection
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.add_members_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
            )
            Text(
                text = stringResource(R.string.members_selected, uiState.selectedMemberIds.size),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Member list
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
                uiState.availableFriends.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_friends_to_add),
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary,
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.availableFriends) { friend ->
                            MemberSelectionItem(
                                user = friend,
                                isSelected = friend.id in uiState.selectedMemberIds,
                                onToggle = { viewModel.onMemberToggled(friend.id) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupNameTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = TextPrimary,
        ),
        singleLine = true,
        cursorBrush = SolidColor(Primary),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(R.string.group_name_placeholder),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun MemberSelectionItem(
    user: User,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .clickable(onClick = onToggle)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(
            initials = (user.username ?: user.email).take(2).uppercase(),
            size = 48.dp,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.username ?: user.email,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
            )
            if (user.username != null) {
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
        }
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = Primary,
                uncheckedColor = TextSecondary,
                checkmarkColor = Background,
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateGroupScreenPreview() {
    KairnTheme {
        // Preview requires manual state setup
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
        ) {
            Text("Create Group Screen Preview")
        }
    }
}

package com.example.kairn.ui.group

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kairn.R
import com.example.kairn.domain.model.GroupMember
import com.example.kairn.domain.model.GroupRole
import com.example.kairn.ui.components.UserAvatar
import com.example.kairn.ui.theme.Accent
import com.example.kairn.ui.theme.Background
import com.example.kairn.ui.theme.CardBackground
import com.example.kairn.ui.theme.KairnTheme
import com.example.kairn.ui.theme.Primary
import com.example.kairn.ui.theme.TextPrimary
import com.example.kairn.ui.theme.TextSecondary
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupInfoScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GroupInfoViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showMenu by remember { mutableStateOf(false) }

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
                        text = stringResource(R.string.group_info_title),
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
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.cd_more_options)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_leave_group)) },
                                onClick = {
                                    showMenu = false
                                    viewModel.leaveGroup(onNavigateBack)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = null
                                    )
                                }
                            )
                            if (uiState.canDelete) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.menu_delete_group)) },
                                    onClick = {
                                        showMenu = false
                                        viewModel.deleteGroup(onNavigateBack)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null
                                        )
                                    }
                                )
                            }
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
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            uiState.group == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.group_not_found),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Group info card
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CardBackground)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = uiState.group!!.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                            )
                            if (uiState.group!!.description != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = uiState.group!!.description!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(
                                    R.string.members_count_visibility,
                                    uiState.members.size,
                                    uiState.group!!.visibility,
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                            )
                        }
                    }

                    // Members section
                    item {
                        Text(
                            text = stringResource(R.string.members_section_header),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                        )
                    }

                    items(uiState.members) { member ->
                        MemberItem(
                            member = member,
                            canRemove = uiState.canManageMembers && member.role != GroupRole.OWNER,
                            onRemove = { viewModel.removeMember(member.userId) },
                            isUpdating = uiState.isUpdating,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberItem(
    member: GroupMember,
    canRemove: Boolean,
    onRemove: () -> Unit,
    isUpdating: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(
            initials = (member.user?.username ?: member.userId).take(2).uppercase(),
            size = 48.dp,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member.user?.username ?: stringResource(R.string.unknown_member),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
            )
            Text(
                text = member.role.name,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
        if (canRemove) {
            Button(
                onClick = onRemove,
                enabled = !isUpdating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Accent.copy(alpha = 0.1f),
                    contentColor = Accent,
                )
            ) {
                Text(stringResource(R.string.remove_member_button))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupInfoScreenPreview() {
    KairnTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
        ) {
            Text("Group Info Screen Preview")
        }
    }
}

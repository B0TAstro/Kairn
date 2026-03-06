package com.example.kairn.ui.friends

import android.util.Log
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.kairn.domain.model.Friendship
import com.example.kairn.domain.model.User
import com.example.kairn.ui.components.UserAvatar
import com.example.kairn.ui.theme.Accent
import com.example.kairn.ui.theme.Background
import com.example.kairn.ui.theme.CardBackground
import com.example.kairn.ui.theme.Primary
import com.example.kairn.ui.theme.Secondary
import com.example.kairn.ui.theme.TextPrimary
import com.example.kairn.ui.theme.TextSecondary

private const val TAG = "FriendListScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FriendViewModel = hiltViewModel(),
) {
    val friendListState by viewModel.friendListUiState.collectAsStateWithLifecycle()
    val searchState by viewModel.searchUiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Friends",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary,
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            SearchBar(
                query = searchState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when {
                searchState.searchQuery.isNotBlank() -> {
                    // Show search results
                    if (searchState.isSearching) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Primary)
                        }
                    } else {
                        SearchResults(
                            users = searchState.searchResults,
                            onSendRequest = viewModel::sendFriendRequest,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                else -> {
                    // Show friends list
                    when (friendListState) {
                        is FriendListUiState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Primary)
                            }
                        }
                        is FriendListUiState.Success -> {
                            val state = friendListState as FriendListUiState.Success
                            FriendList(
                                friends = state.friends,
                                pendingRequests = state.pendingRequests,
                                onAcceptRequest = viewModel::acceptFriendRequest,
                                onDeclineRequest = viewModel::declineFriendRequest,
                                onStartChat = { friend ->
                                    Log.d(TAG, "onStartChat: friend.id=${friend.id}, username=${friend.username}")
                                    viewModel.startConversationWith(friend.id) { conversationId ->
                                        Log.d(TAG, "onStartChat: Navigating to chat - conversationId=$conversationId")
                                        onNavigateToChat(conversationId, friend.username ?: "User")
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        is FriendListUiState.Error -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (friendListState as FriendListUiState.Error).message,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = TextPrimary,
                fontSize = 16.sp,
            ),
            cursorBrush = SolidColor(Primary),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = "Search for friends...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 16.sp,
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
private fun SearchResults(
    users: List<User>,
    onSendRequest: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (users.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No users found",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(users, key = { it.id }) { user ->
                UserCard(
                    user = user,
                    onSendRequest = { onSendRequest(user.id) },
                )
            }
        }
    }
}

@Composable
private fun FriendList(
    friends: List<Friendship>,
    pendingRequests: List<Friendship>,
    onAcceptRequest: (String) -> Unit,
    onDeclineRequest: (String) -> Unit,
    onStartChat: (User) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Pending requests section
        if (pendingRequests.isNotEmpty()) {
            item {
                Text(
                    text = "Pending Requests",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(pendingRequests, key = { it.id }) { friendship ->
                PendingRequestCard(
                    friendship = friendship,
                    onAccept = { onAcceptRequest(friendship.id) },
                    onDecline = { onDeclineRequest(friendship.id) },
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // Friends section
        if (friends.isNotEmpty()) {
            item {
                Text(
                    text = "Friends",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(friends, key = { it.id }) { friendship ->
                FriendCard(
                    friendship = friendship,
                    onStartChat = { onStartChat(friendship.friend) },
                )
            }
        }

        if (friends.isEmpty() && pendingRequests.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No friends yet\nSearch for users to add friends",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun UserCard(
    user: User,
    onSendRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UserAvatar(
            initials = (user.username ?: user.email).take(2).uppercase(),
            size = 40.dp,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.username ?: user.email,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
            )
            if (user.username != null) {
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 12.sp,
                )
            }
        }
        Button(
            onClick = onSendRequest,
            colors = ButtonDefaults.buttonColors(containerColor = Accent)
        ) {
            Text("Add", color = Background)
        }
    }
}

@Composable
private fun PendingRequestCard(
    friendship: Friendship,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Secondary.copy(alpha = 0.2f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UserAvatar(
            initials = (friendship.friend.username ?: friendship.friend.email).take(2).uppercase(),
            size = 40.dp,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = friendship.friend.username ?: friendship.friend.email,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "wants to be friends",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 12.sp,
            )
        }
        IconButton(onClick = onAccept) {
            Icon(Icons.Default.Check, contentDescription = "Accept", tint = Primary)
        }
        IconButton(onClick = onDecline) {
            Icon(Icons.Default.Close, contentDescription = "Decline", tint = TextSecondary)
        }
    }
}

@Composable
private fun FriendCard(
    friendship: Friendship,
    onStartChat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .clickable(onClick = onStartChat)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UserAvatar(
            initials = (friendship.friend.username ?: friendship.friend.email).take(2).uppercase(),
            size = 40.dp,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = friendship.friend.username ?: friendship.friend.email,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "Tap to chat",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 12.sp,
            )
        }
    }
}

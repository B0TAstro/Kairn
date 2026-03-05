package com.example.kairn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ChatBubble(
    senderName: String,
    senderInitials: String,
    message: String,
    modifier: Modifier = Modifier,
    isCurrentUser: Boolean = false,
) {
    val surfaceColor = MaterialTheme.colorScheme.surface

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
    ) {
        if (!isCurrentUser) {
            UserAvatar(
                initials = senderInitials,
                size = 32.dp,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Column(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = if (isCurrentUser) 16.dp else 4.dp,
                        topEnd = if (isCurrentUser) 4.dp else 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp,
                    ),
                )
                .background(
                    if (isCurrentUser) surfaceColor
                    else surfaceColor.copy(alpha = 0.7f),
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            if (!isCurrentUser) {
                Text(
                    text = senderName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        if (isCurrentUser) {
            Spacer(modifier = Modifier.width(8.dp))
            UserAvatar(
                initials = senderInitials,
                size = 32.dp,
            )
        }
    }
}

package com.example.kairn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairn.domain.model.MessageType
import com.example.kairn.ui.theme.CardBackground
import com.example.kairn.ui.theme.Primary
import com.example.kairn.ui.theme.TextPrimary
import com.example.kairn.ui.theme.TextSecondary

/**
 * Instagram-style chat bubble
 * - Current user's messages: Right-aligned, primary color background
 * - Other user's messages: Left-aligned, card background, with avatar
 * - System messages: Centered, small text
 */
@Composable
fun ChatBubble(
    senderName: String,
    senderInitials: String,
    message: String,
    messageType: MessageType = MessageType.TEXT,
    modifier: Modifier = Modifier,
    isCurrentUser: Boolean = false,
) {
    // System messages are centered and styled differently
    if (messageType == MessageType.SYSTEM) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    // Regular message bubbles
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        // Avatar for other user (left side)
        if (!isCurrentUser) {
            UserAvatar(
                initials = senderInitials,
                size = 32.dp,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        // Message bubble
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isCurrentUser) 16.dp else 4.dp,
                        bottomEnd = if (isCurrentUser) 4.dp else 16.dp,
                    ),
                )
                .background(
                    if (isCurrentUser) Primary else CardBackground
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            // Show sender name only for other users in group chats
            if (!isCurrentUser && senderName.isNotBlank()) {
                Text(
                    text = senderName,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                )
            }
            Text(
                text = message,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontSize = 14.sp,
            )
        }
    }
}

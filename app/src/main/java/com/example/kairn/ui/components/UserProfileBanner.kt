package com.example.kairn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.sp
import com.example.kairn.ui.theme.CardBackground
import com.example.kairn.ui.theme.TextPrimary
import com.example.kairn.ui.theme.TextSecondary

@Composable
fun UserProfileBanner(
    username: String,
    location: String,
    initials: String,
    modifier: Modifier = Modifier,
    isOnline: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground.copy(alpha = 0.85f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UserAvatar(
            initials = initials,
            size = 48.dp,
            showOnlineIndicator = isOnline,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = username,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
            )
            Text(
                text = location,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
fun HomeGreetingHeader(
    username: String,
    location: String,
    initials: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "Hello, $username",
                style = MaterialTheme.typography.displayLarge,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Where would you like to go?",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontSize = 14.sp,
            )
        }
        UserAvatar(
            initials = initials,
            size = 48.dp,
        )
    }
}

package com.example.kairn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.kairn.ui.theme.Accent
import com.example.kairn.ui.theme.CardBackground
import com.example.kairn.ui.theme.TextPrimary
import com.example.kairn.ui.theme.TextSecondary

data class HikeParticipant(
    val name: String,
    val initials: String,
    val position: Int,
    val isOnline: Boolean = false,
)

@Composable
fun UserListItem(
    participant: HikeParticipant,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Position badge
        Text(
            text = "#${participant.position}",
            style = MaterialTheme.typography.bodyMedium,
            color = if (participant.position <= 3) Accent else TextSecondary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
        )
        Spacer(modifier = Modifier.width(12.dp))
        UserAvatar(
            initials = participant.initials,
            size = 36.dp,
            showOnlineIndicator = participant.isOnline,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = participant.name,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun HikeProgressList(
    participants: List<HikeParticipant>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        participants.forEach { participant ->
            UserListItem(participant = participant)
        }
    }
}

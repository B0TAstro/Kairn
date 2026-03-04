package com.example.kairn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairn.ui.theme.CardBackground
import com.example.kairn.ui.theme.Secondary
import com.example.kairn.ui.theme.TextPrimary
import com.example.kairn.ui.theme.TextSecondary

@Composable
fun HikeDetailStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    subValue: String = "",
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .padding(16.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Secondary,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            fontSize = 12.sp,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
        )
        if (subValue.isNotEmpty()) {
            Text(
                text = subValue,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
fun HikeDetailStatsRow(
    durationFormatted: String,
    distanceFormatted: String,
    durationIcon: ImageVector,
    distanceIcon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HikeDetailStatCard(
            icon = durationIcon,
            label = "Duration",
            value = durationFormatted,
            modifier = Modifier.weight(1f),
        )
        HikeDetailStatCard(
            icon = distanceIcon,
            label = "Distance",
            value = distanceFormatted,
            modifier = Modifier.weight(1f),
        )
    }
}

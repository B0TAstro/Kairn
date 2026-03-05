package com.example.kairn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairn.domain.model.HikeDifficulty
import com.example.kairn.ui.theme.ChipBackground
import com.example.kairn.ui.theme.TextPrimary
import com.example.kairn.ui.theme.TextSecondary

@Composable
fun HikeStatItem(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    iconTint: Color = TextSecondary,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            fontSize = 14.sp,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            fontSize = 11.sp,
        )
    }
}

@Composable
fun DifficultyBadge(
    difficulty: HikeDifficulty,
    modifier: Modifier = Modifier,
) {
    val color = when (difficulty) {
        HikeDifficulty.EASY -> Color(0xFF66bb6a)
        HikeDifficulty.MODERATE -> Color(0xFFffa726)
        HikeDifficulty.HARD -> Color(0xFFef5350)
        HikeDifficulty.EXPERT -> Color(0xFFab47bc)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(
            text = difficulty.label,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontSize = 12.sp,
        )
    }
}

@Composable
fun HikeStatRow(
    durationFormatted: String,
    distanceFormatted: String,
    elevationFormatted: String,
    difficulty: HikeDifficulty,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(ChipBackground.copy(alpha = 0.7f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatChip(label = durationFormatted, icon = "\u23F1")
        StatChip(label = distanceFormatted, icon = "\uD83D\uDCCF")
        StatChip(label = elevationFormatted, icon = "\u26F0")
        DifficultyBadge(difficulty = difficulty)
    }
}

@Composable
private fun StatChip(
    label: String,
    icon: String,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            text = icon,
            fontSize = 14.sp,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary,
            fontSize = 12.sp,
        )
    }
}

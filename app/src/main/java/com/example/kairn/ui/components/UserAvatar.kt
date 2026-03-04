package com.example.kairn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairn.ui.theme.Background
import com.example.kairn.ui.theme.OnlineGreen
import com.example.kairn.ui.theme.Primary
import com.example.kairn.ui.theme.TextPrimary

@Composable
fun UserAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    showOnlineIndicator: Boolean = false,
) {
    Box(modifier = modifier) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Primary),
        ) {
            Text(
                text = initials.take(2).uppercase(),
                color = TextPrimary,
                fontSize = (size.value / 2.5).sp,
                fontWeight = FontWeight.Medium,
            )
        }
        if (showOnlineIndicator) {
            Box(
                modifier = Modifier
                    .size(size / 3.5f)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-1).dp, y = (-1).dp)
                    .clip(CircleShape)
                    .background(OnlineGreen)
                    .border(2.dp, Background, CircleShape),
            )
        }
    }
}

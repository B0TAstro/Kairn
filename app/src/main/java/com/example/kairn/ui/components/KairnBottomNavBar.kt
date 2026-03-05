package com.example.kairn.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeChild

data class NavBarItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun KairnBottomNavBar(
    items: List<NavBarItem>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(50.dp)
    val hazeBackgroundColor = MaterialTheme.colorScheme.onBackground

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(shape)
                .hazeChild(
                    state = hazeState,
                    style = HazeStyle(
                        backgroundColor = hazeBackgroundColor.copy(alpha = 0.55f),
                        blurRadius = 24.dp,
                        tints = listOf(
                            HazeTint(
                                color = hazeBackgroundColor.copy(alpha = 0.3f),
                            ),
                        ),
                        noiseFactor = 0.03f,
                    ),
                )
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                NavBarItemPill(
                    item = item,
                    isSelected = selectedItem == item.id,
                    onClick = { onItemSelected(item.id) },
                )
            }
        }
    }
}

@Composable
private fun NavBarItemPill(
    item: NavBarItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }

    val backgroundColor = MaterialTheme.colorScheme.background
    val accentColor = MaterialTheme.colorScheme.primaryContainer

    val pillColor by animateColorAsState(
        targetValue = if (isSelected) backgroundColor.copy(alpha = 0.65f) else Color.Transparent,
        animationSpec = tween(durationMillis = 350),
        label = "pillColor",
    )

    val horizontalPadding by animateDpAsState(
        targetValue = if (isSelected) 20.dp else 16.dp,
        animationSpec = tween(durationMillis = 350),
        label = "horizontalPadding",
    )

    val iconTint by animateColorAsState(
        targetValue = if (isSelected) accentColor else Color.White.copy(alpha = 0.55f),
        animationSpec = tween(durationMillis = 300),
        label = "iconTint",
    )

    val textAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = if (isSelected) 350 else 150),
        label = "textAlpha",
    )

    val textWidth by animateDpAsState(
        targetValue = if (isSelected) 56.dp else 0.dp,
        animationSpec = tween(durationMillis = 350),
        label = "textWidth",
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .drawBehind { drawRect(pillColor) }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = horizontalPadding, vertical = 12.dp)
            .height(22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = iconTint,
            modifier = Modifier.size(22.dp),
        )

        if (textWidth > 0.dp) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier.width(textWidth),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.3.sp,
                    ),
                    color = accentColor.copy(alpha = textAlpha),
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

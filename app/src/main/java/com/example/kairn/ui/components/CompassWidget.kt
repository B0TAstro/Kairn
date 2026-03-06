package com.example.kairn.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kairn.ui.theme.CompassNorth
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CompassWidget(
    degrees: Float,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(200.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.background),
    ) {
        Canvas(modifier = Modifier.size(180.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2

            // Outer circle
            drawCircle(
                color = primaryColor.copy(alpha = 0.3f),
                radius = radius,
                center = center,
                style = Stroke(width = 2f),
            )

            // Inner circle
            drawCircle(
                color = primaryColor.copy(alpha = 0.2f),
                radius = radius * 0.7f,
                center = center,
                style = Stroke(width = 1f),
            )

            // Tick marks
            val tickCount = 72
            for (i in 0 until tickCount) {
                val angle = Math.toRadians((i * 360.0 / tickCount) - 90)
                val isCardinal = i % 18 == 0
                val isMajor = i % 9 == 0
                val startRadius = if (isCardinal) radius * 0.78f
                else if (isMajor) radius * 0.82f
                else radius * 0.88f
                val endRadius = radius * 0.92f
                val tickColor = if (isCardinal) onBackgroundColor
                else if (isMajor) onSurfaceVariantColor
                else onSurfaceVariantColor.copy(alpha = 0.4f)

                drawLine(
                    color = tickColor,
                    start = Offset(
                        x = center.x + (startRadius * cos(angle)).toFloat(),
                        y = center.y + (startRadius * sin(angle)).toFloat(),
                    ),
                    end = Offset(
                        x = center.x + (endRadius * cos(angle)).toFloat(),
                        y = center.y + (endRadius * sin(angle)).toFloat(),
                    ),
                    strokeWidth = if (isCardinal) 2f else 1f,
                    cap = StrokeCap.Round,
                )
            }

            // North indicator (red line)
            val northAngle = Math.toRadians(-90.0)
            val northEnd = Offset(
                x = center.x + (radius * 0.6f * cos(northAngle)).toFloat(),
                y = center.y + (radius * 0.6f * sin(northAngle)).toFloat(),
            )
            drawLine(
                color = CompassNorth,
                start = center,
                end = northEnd,
                strokeWidth = 3f,
                cap = StrokeCap.Round,
            )
        }

        // Cardinal letters
        Text(
            text = "N",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = CompassNorth,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp),
        )
        Text(
            text = "S",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
        )
        Text(
            text = "E",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
        )
        Text(
            text = "O",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 12.dp),
        )

        // Degree text in center
        Text(
            text = "${degrees.toInt()}\u00B0",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

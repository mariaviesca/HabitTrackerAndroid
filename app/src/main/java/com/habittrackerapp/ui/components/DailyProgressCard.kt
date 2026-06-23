package com.habittrackerapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DailyProgressCard(completed: Int, total: Int, modifier: Modifier = Modifier) {
    val progress = if (total > 0) completed.toFloat() / total else 0f
    val remaining = total - completed

    val headline = when {
        progress >= 1f -> "Perfect day! 🎉"
        progress >= 0.75f -> "Almost there!"
        progress >= 0.4f -> "Halfway hero!"
        progress > 0f -> "Nice start!"
        else -> "Ready when you are"
    }

    val subline = when {
        progress >= 1f -> "Every habit done — amazing work ✨"
        progress > 0f -> if (remaining == 1) "1 more habit and today is perfect ✨"
        else "$remaining more habits and today is perfect ✨"
        else -> "Check off your first habit to get rolling"
    }

    val accent = when {
        progress >= 1f -> Color(0xFF34C759)
        progress >= 0.5f -> Color(0xFFFF2D55)
        else -> Color(0xFFFF9500)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = accent.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(54.dp)) {
                Canvas(modifier = Modifier.size(54.dp)) {
                    val strokeWidth = 6.dp.toPx()
                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    drawArc(
                        color = accent.copy(alpha = 0.25f),
                        startAngle = 0f, sweepAngle = 360f, useCenter = false,
                        topLeft = topLeft, size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = accent,
                        startAngle = -90f, sweepAngle = 360f * progress, useCenter = false,
                        topLeft = topLeft, size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "$completed/$total",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    color = accent
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = headline,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = accent
                )
                Text(
                    text = subline,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

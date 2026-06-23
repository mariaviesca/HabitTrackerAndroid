package com.habittrackerapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.habittrackerapp.model.Habit
import com.habittrackerapp.util.parseHexColor
import java.time.LocalDate

@Composable
fun HeatmapCard(habit: Habit, modifier: Modifier = Modifier) {
    val columns = 13
    val rows = 7
    val today = LocalDate.now()
    val color = parseHexColor(habit.colorHex)

    val dates = (0 until columns).map { col ->
        (0 until rows).map { row ->
            val offset = (columns - 1 - col) * rows + (rows - 1 - row)
            today.minusDays(offset.toLong())
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Last 3 Months", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                dates.forEach { column ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        column.forEach { date ->
                            val cellColor = when {
                                habit.isCompleted(date) -> color
                                habit.isFrozen(date) -> Color(0xFF5AC8FA).copy(alpha = 0.4f)
                                else -> MaterialTheme.colorScheme.surfaceContainerHighest
                            }
                            val alpha = if (habit.isActive(date)) 1f else 0.3f
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(cellColor.copy(alpha = cellColor.alpha * alpha))
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Less", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                listOf(0.2f, 0.5f, 0.8f, 1.0f).forEach { opacity ->
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color.copy(alpha = opacity))
                    )
                }
                Text("More", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

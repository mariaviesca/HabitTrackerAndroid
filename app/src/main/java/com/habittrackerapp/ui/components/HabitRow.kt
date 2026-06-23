package com.habittrackerapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habittrackerapp.model.Habit
import com.habittrackerapp.util.parseHexColor
import java.time.LocalDate

@Composable
fun HabitRow(
    habit: Habit,
    date: LocalDate,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = habit.isCompleted(date)
    val isDaily = habit.frequency.isDaily
    val color = parseHexColor(habit.colorHex)
    val weeklyDone = habit.completionsThisWeek()
    val weeklyTarget = habit.frequency.weeklyTarget

    val bgColor = when {
        isCompleted -> color.copy(alpha = 0.18f)
        habit.isAtRisk -> Color.Red.copy(alpha = 0.07f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = when {
            habit.isAtRisk && !isCompleted -> BorderStroke(1.dp, Color.Red.copy(alpha = 0.35f))
            else -> null
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isCompleted) MaterialTheme.colorScheme.surface else color.copy(alpha = 0.15f))
            ) {
                Text(habit.icon, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))

            // Name + chips
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = if (isCompleted) color else MaterialTheme.colorScheme.onSurface
                    )
                    if (habit.isAtRisk) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (isDaily) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (habit.currentStreak > 0) {
                            ChipLabel(
                                text = "🔥 ${habit.currentStreak} day${if (habit.currentStreak == 1) "" else "s"}",
                                chipColor = Color(0xFFFF9500)
                            )
                        }
                        if (habit.streakFreezesAvailable > 0) {
                            ChipLabel(
                                text = "❄️ ${habit.streakFreezesAvailable} freeze${if (habit.streakFreezesAvailable == 1) "" else "s"}",
                                chipColor = Color(0xFF5AC8FA)
                            )
                        }
                    }
                } else {
                    WeeklyMiniProgress(done = weeklyDone, target = weeklyTarget, color = color)
                }
            }

            // Check button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clickable(onClick = onToggle)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isCompleted) color else Color.Transparent)
                        .then(
                            if (!isCompleted) Modifier.border(2.dp, Color.Gray, CircleShape)
                            else Modifier
                        )
                ) {
                    if (isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Done",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipLabel(text: String, chipColor: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = chipColor.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = chipColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun WeeklyMiniProgress(done: Int, target: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(target) { i ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (i < done) color else color.copy(alpha = 0.25f))
            )
        }
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = "$done of $target this week",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

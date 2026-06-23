package com.habittrackerapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habittrackerapp.model.Habit
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

enum class DayStatus { ALL_DONE, FROZEN, PARTIAL, NONE }

fun computeDayStatus(habits: List<Habit>, day: LocalDate): DayStatus {
    val active = habits.filter { it.frequency.isDaily && it.isActive(day) }
    if (active.isEmpty()) return DayStatus.NONE
    if (active.all { it.isCompleted(day) }) return DayStatus.ALL_DONE
    if (active.any { it.isFrozen(day) }) return DayStatus.FROZEN
    if (active.any { it.isCompleted(day) }) return DayStatus.PARTIAL
    return DayStatus.NONE
}

@Composable
fun DateScrollerRow(
    habits: List<Habit>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val days = (-13..0).map { today.plusDays(it.toLong()) }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        listState.scrollToItem(days.size - 1)
    }

    LazyRow(
        state = listState,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(days) { day ->
            val isSelected = day == selectedDate
            val isToday = day == today
            val status = computeDayStatus(habits, day)

            val fillColor = when {
                isSelected -> MaterialTheme.colorScheme.primary
                status == DayStatus.ALL_DONE -> Color(0xFF34C759).copy(alpha = 0.25f)
                status == DayStatus.FROZEN -> Color(0xFF5AC8FA).copy(alpha = 0.25f)
                status == DayStatus.PARTIAL -> Color(0xFFFF9500).copy(alpha = 0.22f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }

            val numberColor = when {
                isSelected -> Color.White
                status == DayStatus.ALL_DONE -> Color(0xFF34C759)
                status == DayStatus.FROZEN -> Color(0xFF5AC8FA)
                status == DayStatus.PARTIAL -> Color(0xFFFF9500)
                else -> MaterialTheme.colorScheme.onSurface
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(40.dp)
                    .clickable { onDateSelected(day) }
            ) {
                Text(
                    text = day.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isToday) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(5.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(fillColor)
                        .then(
                            if (isToday && !isSelected)
                                Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            else Modifier
                        )
                ) {
                    if (status == DayStatus.FROZEN && !isSelected) {
                        Text("❄️", fontSize = 14.sp)
                    } else {
                        Text(
                            text = "${day.dayOfMonth}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = numberColor
                        )
                    }
                }
            }
        }
    }
}

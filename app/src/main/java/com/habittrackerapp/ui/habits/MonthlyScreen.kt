package com.habittrackerapp.ui.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habittrackerapp.viewmodel.HabitStore
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyScreen(store: HabitStore, onReportClick: () -> Unit) {
    var displayedMonth by remember { mutableStateOf(YearMonth.now()) }
    val today = LocalDate.now()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly View") },
                actions = {
                    IconButton(onClick = onReportClick) {
                        Icon(Icons.Default.Description, contentDescription = "Report")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Month navigator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { displayedMonth = displayedMonth.minusMonths(1) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                }
                Text(
                    text = displayedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                val canGoForward = displayedMonth.isBefore(YearMonth.now())
                IconButton(onClick = {
                    if (canGoForward) displayedMonth = displayedMonth.plusMonths(1)
                }, enabled = canGoForward) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                }
            }

            // Calendar grid
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Weekday headers
                    val wf = WeekFields.of(Locale.getDefault())
                    val weekDays = DayOfWeek.entries.let { days ->
                        val firstDay = wf.firstDayOfWeek
                        val idx = days.indexOf(firstDay)
                        days.subList(idx, days.size) + days.subList(0, idx)
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        weekDays.forEach { day ->
                            Text(
                                text = day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    // Day cells
                    val firstOfMonth = displayedMonth.atDay(1)
                    val firstDayOfWeek = wf.firstDayOfWeek
                    val leadingBlanks = (firstOfMonth.dayOfWeek.value - firstDayOfWeek.value + 7) % 7
                    val daysInMonth = displayedMonth.lengthOfMonth()
                    val totalCells = leadingBlanks + daysInMonth
                    val rows = (totalCells + 6) / 7

                    for (row in 0 until rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (col in 0 until 7) {
                                val cellIndex = row * 7 + col
                                val dayNum = cellIndex - leadingBlanks + 1
                                if (dayNum in 1..daysInMonth) {
                                    val date = displayedMonth.atDay(dayNum)
                                    val status = dayStatus(store, date, today)
                                    val isToday = date == today

                                    val bgColor = when (status) {
                                        DStatus.ALL_DONE -> Color(0xFF34C759)
                                        DStatus.PARTIAL -> Color(0xFFFF9500)
                                        DStatus.NONE -> MaterialTheme.colorScheme.surfaceContainerHighest
                                        DStatus.FUTURE -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f)
                                    }
                                    val textColor = when (status) {
                                        DStatus.ALL_DONE, DStatus.PARTIAL -> Color.White
                                        DStatus.FUTURE -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }

                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(bgColor)
                                            .then(
                                                if (isToday) Modifier.border(
                                                    2.dp,
                                                    MaterialTheme.colorScheme.primary,
                                                    RoundedCornerShape(10.dp)
                                                ) else Modifier
                                            )
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                "$dayNum",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium
                                                ),
                                                color = textColor,
                                                fontSize = 15.sp
                                            )
                                            if (status == DStatus.ALL_DONE) {
                                                Text("✓", fontSize = 8.sp, color = Color.White)
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }

            // Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                LegendItem(Color(0xFF34C759), "All done")
                LegendItem(Color(0xFFFF9500), "Partial")
                LegendItem(MaterialTheme.colorScheme.surfaceContainerHighest, "None / future")
            }

            // Monthly summary
            MonthlySummaryCard(store, displayedMonth)
        }
    }
}

private enum class DStatus { ALL_DONE, PARTIAL, NONE, FUTURE }

private fun dayStatus(store: HabitStore, date: LocalDate, today: LocalDate): DStatus {
    if (date.isAfter(today)) return DStatus.FUTURE
    val relevant = store.habits.filter { it.isActive(date) }
    if (relevant.isEmpty()) return DStatus.NONE
    val completed = relevant.count { it.isCompleted(date) }
    return when {
        completed == 0 -> DStatus.NONE
        completed == relevant.size -> DStatus.ALL_DONE
        else -> DStatus.PARTIAL
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MonthlySummaryCard(store: HabitStore, month: YearMonth) {
    val today = LocalDate.now()
    val daysInMonth = month.lengthOfMonth()
    val firstDay = month.atDay(1)
    val passedDays = (1..daysInMonth).map { month.atDay(it) }.filter { !it.isAfter(today) }

    val perfectDays = passedDays.count { day ->
        val active = store.habits.filter { it.isActive(day) }
        active.isNotEmpty() && active.all { it.isCompleted(day) }
    }

    val totalCheckIns = store.habits.flatMap { it.entries }.count { entry ->
        entry.completed && !entry.date.isBefore(firstDay) && entry.date.isBefore(firstDay.plusMonths(1))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Month Summary", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(12.dp))
            Row {
                SummaryPill("$perfectDays", "Perfect days", Color(0xFF34C759), Modifier.weight(1f))
                SummaryPill("${passedDays.size}", "Days tracked", Color(0xFF007AFF), Modifier.weight(1f))
                SummaryPill("$totalCheckIns", "Total check-ins", Color(0xFFAF52DE), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SummaryPill(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

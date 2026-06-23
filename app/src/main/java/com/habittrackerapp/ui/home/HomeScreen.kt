package com.habittrackerapp.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habittrackerapp.model.Habit
import com.habittrackerapp.ui.components.*
import com.habittrackerapp.util.parseHexColor
import com.habittrackerapp.viewmodel.HabitStore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    store: HabitStore,
    onAddHabit: () -> Unit,
    onHabitClick: (Habit) -> Unit,
    onScoreClick: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showConfetti by remember { mutableStateOf(false) }

    val allHabits = store.habits.filter { it.isActive(selectedDate) }
    val dailyHabits = allHabits.filter { it.frequency.isDaily }
    val completedToday = dailyHabits.count { it.isCompleted(selectedDate) }
    val isDailyAllDone = dailyHabits.isNotEmpty() && completedToday == dailyHabits.size
    val atRiskHabits = allHabits.filter { it.isAtRisk }

    val prevCompleted = remember { mutableIntStateOf(completedToday) }
    LaunchedEffect(completedToday) {
        if (completedToday == dailyHabits.size && completedToday > 0 && completedToday > prevCompleted.intValue) {
            showConfetti = true
        }
        prevCompleted.intValue = completedToday
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    actions = {
                        IconButton(onClick = onAddHabit) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Add Habit")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 8.dp)
            ) {
                // Greeting header
                GreetingHeader(store = store, onScoreClick = onScoreClick)

                Spacer(modifier = Modifier.height(16.dp))
                QuoteCard(modifier = Modifier.padding(horizontal = 16.dp))

                Spacer(modifier = Modifier.height(16.dp))
                DateScrollerRow(
                    habits = store.habits,
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (store.habits.isEmpty()) {
                    EmptyState(onAddHabit = onAddHabit)
                } else {
                    DailyProgressCard(
                        completed = completedToday,
                        total = dailyHabits.size,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    if (atRiskHabits.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        AtRiskBanner(habits = atRiskHabits, modifier = Modifier.padding(horizontal = 16.dp))
                    }

                    if (isDailyAllDone) {
                        Spacer(modifier = Modifier.height(12.dp))
                        AllDoneBanner(modifier = Modifier.padding(horizontal = 16.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Grouped by category
                    val grouped = store.categories.mapNotNull { cat ->
                        val habits = store.habitsForCategory(cat).filter { it.isActive(selectedDate) }
                        if (habits.isEmpty()) null else cat to habits
                    }

                    grouped.forEach { (category, habits) ->
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColor(category.colorHex))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        habits.forEach { habit ->
                            HabitRow(
                                habit = habit,
                                date = selectedDate,
                                onToggle = { store.toggleCompletion(habit, selectedDate) },
                                onClick = { onHabitClick(habit) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        ConfettiEffect(trigger = showConfetti, onFinished = { showConfetti = false })
    }
}

@Composable
private fun GreetingHeader(store: HabitStore, onScoreClick: () -> Unit) {
    val hour = java.time.LocalTime.now().hour
    val greeting = when (hour) {
        in 5..11 -> "Good morning ☀️"
        in 12..17 -> "Good afternoon 🌤️"
        else -> "Good evening 🌙"
    }
    val dateText = LocalDate.now().format(
        DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dateText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = greeting,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
        Surface(
            modifier = Modifier.clickable(onClick = onScoreClick),
            shape = RoundedCornerShape(50),
            color = store.scoreColor.copy(alpha = 0.15f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = store.scoreGrade,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = store.scoreColor
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "${store.habitScore} pts",
                    style = MaterialTheme.typography.bodySmall,
                    color = store.scoreColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun AtRiskBanner(habits: List<Habit>, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.Red.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.35f))
    ) {
        Row(modifier = Modifier.padding(14.dp)) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    "You're falling behind!",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Red
                )
                Text(
                    habits.joinToString(", ") { "${it.icon} ${it.name}" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun AllDoneBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF34C759).copy(alpha = 0.12f)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🎉", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    "All done for today!",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    "Amazing work — keep the streak alive!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyState(onAddHabit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("✅", fontSize = 60.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No habits yet", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Start building better habits by adding your first one.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAddHabit) {
            Text("Add Habit")
        }
    }
}

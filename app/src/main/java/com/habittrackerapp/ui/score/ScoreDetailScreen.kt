package com.habittrackerapp.ui.score

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habittrackerapp.viewmodel.HabitStore
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreDetailScreen(store: HabitStore, onDone: () -> Unit) {
    val gradeDescription = when (store.scoreGrade) {
        "S" -> "Outstanding! You're crushing your habits. 🏆"
        "A" -> "Excellent consistency. Keep it up! 💪"
        "B" -> "Good progress. A few more check-ins to reach A. 📈"
        "C" -> "You're getting there. Don't miss your weekly goals! ⚠️"
        "D" -> "Struggling a bit. Focus on at least one habit daily. 😬"
        else -> "Your score is negative. Time to get back on track! 💀"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Habit Score") },
                actions = {
                    TextButton(onClick = onDone) { Text("Done", fontWeight = FontWeight.Bold) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Big score display
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(store.scoreColor.copy(alpha = 0.15f))
            ) {
                Text(
                    store.scoreGrade,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    color = store.scoreColor
                )
            }
            Text(
                "${store.habitScore} points",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = store.scoreColor
            )
            Text(
                gradeDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // Grade scale
            GradeScale(store.habitScore)

            // How points work
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("How points work", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    RuleRow(Icons.Default.CheckCircle, Color(0xFF34C759), "+1 point", "Every time you check off a habit")
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                    RuleRow(Icons.Default.Close, Color(0xFFFF9500), "−1 point", "If you undo a check-off")
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                    RuleRow(Icons.Default.Warning, Color.Red, "−15 / −25 / −40 points", "When a weekly habit goal is missed — penalty escalates each consecutive missed week")
                }
            }

            // Penalty explanation
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.Red.copy(alpha = 0.06f),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Red.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("About the missed-week penalty", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    PenaltyStep("1", "You set a habit to repeat X times per week — for example the gym 4× a week.")
                    PenaltyStep("2", "Every Monday, the app checks if you hit your goal the week before.")
                    PenaltyStep("3", "If you fell short — even by just one session — 15 points are deducted. Miss the same habit two weeks in a row and it costs 25, three or more in a row costs 40.")
                    PenaltyStep("4", "Daily habits don't trigger this penalty. They only affect your streak — and streak freezes (earned at 20, 50, then every 40 streak days) auto-cover a missed day.")
                }
            }

            // Missed weeks
            if (store.missedWeeks.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Penalties this session (${store.missedWeeks.size})", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        store.missedWeeks.forEach { missed ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(missed.habitIcon, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(missed.habitName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                                    Text(
                                        "Week of ${missed.weekStarting.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text("−${missed.points}", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color.Red)
                            }
                        }
                    }
                }
            }

            // Stats
            val totalCheckIns = store.habits.flatMap { it.entries }.count { it.completed }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Your stats", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        MiniStat("$totalCheckIns", "Total check-ins", Color(0xFF34C759), Modifier.weight(1f))
                        MiniStat("${store.missedWeeks.size}", "Penalties", Color.Red, Modifier.weight(1f))
                        MiniStat("${store.habits.size}", "Habits", Color(0xFF007AFF), Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GradeScale(currentScore: Int) {
    val grades = listOf(
        Triple("S", 150, Color(0xFF34C759)),
        Triple("A", 120, Color(0xFF007AFF)),
        Triple("B", 90, Color(0xFF5856D6)),
        Triple("C", 60, Color(0xFFFF9500)),
        Triple("D", 0, Color.Red),
        Triple("F", Int.MIN_VALUE, Color.Red),
    )

    fun isCurrentGrade(grade: String) = when (currentScore) {
        in 150..Int.MAX_VALUE -> grade == "S"
        in 120..149 -> grade == "A"
        in 90..119 -> grade == "B"
        in 60..89 -> grade == "C"
        in 0..59 -> grade == "D"
        else -> grade == "F"
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Grade Scale", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                grades.forEach { (grade, threshold, color) ->
                    val isCurrent = isCurrentGrade(grade)
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(color.copy(alpha = if (isCurrent) 0.25f else 0.1f))
                                .then(
                                    if (isCurrent) Modifier.border(2.dp, color, RoundedCornerShape(8.dp))
                                    else Modifier
                                )
                        ) {
                            Text(grade, fontWeight = FontWeight.Bold, color = color, fontSize = 16.sp)
                        }
                        Text(
                            if (threshold == Int.MIN_VALUE) "< 0" else "$threshold+",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleRow(icon: ImageVector, iconColor: Color, title: String, subtitle: String) {
    Row(
        modifier = Modifier.padding(horizontal = 0.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PenaltyStep(step: String, text: String) {
    Row(modifier = Modifier.padding(vertical = 5.dp)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.Red)
        ) {
            Text(step, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun MiniStat(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

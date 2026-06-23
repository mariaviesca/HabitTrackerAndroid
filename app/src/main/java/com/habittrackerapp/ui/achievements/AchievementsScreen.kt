@file:OptIn(ExperimentalLayoutApi::class)
package com.habittrackerapp.ui.achievements

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habittrackerapp.model.Achievement
import com.habittrackerapp.model.MissedWeek
import com.habittrackerapp.viewmodel.HabitStore
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AchievementsScreen(store: HabitStore) {
    val unlocked = store.achievements.filter { it.isUnlocked }
    val locked = store.achievements.filter { !it.isUnlocked }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Achievements") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Summary
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(modifier = Modifier.padding(vertical = 14.dp)) {
                    StatPill("${unlocked.size}", "Unlocked", Color(0xFFFFCC00), Modifier.weight(1f))
                    StatPill("${locked.size}", "Remaining", MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f))
                    StatPill("${store.missedWeeks.size}", "Penalties", Color.Red, Modifier.weight(1f))
                }
            }

            // Missed weeks
            if (store.missedWeeks.isNotEmpty()) {
                MissedWeekSection(store.missedWeeks)
            }

            // Unlocked
            if (unlocked.isNotEmpty()) {
                AchievementSection("🏆 Unlocked", unlocked, isLocked = false)
            }

            // Locked
            AchievementSection("🔒 Locked", locked, isLocked = true)
        }
    }
}

@Composable
private fun StatPill(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = color)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AchievementSection(title: String, achievements: List<Achievement>, isLocked: Boolean) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            achievements.forEach { achievement ->
                AchievementCard(achievement, isLocked, Modifier.width(160.dp))
            }
        }
    }
}

@Composable
private fun AchievementCard(achievement: Achievement, isLocked: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.then(
            if (!isLocked) Modifier.border(1.5.dp, Color(0xFFFFCC00).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            else Modifier
        ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                Text(
                    achievement.icon,
                    fontSize = 36.sp,
                    modifier = Modifier.then(if (isLocked) Modifier else Modifier)
                )
                if (isLocked) {
                    Text("🔒", fontSize = 12.sp, modifier = Modifier.align(Alignment.TopEnd))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                achievement.title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                achievement.description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (achievement.unlockedAt != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    achievement.unlockedAt.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = Color(0xFFFFCC00)
                )
            }
        }
    }
}

@Composable
private fun MissedWeekSection(missedWeeks: List<MissedWeek>) {
    Column {
        Text(
            "💀 Penalty Record",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            missedWeeks.forEach { missed ->
                Surface(
                    modifier = Modifier
                        .width(160.dp)
                        .border(1.5.dp, Color.Red.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Red.copy(alpha = 0.07f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(missed.habitIcon, fontSize = 30.sp)
                        Text(
                            "Missed Week",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.Red
                        )
                        Text(
                            missed.habitName,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Wk of ${missed.weekStarting.format(DateTimeFormatter.ofPattern("MMM d"))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "−${missed.points} pts",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.Red
                        )
                    }
                }
            }
        }
    }
}

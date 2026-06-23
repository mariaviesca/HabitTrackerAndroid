@file:OptIn(ExperimentalLayoutApi::class)
package com.habittrackerapp.ui.habits

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habittrackerapp.model.*
import com.habittrackerapp.util.parseHexColor
import com.habittrackerapp.viewmodel.HabitStore

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddHabitScreen(
    store: HabitStore,
    editingHabitId: String? = null,
    onDone: () -> Unit
) {
    val editingHabit = editingHabitId?.let { id -> store.habits.firstOrNull { it.id == id } }
    val isEditing = editingHabit != null

    var name by remember { mutableStateOf(editingHabit?.name ?: "") }
    var selectedIcon by remember { mutableStateOf(editingHabit?.icon ?: "🎯") }
    var selectedColorHex by remember { mutableStateOf(editingHabit?.colorHex ?: "#007AFF") }
    var selectedCategoryID by remember {
        mutableStateOf(editingHabit?.categoryID ?: store.categories.firstOrNull()?.id ?: "")
    }
    var isDaily by remember {
        mutableStateOf(editingHabit?.frequency?.isDaily ?: true)
    }
    var timesPerWeek by remember {
        mutableIntStateOf(
            if (editingHabit != null && !editingHabit.frequency.isDaily) editingHabit.frequency.value else 3
        )
    }
    var reminderEnabled by remember { mutableStateOf(editingHabit?.hasReminder ?: false) }
    var reminderHour by remember { mutableIntStateOf(editingHabit?.reminderHour?.takeIf { it >= 0 } ?: 9) }
    var reminderMinute by remember { mutableIntStateOf(editingHabit?.reminderMinute?.takeIf { it >= 0 } ?: 0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Habit" else "New Habit") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val trimmed = name.trim()
                            if (trimmed.isEmpty()) return@TextButton
                            val freq = if (isDaily) HabitFrequency.Daily else HabitFrequency.timesPerWeek(timesPerWeek)
                            if (editingHabit != null) {
                                store.updateHabit(
                                    editingHabit.copy(
                                        name = trimmed, icon = selectedIcon,
                                        colorHex = selectedColorHex,
                                        categoryID = selectedCategoryID,
                                        frequency = freq,
                                        reminderHour = if (reminderEnabled) reminderHour else -1,
                                        reminderMinute = if (reminderEnabled) reminderMinute else -1
                                    )
                                )
                            } else {
                                store.addHabit(
                                    Habit(
                                        name = trimmed, categoryID = selectedCategoryID,
                                        colorHex = selectedColorHex, icon = selectedIcon,
                                        frequency = freq,
                                        reminderHour = if (reminderEnabled) reminderHour else -1,
                                        reminderMinute = if (reminderEnabled) reminderMinute else -1
                                    )
                                )
                            }
                            onDone()
                        },
                        enabled = name.trim().isNotEmpty()
                    ) {
                        Text(if (isEditing) "Save" else "Add", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                placeholder = { Text("e.g. Morning run, Read 10 pages…") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Icon
            Text("Icon", style = MaterialTheme.typography.titleSmall)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                habitIcons.forEach { icon ->
                    val isSelected = selectedIcon == icon
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) parseHexColor(selectedColorHex).copy(alpha = 0.2f)
                                else Color.Transparent
                            )
                            .then(
                                if (isSelected) Modifier.border(
                                    2.dp, parseHexColor(selectedColorHex), RoundedCornerShape(8.dp)
                                ) else Modifier
                            )
                            .clickable { selectedIcon = icon }
                    ) {
                        Text(icon, fontSize = 22.sp)
                    }
                }
            }

            // Color
            Text("Color", style = MaterialTheme.typography.titleSmall)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                habitColors.forEach { (_, hex) ->
                    val isSelected = selectedColorHex == hex
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(parseHexColor(hex))
                            .clickable { selectedColorHex = hex }
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Frequency
            Text("Frequency", style = MaterialTheme.typography.titleSmall)
            Row(modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = isDaily,
                    onClick = { isDaily = true },
                    label = { Text("Every day") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = !isDaily,
                    onClick = { isDaily = false },
                    label = { Text("X times per week") },
                    modifier = Modifier.weight(1f)
                )
            }
            if (!isDaily) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Goal", modifier = Modifier.weight(1f))
                    IconButton(onClick = { if (timesPerWeek > 1) timesPerWeek-- }) {
                        Text("−", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("${timesPerWeek}× per week", style = MaterialTheme.typography.bodyMedium)
                    IconButton(onClick = { if (timesPerWeek < 7) timesPerWeek++ }) {
                        Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Reminder
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Reminder", modifier = Modifier.weight(1f))
                Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
            }
            if (reminderEnabled) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Time: ", style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = {
                        reminderHour = (reminderHour - 1 + 24) % 24
                    }) { Text("◀") }
                    Text(
                        text = String.format("%02d:%02d", reminderHour, reminderMinute),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    TextButton(onClick = {
                        reminderHour = (reminderHour + 1) % 24
                    }) { Text("▶") }
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(onClick = {
                        reminderMinute = (reminderMinute - 15 + 60) % 60
                    }) { Text("◀") }
                    Text("min")
                    TextButton(onClick = {
                        reminderMinute = (reminderMinute + 15) % 60
                    }) { Text("▶") }
                }
            }

            // Category
            Text("Category", style = MaterialTheme.typography.titleSmall)
            store.categories.forEach { cat ->
                val isSelected = selectedCategoryID == cat.id
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCategoryID = cat.id },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(cat.colorHex))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(cat.name)
                        if (isSelected) {
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

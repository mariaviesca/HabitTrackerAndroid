@file:OptIn(ExperimentalLayoutApi::class)
package com.habittrackerapp.ui.habits

import android.app.TimePickerDialog
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

    val context = LocalContext.current
    val accentColor = parseHexColor(selectedColorHex)

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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                placeholder = { Text("e.g. Morning run, Read 10 pages…") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            // Icon
            SectionCard(title = "Icon") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    habitIcons.forEach { icon ->
                        val isSelected = selectedIcon == icon
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) accentColor.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceContainerHighest
                                )
                                .then(
                                    if (isSelected) Modifier.border(2.dp, accentColor, RoundedCornerShape(12.dp))
                                    else Modifier
                                )
                                .clickable { selectedIcon = icon }
                        ) {
                            Text(icon, fontSize = 22.sp)
                        }
                    }
                }
            }

            // Color
            SectionCard(title = "Color") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    habitColors.forEach { (_, hex) ->
                        val isSelected = selectedColorHex == hex
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(hex))
                                .then(
                                    if (isSelected) Modifier.border(3.dp, Color.White, CircleShape)
                                    else Modifier
                                )
                                .clickable { selectedColorHex = hex }
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Frequency
            SectionCard(title = "Frequency") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SegmentButton("Every day", isDaily, accentColor, Modifier.weight(1f)) { isDaily = true }
                    SegmentButton("X per week", !isDaily, accentColor, Modifier.weight(1f)) { isDaily = false }
                }
                if (!isDaily) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Goal", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (timesPerWeek > 1) timesPerWeek-- }) {
                                    Text("−", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = accentColor)
                                }
                                Text(
                                    "${timesPerWeek}×",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                IconButton(onClick = { if (timesPerWeek < 7) timesPerWeek++ }) {
                                    Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = accentColor)
                                }
                            }
                        }
                    }
                }
            }

            // Reminder
            SectionCard(title = "Reminder") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Daily reminder", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                }
                if (reminderEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    android.R.style.Theme_DeviceDefault_Dialog,
                                    { _, h, m -> reminderHour = h; reminderMinute = m },
                                    reminderHour, reminderMinute, false
                                ).show()
                            },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = String.format(
                                    "%d:%02d %s",
                                    if (reminderHour % 12 == 0) 12 else reminderHour % 12,
                                    reminderMinute,
                                    if (reminderHour < 12) "AM" else "PM"
                                ),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = accentColor
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                "Tap to change",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Category
            SectionCard(title = "Category") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    store.categories.forEach { cat ->
                        val isSelected = selectedCategoryID == cat.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCategoryID = cat.id },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) accentColor.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceContainerHighest,
                            border = if (isSelected) BorderStroke(1.5.dp, accentColor) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(parseHexColor(cat.colorHex))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(cat.name, style = MaterialTheme.typography.bodyMedium)
                                if (isSelected) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SegmentButton(label: String, selected: Boolean, accentColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) accentColor.copy(alpha = 0.2f)
        else MaterialTheme.colorScheme.surfaceContainerHighest,
        border = if (selected) BorderStroke(1.5.dp, accentColor) else null
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium),
                color = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

package com.habittrackerapp.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale
import java.util.UUID

data class HabitEntry(
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDate = LocalDate.now(),
    val completed: Boolean = false
)

data class Category(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val colorHex: String = "#8E8E93"
) {
    companion object {
        val defaults = listOf(
            Category(name = "Health", colorHex = "#34C759"),
            Category(name = "Fitness", colorHex = "#FF9500"),
            Category(name = "Mindfulness", colorHex = "#AF52DE"),
            Category(name = "Learning", colorHex = "#007AFF"),
            Category(name = "Productivity", colorHex = "#FF3B30"),
        )
    }
}

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val categoryID: String = "",
    val colorHex: String = "#007AFF",
    val icon: String = "🎯",
    val frequency: HabitFrequency = HabitFrequency.Daily,
    val createdAt: LocalDate = LocalDate.now(),
    val entries: List<HabitEntry> = emptyList(),
    val reminderHour: Int = -1,
    val reminderMinute: Int = -1,
    val streakFreezesAvailable: Int = 0,
    val freezesEarnedThisStreak: Int = 0,
    val frozenDates: List<LocalDate> = emptyList()
) {
    val hasReminder: Boolean get() = reminderHour >= 0

    fun isActive(on: LocalDate): Boolean = !on.isBefore(createdAt)

    fun activeDays(days: List<LocalDate>): List<LocalDate> = days.filter { isActive(it) }

    fun isCompleted(on: LocalDate): Boolean =
        entries.any { it.date == on && it.completed }

    fun isFrozen(on: LocalDate): Boolean = frozenDates.contains(on)

    fun completionsThisWeek(weekOf: LocalDate = LocalDate.now()): Int {
        val wf = WeekFields.of(Locale.getDefault())
        val weekStart = weekOf.with(wf.dayOfWeek(), 1)
        return (0 until 7).count { isCompleted(weekStart.plusDays(it.toLong())) }
    }

    fun metGoal(weekOf: LocalDate): Boolean =
        completionsThisWeek(weekOf) >= frequency.weeklyTarget

    val isAtRisk: Boolean
        get() {
            if (frequency.isDaily) return false
            val today = LocalDate.now()
            val dayOfWeek = today.dayOfWeek.value // 1=Mon..7=Sun
            if (dayOfWeek < 4) return false // Warn from Thursday onward
            val daysLeft = 7 - dayOfWeek + 1
            val needed = frequency.weeklyTarget - completionsThisWeek()
            return needed > 0 && needed >= daysLeft
        }

    val currentStreak: Int
        get() = when {
            frequency.isDaily -> {
                var streak = 0
                var date = LocalDate.now()
                if (!isCompleted(date) && !isFrozen(date)) {
                    date = date.minusDays(1)
                }
                while (isCompleted(date) || isFrozen(date)) {
                    if (isCompleted(date)) streak++
                    date = date.minusDays(1)
                }
                streak
            }
            else -> consecutiveWeeksStreak()
        }

    val longestStreak: Int
        get() {
            return when {
            frequency.isDaily -> {
                val completedDays = entries.filter { it.completed }.map { it.date }.toSet()
                val coveredDays = completedDays + frozenDates.toSet()
                if (coveredDays.isEmpty()) return 0
                val sorted = coveredDays.sorted()
                var longest = 0
                var current = 0
                var previous: LocalDate? = null
                for (day in sorted) {
                    if (previous != null && ChronoUnit.DAYS.between(previous, day) != 1L) {
                        current = 0
                    }
                    if (completedDays.contains(day)) current++
                    longest = maxOf(longest, current)
                    previous = day
                }
                longest
            }
            else -> longestWeekStreak()
        }
        }

    val streakLabel: String get() = if (frequency.isDaily) "day streak" else "week streak"

    val daysUntilNextFreeze: Int
        get() = freezeThreshold(freezesEarnedThisStreak) - currentStreak

    private fun consecutiveWeeksStreak(): Int {
        var streak = 0
        var weekAnchor = LocalDate.now()
        repeat(52) {
            if (metGoal(weekAnchor)) {
                streak++
                weekAnchor = weekAnchor.minusWeeks(1)
            } else return streak
        }
        return streak
    }

    private fun longestWeekStreak(): Int {
        if (entries.isEmpty()) return 0
        val oldest = entries.minByOrNull { it.date }?.date ?: return 0
        val weeksBack = ChronoUnit.WEEKS.between(oldest, LocalDate.now()).toInt()
        var longest = 0
        var current = 0
        var anchor = LocalDate.now()
        for (i in 0..weeksBack) {
            if (metGoal(anchor)) {
                current++
                longest = maxOf(longest, current)
            } else {
                current = 0
            }
            anchor = anchor.minusWeeks(1)
        }
        return longest
    }

    companion object {
        fun freezeThreshold(index: Int): Int = when (index) {
            0 -> 20
            1 -> 50
            else -> 90 + (index - 2) * 40
        }

        fun freezesEarnable(forStreak: Int): Int {
            var count = 0
            while (freezeThreshold(count) <= forStreak) count++
            return count
        }
    }
}

val habitColors = listOf(
    "Red" to "#FF3B30", "Orange" to "#FF9500", "Yellow" to "#FFCC00",
    "Green" to "#34C759", "Teal" to "#5AC8FA", "Blue" to "#007AFF",
    "Purple" to "#AF52DE", "Pink" to "#FF2D55", "Brown" to "#A2845E",
    "Gray" to "#8E8E93",
)

val habitIcons = listOf(
    "🏃", "💧", "📚", "🧘", "💪", "🥗", "😴", "✍️",
    "🎯", "🎨", "🎸", "🧹", "💊", "🌿", "☀️", "🧠",
)

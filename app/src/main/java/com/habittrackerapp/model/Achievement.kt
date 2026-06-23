package com.habittrackerapp.model

import java.time.LocalDate

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val unlockedAt: LocalDate? = null
) {
    val isUnlocked: Boolean get() = unlockedAt != null

    companion object {
        val all = listOf(
            Achievement("first_checkin", "First Step", "Complete a habit for the first time.", "👟"),
            Achievement("perfect_day", "Perfect Day", "Complete all habits in a single day.", "⭐️"),
            Achievement("perfect_week", "Perfect Week", "Complete all habits every day for 7 days.", "🗓️"),
            Achievement("streak_3", "3-Day Streak", "Keep a habit going for 3 days in a row.", "🔥"),
            Achievement("streak_7", "Week Warrior", "Keep a habit going for 7 days in a row.", "💪"),
            Achievement("streak_30", "Monthly Master", "Keep a habit going for 30 days in a row.", "🏅"),
            Achievement("streak_100", "Century Club", "Keep a habit going for 100 days in a row.", "💯"),
            Achievement("habits_3", "Habit Builder", "Track 3 different habits at once.", "🧱"),
            Achievement("habits_5", "Habit Machine", "Track 5 different habits at once.", "⚙️"),
            Achievement("entries_50", "Half Century", "Log 50 total habit completions.", "🎯"),
            Achievement("entries_100", "Centurion", "Log 100 total habit completions.", "🏆"),
            Achievement("entries_365", "Year of Effort", "Log 365 total habit completions.", "🌟"),
        )

        fun evaluate(habits: List<Habit>, existing: List<Achievement>): List<Achievement> {
            val updated = existing.toMutableList()

            fun unlock(id: String) {
                val idx = updated.indexOfFirst { it.id == id }
                if (idx >= 0 && !updated[idx].isUnlocked) {
                    updated[idx] = updated[idx].copy(unlockedAt = LocalDate.now())
                }
            }

            val totalEntries = habits.flatMap { it.entries }.count { it.completed }
            val maxStreak = habits.maxOfOrNull { it.currentStreak } ?: 0
            val today = LocalDate.now()

            if (totalEntries >= 1) unlock("first_checkin")
            if (maxStreak >= 3) unlock("streak_3")
            if (maxStreak >= 7) unlock("streak_7")
            if (maxStreak >= 30) unlock("streak_30")
            if (maxStreak >= 100) unlock("streak_100")
            if (habits.size >= 3) unlock("habits_3")
            if (habits.size >= 5) unlock("habits_5")
            if (totalEntries >= 50) unlock("entries_50")
            if (totalEntries >= 100) unlock("entries_100")
            if (totalEntries >= 365) unlock("entries_365")

            if (habits.isNotEmpty() && habits.all { it.isCompleted(today) }) {
                unlock("perfect_day")
            }

            val last7 = (0 until 7).map { today.minusDays(it.toLong()) }
            if (habits.isNotEmpty() && last7.all { day -> habits.all { it.isCompleted(day) } }) {
                unlock("perfect_week")
            }

            return updated
        }
    }
}

package com.habittrackerapp.model

data class HabitFrequency(
    val type: String = "daily",
    val value: Int = 7
) {
    val isDaily: Boolean get() = type == "daily"

    val weeklyTarget: Int
        get() = if (isDaily) 7 else value

    val displayText: String
        get() = if (isDaily) "Every day" else "${value}× per week"

    companion object {
        val Daily = HabitFrequency("daily", 7)
        fun timesPerWeek(n: Int) = HabitFrequency("timesPerWeek", n)
    }
}

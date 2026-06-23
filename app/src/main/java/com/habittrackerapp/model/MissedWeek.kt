package com.habittrackerapp.model

import java.time.LocalDate
import java.util.UUID

data class MissedWeek(
    val id: String = UUID.randomUUID().toString(),
    val habitID: String = "",
    val habitName: String = "",
    val habitIcon: String = "",
    val weekStarting: LocalDate = LocalDate.now(),
    val points: Int = 15
)

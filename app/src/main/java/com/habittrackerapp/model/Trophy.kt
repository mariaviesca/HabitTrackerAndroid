package com.habittrackerapp.model

import java.time.LocalDate
import java.util.UUID

data class MonthlyTrophy(
    val title: String = "",
    val icon: String = "🏝️",
    val targetDays: Int = 25
)

data class WonTrophy(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val icon: String = "",
    val month: LocalDate = LocalDate.now(),
    val wonAt: LocalDate = LocalDate.now()
)

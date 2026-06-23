package com.habittrackerapp.util

import androidx.compose.ui.graphics.Color

fun parseHexColor(hex: String): Color {
    val cleaned = hex.removePrefix("#")
    if (cleaned.length != 6) return Color(0xFF007AFF)
    val colorLong = cleaned.toLongOrNull(16) ?: return Color(0xFF007AFF)
    return Color(0xFF000000 or colorLong)
}

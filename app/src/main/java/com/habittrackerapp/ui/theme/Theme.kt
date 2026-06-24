package com.habittrackerapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    secondary = AccentPurple,
    tertiary = AccentPink,
    surface = androidx.compose.ui.graphics.Color(0xFF1C1C1E),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF2C2C2E),
    surfaceContainerHighest = androidx.compose.ui.graphics.Color(0xFF3A3A3C),
    background = androidx.compose.ui.graphics.Color(0xFF000000),
)

@Composable
fun HabitTrackerTheme(content: @Composable () -> Unit) {
    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        dynamicDarkColorScheme(context).copy(
            surface = androidx.compose.ui.graphics.Color(0xFF1C1C1E),
            surfaceVariant = androidx.compose.ui.graphics.Color(0xFF2C2C2E),
            background = androidx.compose.ui.graphics.Color(0xFF000000),
        )
    } else {
        DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}

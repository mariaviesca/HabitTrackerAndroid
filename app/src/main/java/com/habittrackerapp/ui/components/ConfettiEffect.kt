package com.habittrackerapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlin.math.sin
import kotlin.random.Random

data class ConfettiPiece(
    val x: Float,
    val speed: Float,
    val amplitude: Float,
    val color: Color,
    val size: Float,
    val phase: Float
)

@Composable
fun ConfettiEffect(trigger: Boolean, onFinished: () -> Unit) {
    if (!trigger) return

    val pieces = remember {
        val colors = listOf(
            Color(0xFFFF3B30), Color(0xFFFF9500), Color(0xFFFFCC00),
            Color(0xFF34C759), Color(0xFF007AFF), Color(0xFFAF52DE), Color(0xFFFF2D55)
        )
        (0 until 80).map {
            ConfettiPiece(
                x = Random.nextFloat(),
                speed = Random.nextFloat() * 300 + 200,
                amplitude = Random.nextFloat() * 40 + 10,
                color = colors.random(),
                size = Random.nextFloat() * 8 + 4,
                phase = Random.nextFloat() * 6.28f
            )
        }
    }

    val progress = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(3000, easing = LinearEasing))
        onFinished()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val height = size.height
        val width = size.width
        val t = progress.value

        pieces.forEach { piece ->
            val y = -50f + (height + 100f) * t * (piece.speed / 500f)
            val x = piece.x * width + sin(t * 10 + piece.phase) * piece.amplitude
            if (y in -50f..height + 50f) {
                drawRect(
                    color = piece.color.copy(alpha = (1f - t).coerceIn(0f, 1f)),
                    topLeft = Offset(x, y),
                    size = Size(piece.size, piece.size * 0.6f)
                )
            }
        }
    }
}

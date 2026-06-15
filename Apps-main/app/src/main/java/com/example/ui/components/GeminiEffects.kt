package com.example.ui.components

import android.graphics.Matrix
import android.graphics.SweepGradient
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/**
 * A custom modifier that applies an orbiting/rotating multicolor neon border glow.
 * Inspired by the glowing halo border seen in the Gemini voice assistant overlay.
 */
@Composable
fun Modifier.geminiGlowBorder(
    borderWidth: Dp = 1.5.dp,
    cornerRadius: Dp = 24.dp,
    speedMillis: Int = 5000
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "gemini_glow")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = speedMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_angle"
    )

    return this.drawWithContent {
        // First draw the inner content of the card/dialog
        drawContent()

        val strokeWidthPx = borderWidth.toPx()
        val cornerRadiusPx = cornerRadius.toPx()

        // Multicolor gradient representing Gemini aura
        val colors = intArrayOf(
            android.graphics.Color.parseColor("#9E00FF"), // Violet
            android.graphics.Color.parseColor("#00F0FF"), // Cyan
            android.graphics.Color.parseColor("#00FF85"), // Emerald
            android.graphics.Color.parseColor("#FF007A"), // Magenta/Pink
            android.graphics.Color.parseColor("#FFFFB800"), // Yellow/Gold
            android.graphics.Color.parseColor("#9E00FF")  // Loop Violet
        )

        // Create SweepGradient centered in the element
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val sweepGradient = SweepGradient(centerX, centerY, colors, null)

        // Rotate the gradient using a matrix transformation
        val matrix = Matrix()
        matrix.postRotate(angle, centerX, centerY)
        sweepGradient.setLocalMatrix(matrix)

        // Bridge to Compose Brush
        val glowBrush = ShaderBrush(sweepGradient)

        // Draw soft, glowing shadow stroke under the main border
        drawRoundRect(
            brush = glowBrush,
            size = size,
            cornerRadius = CornerRadius(cornerRadiusPx),
            style = Stroke(width = strokeWidthPx * 2.8f, cap = StrokeCap.Round),
            alpha = 0.35f
        )

        // Draw crisp, bright border
        drawRoundRect(
            brush = glowBrush,
            size = size,
            cornerRadius = CornerRadius(cornerRadiusPx),
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
        )
    }
}

/**
 * An organic, flowing, and undulating wave/waveform canvas.
 * Renders multiple overlapping sine waves with custom phases and frequencies.
 */
@Composable
fun GeminiWaves(
    modifier: Modifier = Modifier,
    isThinking: Boolean = false,
    heightDp: Dp = 50.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gemini_waves")

    // Slow organic forward phase
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase_1"
    )

    // Faster backward phase for interference effect
    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (-2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase_2"
    )

    // Medium speed forward phase
    val phase3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase_3"
    )

    // Dynamic scale driven by thinking state (pulses higher when AI is computing)
    val amplitudeMultiplier by animateFloatAsState(
        targetValue = if (isThinking) 1.8f else 0.6f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "wave_amplitude_multiplier"
    )

    Canvas(modifier = modifier.height(heightDp)) {
        val width = size.width
        val height = size.height
        val centerY = height * 0.65f // Baseline of wave near bottom middle

        // Wave 1: Cyan/Blue (Left to Right)
        val brush1 = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF00F2FE).copy(alpha = 0.4f),
                Color(0xFF4FACFE).copy(alpha = 0.5f),
                Color(0xFF7000FF).copy(alpha = 0.1f)
            )
        )

        // Wave 2: Magenta/Pink (Right to Left)
        val brush2 = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFFF355DA).copy(alpha = 0.1f),
                Color(0xFFE22B93).copy(alpha = 0.45f),
                Color(0xFFFF0844).copy(alpha = 0.2f)
            )
        )

        // Wave 3: Emerald/Lime/Mint Glow
        val brush3 = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF38EF7D).copy(alpha = 0.15f),
                Color(0xFF11998E).copy(alpha = 0.4f),
                Color(0xFF00F0FF).copy(alpha = 0.2f)
            )
        )

        // Wave 1 Path
        drawSineWave(
            width = width,
            centerY = centerY,
            phase = phase1,
            amplitude = 12f * amplitudeMultiplier,
            frequency = 0.007f,
            brush = brush1
        )

        // Wave 2 Path
        drawSineWave(
            width = width,
            centerY = centerY,
            phase = phase2,
            amplitude = 16f * amplitudeMultiplier,
            frequency = 0.005f,
            brush = brush2
        )

        // Wave 3 Path
        drawSineWave(
            width = width,
            centerY = centerY,
            phase = phase3,
            amplitude = 8f * amplitudeMultiplier,
            frequency = 0.010f,
            brush = brush3
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSineWave(
    width: Float,
    centerY: Float,
    phase: Float,
    amplitude: Float,
    frequency: Float,
    brush: Brush
) {
    val path = Path()
    path.moveTo(0f, size.height) // Bottom-left corner
    path.lineTo(0f, centerY)     // Start curve at centerY

    val stepPx = 6f
    var x = 0f
    while (x <= width) {
        val y = centerY + amplitude * sin(x * frequency + phase)
        path.lineTo(x, y)
        x += stepPx
    }

    path.lineTo(width, size.height) // Bottom-right corner
    path.close()

    drawPath(path = path, brush = brush)
}

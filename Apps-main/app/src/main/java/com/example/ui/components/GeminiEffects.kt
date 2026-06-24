package com.example.ui.components

import android.graphics.Matrix
import android.graphics.SweepGradient
import android.os.Build
import android.graphics.RuntimeShader
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin
import kotlin.math.cos
import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer

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
 * A modifier that applies a real glassmorphic blur and translucency effect.
 * Uses hardware blur RenderEffect on Android 12+ (API 31+) and falls back to simple alpha.
 */
fun Modifier.geminiGlass(
    cornerRadius: Dp = 16.dp,
    alpha: Float = 0.15f
): Modifier = this.then(
    Modifier.graphicsLayer {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            renderEffect = RenderEffect.createBlurEffect(
                45f,
                45f,
                Shader.TileMode.CLAMP
            ).asComposeRenderEffect()
        }
        shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius)
        clip = true
    }
)

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

/**
 * A dynamic, battery-optimized wavy fluid background shader that mimics the smooth Gemini visual style.
 * Uses a native AGSL RuntimeShader on Android 13+ (API 33+) and falls back to a highly optimized,
 * zero-allocation double-buffered multi-layer Canvas path animation on older devices.
 */
@Composable
fun GeminiFluidBackground(
    time: Float,
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme()
) {
    val bgColor = MaterialTheme.colorScheme.background
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        GeminiShaderBackground(time = time, bgColor = bgColor, isDark = isDark, modifier = modifier)
    } else {
        GeminiCanvasBackground(time = time, bgColor = bgColor, isDark = isDark, modifier = modifier)
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun GeminiShaderBackground(
    time: Float,
    bgColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val shaderSource = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform half4 uBgColor;
        uniform float uIsDark;
        
        half4 main(in float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float t = iTime * 0.03;
            
            float2 p = uv;
            p.x += sin(uv.y * 4.0 + t) * 0.06;
            p.y += cos(uv.x * 3.5 - t * 0.8) * 0.06;
            
            half3 color1 = half3(0.62, 0.0, 1.0);   // Violet
            half3 color2 = half3(0.0, 0.94, 1.0);   // Cyan
            half3 color3 = half3(1.0, 0.0, 0.48);   // Magenta
            half3 color4 = half3(0.77, 0.94, 0.14);  // Soft Lime
            
            float w1 = sin(p.x * 1.8 + t) * 0.5 + 0.5;
            float w2 = cos(p.y * 1.5 - t * 0.7) * 0.5 + 0.5;
            float w3 = sin((p.x + p.y) * 1.2 + t * 0.9) * 0.5 + 0.5;
            
            half3 fluidColor = mix(color1, color2, w1);
            fluidColor = mix(fluidColor, color3, w2);
            fluidColor = mix(fluidColor, color4, w3 * 0.25);
            
            float glow = uIsDark > 0.5 ? 0.18 : 0.08;
            half3 finalColor = mix(uBgColor.rgb, fluidColor, glow);
            
            return half4(finalColor, 1.0);
        }
    """.trimIndent()
    
    val shader = remember { RuntimeShader(shaderSource) }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("iTime", time)
        shader.setFloatUniform("uBgColor", bgColor.red, bgColor.green, bgColor.blue, bgColor.alpha)
        shader.setFloatUniform("uIsDark", if (isDark) 1.0f else 0.0f)
        
        drawRect(brush = ShaderBrush(shader))
    }
}

@Composable
fun GeminiCanvasBackground(
    time: Float,
    bgColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val path1 = remember { Path() }
    val path2 = remember { Path() }
    val path3 = remember { Path() }
    val path4 = remember { Path() }
    
    val brush1 = remember(isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF9E00FF).copy(alpha = if (isDark) 0.14f else 0.07f),
                Color(0xFF9E00FF).copy(alpha = 0.0f)
            )
        )
    }
    val brush2 = remember(isDark) {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF00F0FF).copy(alpha = if (isDark) 0.16f else 0.08f),
                Color(0xFF00F0FF).copy(alpha = 0.0f)
            )
        )
    }
    val brush3 = remember(isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFF007A).copy(alpha = if (isDark) 0.12f else 0.06f),
                Color(0xFFFF007A).copy(alpha = 0.0f)
            )
        )
    }
    val brush4 = remember(isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFC4F024).copy(alpha = if (isDark) 0.08f else 0.04f),
                Color(0xFFC4F024).copy(alpha = 0.0f)
            )
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(color = bgColor)
        
        val width = size.width
        val height = size.height
        val step = 12f
        
        // Wave 1 (Violet) - Bottom upwards
        path1.reset()
        path1.moveTo(0f, height)
        var x = 0f
        while (x <= width) {
            val y = height - (height * 0.5f) + 
                    sin(x * 0.003f + time * 0.04f) * 50f + 
                    cos(x * 0.0015f + time * 0.02f) * 25f
            path1.lineTo(x, y)
            x += step
        }
        path1.lineTo(width, height)
        path1.close()
        drawPath(path = path1, brush = brush1)
        
        // Wave 2 (Cyan) - Bottom/Left upwards
        path2.reset()
        path2.moveTo(0f, height)
        x = 0f
        while (x <= width) {
            val y = height - (height * 0.4f) + 
                    sin(x * 0.005f - time * 0.035f + 2.0f) * 60f + 
                    cos(x * 0.0025f + time * 0.025f) * 30f
            path2.lineTo(x, y)
            x += step
        }
        path2.lineTo(width, height)
        path2.close()
        drawPath(path = path2, brush = brush2)
        
        // Wave 3 (Magenta) - Bottom/Right upwards
        path3.reset()
        path3.moveTo(0f, height)
        x = 0f
        while (x <= width) {
            val y = height - (height * 0.3f) + 
                    sin(x * 0.004f + time * 0.03f + 4.0f) * 40f + 
                    sin(x * 0.007f - time * 0.05f) * 20f
            path3.lineTo(x, y)
            x += step
        }
        path3.lineTo(width, height)
        path3.close()
        drawPath(path = path3, brush = brush3)
        
        // Wave 4 (Soft Lime) - Top downwards
        path4.reset()
        path4.moveTo(0f, 0f)
        x = 0f
        while (x <= width) {
            val y = (height * 0.35f) + 
                    sin(x * 0.004f + time * 0.03f) * 40f + 
                    cos(x * 0.002f - time * 0.015f) * 20f
            path4.lineTo(x, y)
            x += step
        }
        path4.lineTo(width, 0f)
        path4.close()
        drawPath(path = path4, brush = brush4)
    }
}

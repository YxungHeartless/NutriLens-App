package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFFC4F024), // Vibrant Soft Lime Glow for active accent
    onPrimary = CosmicSlateDarkBg,
    primaryContainer = Color(0xFF3F4E00), // Dark green-lime container for active states
    onPrimaryContainer = Color(0xFFE3FF6B),
    secondary = Color(0xFF34D399), // Emerald Healthy Green
    onSecondary = Color(0xFF064E3B),
    secondaryContainer = Color(0xFF064E3B),
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = CosmicSlateTertiaryDark,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF7F1D1D),
    onTertiaryContainer = Color(0xFFFCA5A5),
    background = CosmicSlateDarkBg,
    onBackground = CosmicSlateOnBackgroundDark,
    surface = CosmicSlateDarkSurface,
    onSurface = CosmicSlateOnSurfaceDark,
    onSurfaceVariant = CosmicSlateOnSurfaceVariantDark,
    surfaceVariant = CosmicSlateDarkSurfaceVariant,
    outline = CosmicSlateOutlineDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CosmicSlatePrimaryLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFECFCCB), // Light lime container for active states
    onPrimaryContainer = CosmicSlatePrimaryLight,
    secondary = CosmicSlateSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = CosmicSlateSecondaryLight,
    tertiary = CosmicSlateTertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFCE7F3),
    onTertiaryContainer = CosmicSlateTertiaryLight,
    background = CosmicSlateLightBg,
    onBackground = CosmicSlateOnBackgroundLight,
    surface = CosmicSlateLightSurface,
    onSurface = CosmicSlateOnSurfaceLight,
    onSurfaceVariant = CosmicSlateOnSurfaceVariantLight,
    surfaceVariant = CosmicSlateLightSurfaceVariant,
    outline = CosmicSlateOutlineLight
  )

@Composable
fun MyApplicationTheme(
  themeMode: String = "system",
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val darkTheme = when (themeMode) {
    "light" -> false
    "dark" -> true
    else -> isSystemInDarkTheme()
  }

  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

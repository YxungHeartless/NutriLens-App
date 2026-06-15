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
    primary = CosmicSlatePrimaryDark,
    onPrimary = CosmicSlateDarkBg,
    secondary = CosmicSlateSecondaryDark,
    onSecondary = Color.White,
    tertiary = CosmicSlateTertiaryDark,
    onTertiary = Color.White,
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
    secondary = CosmicSlateSecondaryLight,
    onSecondary = Color.White,
    tertiary = CosmicSlateTertiaryLight,
    onTertiary = Color.White,
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

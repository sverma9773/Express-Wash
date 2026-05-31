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

private val DarkColorScheme =
  darkColorScheme(
    primary = BrandRed,
    secondary = BrandRedGlow,
    tertiary = GlowNeonRed,
    background = DeepBlack,
    surface = DarkCharcoal,
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onTertiary = PureWhite,
    onBackground = PureWhite,
    onSurface = PureWhite,
    surfaceVariant = CardGray,
    onSurfaceVariant = SoftSilver
  )

private val LightColorScheme =
  lightColorScheme(
    primary = BrandRed,
    secondary = DarkCharcoal,
    tertiary = BrandRedGlow,
    background = LightGray,
    surface = PureWhite,
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onTertiary = PureWhite,
    onBackground = DeepBlack,
    onSurface = DeepBlack,
    surfaceVariant = SoftSilver,
    onSurfaceVariant = DarkCharcoal
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color to maintain strict luxury branding representation
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

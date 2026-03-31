package com.vadymdev.habitix.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = BrandGreen,
    secondary = BrandGreenDark,
    tertiary = BrandMint,
    background = AppBackground,
    surface = SurfaceCard,
    onPrimary = SurfaceCard,
    onSecondary = SurfaceCard,
    onTertiary = BrandGreenDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun HabitixTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
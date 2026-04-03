package com.vadymdev.habitix.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.vadymdev.habitix.domain.model.AccentPalette
import com.vadymdev.habitix.domain.model.ThemeMode

@Composable
fun HabitixTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    accentPalette: AccentPalette = AccentPalette.MINT,
    content: @Composable () -> Unit
) {
    val accent = palettePrimary(accentPalette)
    val accentDark = palettePrimaryDark(accentPalette)

    val colorScheme = when (themeMode) {
        ThemeMode.LIGHT -> lightColorScheme(
            primary = accent,
            secondary = accentDark,
            tertiary = BrandMint,
            background = AppBackground,
            surface = SurfaceCard,
            onPrimary = SurfaceCard,
            onSecondary = SurfaceCard,
            onTertiary = accentDark,
            onBackground = TextPrimary,
            onSurface = TextPrimary
        )

        ThemeMode.DARK -> darkColorScheme(
            primary = accent,
            secondary = accentDark,
            tertiary = BrandMint,
            background = DarkBackground,
            surface = DarkSurface,
            onPrimary = SurfaceCard,
            onSecondary = SurfaceCard,
            onTertiary = SurfaceCard,
            onBackground = DarkTextPrimary,
            onSurface = DarkTextPrimary
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private fun palettePrimary(value: AccentPalette) = when (value) {
    AccentPalette.MINT -> BrandGreen
    AccentPalette.SKY -> AccentSky
    AccentPalette.LAVENDER -> AccentLavender
    AccentPalette.PEACH -> AccentPeach
    AccentPalette.ROSE -> AccentRose
}

private fun palettePrimaryDark(value: AccentPalette) = when (value) {
    AccentPalette.MINT -> BrandGreenDark
    AccentPalette.SKY -> AccentSkyDark
    AccentPalette.LAVENDER -> AccentLavenderDark
    AccentPalette.PEACH -> AccentPeachDark
    AccentPalette.ROSE -> AccentRoseDark
}
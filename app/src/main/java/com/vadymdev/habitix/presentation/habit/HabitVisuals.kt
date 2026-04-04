package com.vadymdev.habitix.presentation.habit

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

fun habitColor(colorKey: String): Color {
    return when (colorKey) {
        "mint" -> Color(0xFF7CE5C0)
        "orange" -> Color(0xFFF7C084)
        "purple" -> Color(0xFFBEB4F6)
        "blue" -> Color(0xFF84D2F3)
        "pink" -> Color(0xFFF5BCCB)
        else -> Color(0xFF7CE5C0)
    }
}

fun habitIcon(iconKey: String): String {
    return when (iconKey) {
        "water" -> "◔"
        "book" -> "◫"
        "fitness" -> "⌁"
        "moon" -> "☾"
        "mind" -> "❀"
        "heart" -> "♡"
        "fork" -> "Ψ"
        "music" -> "♫"
        "pen" -> "✎"
        "sun" -> "☼"
        "cup" -> "☕"
        "steps" -> "⌕"
        else -> "◔"
    }
}

fun habitIconVector(iconKey: String): ImageVector {
    return when (iconKey) {
        "water" -> Icons.Rounded.WaterDrop
        "book" -> Icons.Rounded.Book
        "fitness" -> Icons.AutoMirrored.Rounded.DirectionsRun
        "moon" -> Icons.Rounded.NightsStay
        "mind" -> Icons.Rounded.Psychology
        "heart" -> Icons.Rounded.FavoriteBorder
        "fork" -> Icons.Rounded.Restaurant
        "music" -> Icons.Rounded.MusicNote
        "pen" -> Icons.Rounded.Edit
        "sun" -> Icons.Rounded.WbSunny
        "cup" -> Icons.Rounded.LocalCafe
        "steps" -> Icons.AutoMirrored.Rounded.MenuBook
        else -> Icons.Rounded.WaterDrop
    }
}

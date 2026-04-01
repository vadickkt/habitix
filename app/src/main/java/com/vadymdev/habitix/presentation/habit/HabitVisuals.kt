package com.vadymdev.habitix.presentation.habit

import androidx.compose.ui.graphics.Color

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

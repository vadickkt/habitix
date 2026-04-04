package com.vadymdev.habitix.presentation.profile

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

internal enum class ShareStyle {
    GRADIENT,
    DARK,
    LIGHT
}

internal fun ShareStyle.previewBrush(): Brush {
    return when (this) {
        ShareStyle.GRADIENT -> Brush.linearGradient(listOf(Color(0xFF07BA73), Color(0xFFDFC169)))
        ShareStyle.DARK -> Brush.linearGradient(listOf(Color(0xFF21233C), Color(0xFF181A2E)))
        ShareStyle.LIGHT -> Brush.linearGradient(listOf(Color(0xFFE9F6EF), Color(0xFFF6F5F2)))
    }
}

internal fun ShareStyle.gradientColors(): IntArray {
    return when (this) {
        ShareStyle.GRADIENT -> intArrayOf(android.graphics.Color.parseColor("#07BA73"), android.graphics.Color.parseColor("#DFC169"))
        ShareStyle.DARK -> intArrayOf(android.graphics.Color.parseColor("#21233C"), android.graphics.Color.parseColor("#181A2E"))
        ShareStyle.LIGHT -> intArrayOf(android.graphics.Color.parseColor("#E9F6EF"), android.graphics.Color.parseColor("#F6F5F2"))
    }
}

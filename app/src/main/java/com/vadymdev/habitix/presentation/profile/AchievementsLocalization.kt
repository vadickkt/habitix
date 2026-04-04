package com.vadymdev.habitix.presentation.profile

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.vadymdev.habitix.domain.model.ProfileAchievement

@Composable
internal fun localizedString(
    isUk: Boolean,
    @StringRes ukResId: Int,
    @StringRes enResId: Int,
    vararg formatArgs: Any
): String {
    return if (isUk) {
        stringResource(ukResId, *formatArgs)
    } else {
        stringResource(enResId, *formatArgs)
    }
}

internal fun achievementsScreenIcon(iconKey: String): ImageVector {
    return achievementIcon(iconKey)
}

internal fun achievementsScreenColor(colorKey: String): Color {
    return achievementColor(colorKey)
}

internal fun achievementsCategoryLabel(categoryKey: String, isUk: Boolean): String {
    if (isUk) return categoryKey
    return when (categoryKey) {
        "Всі" -> "All"
        "Серії" -> "Streaks"
        "Час" -> "Time"
        "Досконалість" -> "Perfection"
        "Початок" -> "Start"
        "Категорії" -> "Categories"
        else -> categoryKey
    }
}

internal fun achievementsLocalizedTitle(achievement: ProfileAchievement, isUk: Boolean): String {
    return localizedAchievementTitle(achievement, isUk)
}

internal fun achievementsLocalizedDescription(achievement: ProfileAchievement, isUk: Boolean): String {
    return localizedAchievementDescription(achievement, isUk)
}

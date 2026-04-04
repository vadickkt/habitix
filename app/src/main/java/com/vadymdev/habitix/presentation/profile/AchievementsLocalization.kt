package com.vadymdev.habitix.presentation.profile

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.vadymdev.habitix.R
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

@Composable
internal fun achievementsCategoryLabel(categoryKey: String, isUk: Boolean): String {
    return when (categoryKey) {
        ACHIEVEMENTS_CATEGORY_ALL -> localizedString(isUk, R.string.achievements_category_all_uk, R.string.achievements_category_all_en)
        ACHIEVEMENTS_CATEGORY_STREAKS -> localizedString(isUk, R.string.achievements_category_streaks_uk, R.string.achievements_category_streaks_en)
        ACHIEVEMENTS_CATEGORY_TIME -> localizedString(isUk, R.string.achievements_category_time_uk, R.string.achievements_category_time_en)
        ACHIEVEMENTS_CATEGORY_PERFECTION -> localizedString(isUk, R.string.achievements_category_perfection_uk, R.string.achievements_category_perfection_en)
        ACHIEVEMENTS_CATEGORY_START -> localizedString(isUk, R.string.achievements_category_start_uk, R.string.achievements_category_start_en)
        ACHIEVEMENTS_CATEGORY_CATEGORIES -> localizedString(isUk, R.string.achievements_category_categories_uk, R.string.achievements_category_categories_en)
        else -> categoryKey
    }
}

@Composable
internal fun achievementsLocalizedTitle(achievement: ProfileAchievement, isUk: Boolean): String {
    return localizedAchievementTitle(achievement, isUk)
}

@Composable
internal fun achievementsLocalizedDescription(achievement: ProfileAchievement, isUk: Boolean): String {
    return localizedAchievementDescription(achievement, isUk)
}

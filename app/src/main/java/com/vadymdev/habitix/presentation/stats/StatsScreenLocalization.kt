package com.vadymdev.habitix.presentation.stats

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.vadymdev.habitix.R
import com.vadymdev.habitix.domain.model.HabitBadge

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

internal fun localizedCategoryName(raw: String, isUk: Boolean): String {
    if (isUk) return raw
    return when (raw) {
        "Здоров'я" -> "Health"
        "Продуктивність" -> "Productivity"
        "Спорт" -> "Sport"
        "Усвідомленість" -> "Mindfulness"
        else -> raw
    }
}

@Composable
internal fun localizedBadgeTitle(badge: HabitBadge, isUk: Boolean): String {
    return when (badge.id) {
        "week_1" -> if (isUk) stringResource(R.string.badge_week_1_title_uk) else stringResource(R.string.badge_week_1_title_en)
        "days_30" -> if (isUk) stringResource(R.string.badge_days_30_title_uk) else stringResource(R.string.badge_days_30_title_en)
        "days_100" -> if (isUk) stringResource(R.string.badge_days_100_title_uk) else stringResource(R.string.badge_days_100_title_en)
        "morning" -> if (isUk) stringResource(R.string.badge_morning_title_uk) else stringResource(R.string.badge_morning_title_en)
        "mind" -> if (isUk) stringResource(R.string.badge_mind_title_uk) else stringResource(R.string.badge_mind_title_en)
        "book" -> if (isUk) stringResource(R.string.badge_book_title_uk) else stringResource(R.string.badge_book_title_en)
        else -> badge.title
    }
}

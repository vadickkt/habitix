package com.vadymdev.habitix.presentation.stats

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
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

internal fun localizedBadgeTitle(badge: HabitBadge, isUk: Boolean): String {
    if (isUk) return badge.title
    return when (badge.id) {
        "week_1" -> "First week"
        "days_30" -> "30 days"
        "days_100" -> "100 days"
        "morning" -> "Morning bird"
        "mind" -> "Meditation master"
        "book" -> "Bookworm"
        else -> badge.title
    }
}

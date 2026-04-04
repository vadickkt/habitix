package com.vadymdev.habitix.presentation.profile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.vadymdev.habitix.R
import com.vadymdev.habitix.domain.model.ProfileAchievement

internal fun achievementIcon(iconKey: String): ImageVector {
    return when (iconKey) {
        "flame" -> Icons.Rounded.LocalFireDepartment
        "medal" -> Icons.Rounded.MilitaryTech
        "crown" -> Icons.Rounded.WorkspacePremium
        "zap" -> Icons.Rounded.Bolt
        "sunrise" -> Icons.Rounded.WbSunny
        "moon" -> Icons.Rounded.Nightlight
        "target" -> Icons.Rounded.TrackChanges
        "sparkles" -> Icons.Rounded.AutoAwesome
        "trophy" -> Icons.Rounded.EmojiEvents
        "heart" -> Icons.Rounded.Favorite
        "dumbbell" -> Icons.Rounded.FitnessCenter
        "brain" -> Icons.Rounded.Psychology
        "book" -> Icons.AutoMirrored.Rounded.MenuBook
        "coffee" -> Icons.Rounded.Coffee
        else -> Icons.Rounded.Star
    }
}

internal fun achievementColor(colorKey: String): Color {
    return when (colorKey) {
        "peach" -> Color(0xFFF6C491)
        "rose" -> Color(0xFFF0B3C5)
        "mint" -> Color(0xFFB7E9D1)
        "sky" -> Color(0xFFB6DDF1)
        "lavender" -> Color(0xFFD8D0F8)
        else -> Color(0xFFE7E4E0)
    }
}

@Composable
internal fun localizedAchievementTitle(achievement: ProfileAchievement, isUk: Boolean): String {
    return when (achievement.id) {
        "week_7" -> localizedString(isUk, R.string.achievement_week_7_title_uk, R.string.achievement_week_7_title_en)
        "week_14" -> localizedString(isUk, R.string.achievement_week_14_title_uk, R.string.achievement_week_14_title_en)
        "week_30" -> localizedString(isUk, R.string.achievement_week_30_title_uk, R.string.achievement_week_30_title_en)
        "week_100" -> localizedString(isUk, R.string.achievement_week_100_title_uk, R.string.achievement_week_100_title_en)
        "early_8" -> localizedString(isUk, R.string.achievement_early_8_title_uk, R.string.achievement_early_8_title_en)
        "early_7" -> localizedString(isUk, R.string.achievement_early_7_title_uk, R.string.achievement_early_7_title_en)
        "late_owl" -> localizedString(isUk, R.string.achievement_late_owl_title_uk, R.string.achievement_late_owl_title_en)
        "month_perfect" -> localizedString(isUk, R.string.achievement_month_perfect_title_uk, R.string.achievement_month_perfect_title_en)
        "perfect_week" -> localizedString(isUk, R.string.achievement_perfect_week_title_uk, R.string.achievement_perfect_week_title_en)
        "first" -> localizedString(isUk, R.string.achievement_first_title_uk, R.string.achievement_first_title_en)
        "five" -> localizedString(isUk, R.string.achievement_five_title_uk, R.string.achievement_five_title_en)
        "ten" -> localizedString(isUk, R.string.achievement_ten_title_uk, R.string.achievement_ten_title_en)
        "health" -> localizedString(isUk, R.string.achievement_health_title_uk, R.string.achievement_health_title_en)
        "sport" -> localizedString(isUk, R.string.achievement_sport_title_uk, R.string.achievement_sport_title_en)
        "mind" -> localizedString(isUk, R.string.achievement_mind_title_uk, R.string.achievement_mind_title_en)
        "book" -> localizedString(isUk, R.string.achievement_book_title_uk, R.string.achievement_book_title_en)
        "prod" -> localizedString(isUk, R.string.achievement_prod_title_uk, R.string.achievement_prod_title_en)
        else -> achievement.title
    }
}

@Composable
internal fun localizedAchievementDescription(achievement: ProfileAchievement, isUk: Boolean): String {
    return when (achievement.id) {
        "week_7" -> localizedString(isUk, R.string.achievement_week_7_desc_uk, R.string.achievement_week_7_desc_en)
        "week_14" -> localizedString(isUk, R.string.achievement_week_14_desc_uk, R.string.achievement_week_14_desc_en)
        "week_30" -> localizedString(isUk, R.string.achievement_week_30_desc_uk, R.string.achievement_week_30_desc_en)
        "week_100" -> localizedString(isUk, R.string.achievement_week_100_desc_uk, R.string.achievement_week_100_desc_en)
        "early_8" -> localizedString(isUk, R.string.achievement_early_8_desc_uk, R.string.achievement_early_8_desc_en)
        "early_7" -> localizedString(isUk, R.string.achievement_early_7_desc_uk, R.string.achievement_early_7_desc_en)
        "late_owl" -> localizedString(isUk, R.string.achievement_late_owl_desc_uk, R.string.achievement_late_owl_desc_en)
        "month_perfect" -> localizedString(isUk, R.string.achievement_month_perfect_desc_uk, R.string.achievement_month_perfect_desc_en)
        "perfect_week" -> localizedString(isUk, R.string.achievement_perfect_week_desc_uk, R.string.achievement_perfect_week_desc_en)
        "first" -> localizedString(isUk, R.string.achievement_first_desc_uk, R.string.achievement_first_desc_en)
        "five" -> localizedString(isUk, R.string.achievement_five_desc_uk, R.string.achievement_five_desc_en)
        "ten" -> localizedString(isUk, R.string.achievement_ten_desc_uk, R.string.achievement_ten_desc_en)
        "health" -> localizedString(isUk, R.string.achievement_health_desc_uk, R.string.achievement_health_desc_en)
        "sport" -> localizedString(isUk, R.string.achievement_sport_desc_uk, R.string.achievement_sport_desc_en)
        "mind" -> localizedString(isUk, R.string.achievement_mind_desc_uk, R.string.achievement_mind_desc_en)
        "book" -> localizedString(isUk, R.string.achievement_book_desc_uk, R.string.achievement_book_desc_en)
        "prod" -> localizedString(isUk, R.string.achievement_prod_desc_uk, R.string.achievement_prod_desc_en)
        else -> achievement.description
    }
}

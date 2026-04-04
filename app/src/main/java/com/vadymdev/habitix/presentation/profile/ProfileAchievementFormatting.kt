package com.vadymdev.habitix.presentation.profile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
        "book" -> Icons.Rounded.MenuBook
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

internal fun localizedAchievementTitle(achievement: ProfileAchievement, isUk: Boolean): String {
    if (isUk) return achievement.title
    return when (achievement.id) {
        "week_7" -> "7-day streak"
        "week_14" -> "14-day streak"
        "week_30" -> "Marathoner"
        "week_100" -> "Legend"
        "early_8" -> "Early bird"
        "early_7" -> "Dawn warrior"
        "late_owl" -> "Night owl"
        "month_perfect" -> "Monthly champion"
        "perfect_week" -> "Perfectionist"
        "first" -> "First win"
        "five" -> "Collector"
        "ten" -> "Ambitious"
        "health" -> "Healthy lifestyle"
        "sport" -> "Athlete"
        "mind" -> "Sage"
        "book" -> "Book lover"
        "prod" -> "Productive"
        else -> achievement.title
    }
}

internal fun localizedAchievementDescription(achievement: ProfileAchievement, isUk: Boolean): String {
    if (isUk) return achievement.description
    return when (achievement.id) {
        "week_7" -> "Complete a habit for 7 days in a row"
        "week_14" -> "Complete a habit for 14 days in a row"
        "week_30" -> "30-day streak"
        "week_100" -> "100-day streak"
        "early_8" -> "Complete 5 habits before 8:00"
        "early_7" -> "Complete 20 habits before 7:00"
        "late_owl" -> "Complete 10 habits after 22:00"
        "month_perfect" -> "100% completion for a month"
        "perfect_week" -> "Perfect week"
        "first" -> "Create your first habit"
        "five" -> "Create 5 different habits"
        "ten" -> "Create 10 habits"
        "health" -> "Complete 50 Health category habits"
        "sport" -> "Complete 30 Sport category habits"
        "mind" -> "Complete 50 Mindfulness category habits"
        "book" -> "Read for 30 days in a row"
        "prod" -> "Complete 100 Productivity category habits"
        else -> achievement.description
    }
}

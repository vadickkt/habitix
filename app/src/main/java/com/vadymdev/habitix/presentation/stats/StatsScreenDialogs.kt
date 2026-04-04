package com.vadymdev.habitix.presentation.stats

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.vadymdev.habitix.R
import com.vadymdev.habitix.domain.model.HabitBadge
import com.vadymdev.habitix.domain.model.HabitCategoryStat
import com.vadymdev.habitix.domain.model.HabitStatsSnapshot
import java.time.format.DateTimeFormatter

@Composable
internal fun MetricDetailsDialog(metric: StatsMetric, snapshot: HabitStatsSnapshot, isUk: Boolean, onDismiss: () -> Unit) {
    val (title, body) = when (metric) {
        StatsMetric.LONGEST_STREAK -> localizedString(isUk, R.string.stats_longest_streak_uk, R.string.stats_longest_streak_en) to
            localizedString(isUk, R.string.stats_metric_longest_body_uk, R.string.stats_metric_longest_body_en, snapshot.longestStreak)
        StatsMetric.BADGES -> localizedString(isUk, R.string.stats_badges_label_uk, R.string.stats_badges_label_en) to
            localizedString(isUk, R.string.stats_metric_badges_body_uk, R.string.stats_metric_badges_body_en, snapshot.earnedBadgesCount, snapshot.badges.size)
        StatsMetric.SUCCESS -> localizedString(isUk, R.string.stats_success_rate_uk, R.string.stats_success_rate_en) to
            localizedString(isUk, R.string.stats_metric_success_body_uk, R.string.stats_metric_success_body_en, snapshot.successRatePercent)
        StatsMetric.COMPLETED -> localizedString(isUk, R.string.stats_completed_label_uk, R.string.stats_completed_label_en) to
            localizedString(isUk, R.string.stats_metric_completed_body_uk, R.string.stats_metric_completed_body_en, snapshot.completedTasksCount)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(localizedString(isUk, R.string.common_got_it_uk, R.string.common_got_it_en)) } },
        title = { Text(title) },
        text = { Text(body) }
    )
}

@Composable
internal fun CategoryDetailsDialog(category: HabitCategoryStat, isUk: Boolean, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(localizedString(isUk, R.string.common_ok_uk, R.string.common_ok_en)) } },
        title = { Text(localizedCategoryName(category.name, isUk)) },
        text = { Text(localizedString(isUk, R.string.stats_category_share_body_uk, R.string.stats_category_share_body_en, category.percent)) }
    )
}

@Composable
internal fun BadgeDetailsDialog(badge: HabitBadge, isUk: Boolean, onDismiss: () -> Unit) {
    val status = if (badge.earned) {
        localizedString(isUk, R.string.stats_badge_unlocked_uk, R.string.stats_badge_unlocked_en)
    } else {
        localizedString(isUk, R.string.stats_badge_locked_uk, R.string.stats_badge_locked_en)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(localizedString(isUk, R.string.common_close_uk, R.string.common_close_en)) } },
        title = { Text("${badge.emoji} ${localizedBadgeTitle(badge, isUk)}") },
        text = { Text(status) }
    )
}

@Composable
internal fun HeatmapDayDialog(day: StatsHeatmapDayDetails, isUk: Boolean, onDismiss: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(localizedString(isUk, R.string.common_close_uk, R.string.common_close_en)) } },
        title = { Text(localizedString(isUk, R.string.stats_activity_day_title_uk, R.string.stats_activity_day_title_en)) },
        text = { Text(localizedString(isUk, R.string.stats_activity_day_body_uk, R.string.stats_activity_day_body_en, day.date.format(formatter), day.completedCount)) }
    )
}

package com.vadymdev.habitix.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

enum class HabitFrequencyType {
    DAILY,
    WEEKDAYS,
    CUSTOM
}

data class Habit(
    val id: Long,
    val title: String,
    val iconKey: String,
    val colorKey: String,
    val frequencyType: HabitFrequencyType,
    val customDays: Set<DayOfWeek>,
    val reminderEnabled: Boolean,
    val isCompletedForSelectedDate: Boolean,
    val streakDays: Int
)

data class HabitCreateDraft(
    val title: String,
    val iconKey: String,
    val colorKey: String,
    val frequencyType: HabitFrequencyType,
    val customDays: Set<DayOfWeek>,
    val reminderEnabled: Boolean
)

data class HabitDailySummary(
    val selectedDate: LocalDate,
    val completed: Int,
    val total: Int
)

data class HabitCategoryStat(
    val name: String,
    val percent: Int,
    val colorKey: String
)

data class HabitBadge(
    val id: String,
    val title: String,
    val emoji: String,
    val earned: Boolean
)

data class HabitStatsSnapshot(
    val longestStreak: Int,
    val earnedBadgesCount: Int,
    val successRatePercent: Int,
    val completedTasksCount: Int,
    val heatmapLevels: List<Int>,
    val heatmapCounts: List<Int>,
    val heatmapStartEpochDay: Long,
    val categoryStats: List<HabitCategoryStat>,
    val badges: List<HabitBadge>
)

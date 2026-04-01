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

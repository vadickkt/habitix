package com.vadymdev.habitix.data.local.room

import androidx.room.Entity

@Entity(
    tableName = "hidden_habit_days",
    primaryKeys = ["habitId", "dateEpochDay"]
)
data class HiddenHabitDayEntity(
    val habitId: Long,
    val dateEpochDay: Long
)

package com.vadymdev.habitix.data.local.room

import androidx.room.Entity

@Entity(
    tableName = "habit_completions",
    primaryKeys = ["habitId", "dateEpochDay"]
)
data class HabitCompletionEntity(
    val habitId: Long,
    val dateEpochDay: Long,
    val completedAtMillis: Long
)

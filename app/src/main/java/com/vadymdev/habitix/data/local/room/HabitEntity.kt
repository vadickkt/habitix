package com.vadymdev.habitix.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cloudId: String,
    val title: String,
    val iconKey: String,
    val colorKey: String,
    val frequencyType: String,
    val customDaysCsv: String,
    val reminderEnabled: Boolean,
    val reminderHour: Int,
    val reminderMinute: Int,
    val createdAt: Long,
    val startEpochDay: Long,
    val activeUntilEpochDay: Long?,
    val isArchived: Boolean,
    val source: String
)

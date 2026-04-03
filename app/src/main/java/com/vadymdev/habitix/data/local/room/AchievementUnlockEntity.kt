package com.vadymdev.habitix.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievement_unlocks")
data class AchievementUnlockEntity(
    @PrimaryKey val achievementId: String,
    val unlockedEpochDay: Long
)

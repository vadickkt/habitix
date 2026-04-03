package com.vadymdev.habitix.domain.model

import java.time.LocalDate

data class ProfileIdentity(
    val displayName: String,
    val bio: String,
    val avatarInitials: String,
    val avatarUri: String?
)

data class ProfileAchievement(
    val id: String,
    val title: String,
    val description: String,
    val iconKey: String,
    val colorKey: String,
    val category: String,
    val xpReward: Int,
    val progressPercent: Int,
    val unlocked: Boolean,
    val unlockedDate: LocalDate?
)

data class ProfileAnalytics(
    val level: Int,
    val xpCurrent: Int,
    val xpTarget: Int,
    val currentStreakDays: Int,
    val bestStreakDays: Int,
    val totalCompleted: Int,
    val daysWithUs: Int,
    val monthGrowthPercent: Int,
    val monthWeeklyActivity: List<Int>,
    val topAchievements: List<ProfileAchievement>,
    val allAchievements: List<ProfileAchievement>
)

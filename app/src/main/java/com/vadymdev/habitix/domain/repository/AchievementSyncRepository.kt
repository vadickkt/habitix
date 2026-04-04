package com.vadymdev.habitix.domain.repository

interface AchievementSyncRepository {
    suspend fun sync(userId: String)
    suspend fun clearUserData(userId: String)
}

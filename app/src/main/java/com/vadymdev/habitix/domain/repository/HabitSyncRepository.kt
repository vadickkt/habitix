package com.vadymdev.habitix.domain.repository

interface HabitSyncRepository {
    suspend fun syncUserHabits(userId: String)
    suspend fun clearUserData(userId: String)
}

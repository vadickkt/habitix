package com.vadymdev.habitix.domain.repository

interface HabitSyncRepository {
    suspend fun syncUserHabits(userId: String)
}

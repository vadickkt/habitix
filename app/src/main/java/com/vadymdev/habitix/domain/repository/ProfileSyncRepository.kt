package com.vadymdev.habitix.domain.repository

interface ProfileSyncRepository {
    suspend fun sync(userId: String)
    suspend fun clearUserData(userId: String)
}

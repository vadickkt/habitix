package com.vadymdev.habitix.domain.repository

interface SettingsSyncRepository {
    suspend fun sync(userId: String)
}

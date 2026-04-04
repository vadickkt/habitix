package com.vadymdev.habitix.domain.usecase

import com.vadymdev.habitix.domain.repository.AchievementSyncRepository
import com.vadymdev.habitix.domain.repository.SettingsRepository

class SyncAchievementsUseCase(
    private val repository: AchievementSyncRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(userId: String) {
        if (!settingsRepository.getCurrentSettings().autoSyncEnabled) return
        repository.sync(userId)
    }
}

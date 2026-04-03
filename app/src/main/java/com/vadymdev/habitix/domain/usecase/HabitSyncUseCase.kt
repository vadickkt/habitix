package com.vadymdev.habitix.domain.usecase

import com.vadymdev.habitix.domain.repository.HabitSyncRepository
import com.vadymdev.habitix.domain.repository.SettingsRepository

class SyncUserHabitsUseCase(
    private val repository: HabitSyncRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(userId: String) {
        if (!settingsRepository.getCurrentSettings().autoSyncEnabled) return
        repository.syncUserHabits(userId)
    }
}

package com.vadymdev.habitix.domain.usecase

import com.vadymdev.habitix.domain.repository.AchievementSyncRepository
import com.vadymdev.habitix.domain.repository.HabitRepository
import com.vadymdev.habitix.domain.repository.HabitSyncRepository
import com.vadymdev.habitix.domain.repository.ProfileRepository
import com.vadymdev.habitix.domain.repository.ProfileSyncRepository
import com.vadymdev.habitix.domain.repository.SettingsRepository
import com.vadymdev.habitix.domain.repository.SettingsSyncRepository

class DeleteDataUseCase(
    private val habitRepository: HabitRepository,
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val habitSyncRepository: HabitSyncRepository,
    private val profileSyncRepository: ProfileSyncRepository,
    private val settingsSyncRepository: SettingsSyncRepository,
    private val achievementSyncRepository: AchievementSyncRepository
) {
    suspend operator fun invoke(userId: String?) {
        habitRepository.deleteAllHabits()
        profileRepository.clearLocalData()
        settingsRepository.resetToDefaults()

        if (userId.isNullOrBlank()) return

        habitSyncRepository.clearUserData(userId)
        profileSyncRepository.clearUserData(userId)
        settingsSyncRepository.clearUserData(userId)
        achievementSyncRepository.clearUserData(userId)
    }
}

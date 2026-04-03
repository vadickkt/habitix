package com.vadymdev.habitix.domain.usecase

import com.vadymdev.habitix.domain.model.ProfileAnalytics
import com.vadymdev.habitix.domain.model.ProfileIdentity
import com.vadymdev.habitix.domain.repository.HabitRepository
import com.vadymdev.habitix.domain.repository.ProfileRepository
import com.vadymdev.habitix.domain.repository.ProfileSyncRepository
import com.vadymdev.habitix.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveProfileIdentityUseCase(private val repository: ProfileRepository) {
    operator fun invoke(): Flow<ProfileIdentity> = repository.observeProfileIdentity()
}

class UpdateProfileNameUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(name: String) = repository.updateDisplayName(name)
}

class UpdateProfileBioUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(bio: String) = repository.updateBio(bio)
}

class UpdateProfileAvatarUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(uri: String?) = repository.updateAvatarUri(uri)
}

class ObserveProfileAnalyticsUseCase(private val repository: HabitRepository) {
    operator fun invoke(): Flow<ProfileAnalytics> = repository.observeProfileAnalytics()
}

class SyncProfileUseCase(
    private val repository: ProfileSyncRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(userId: String) {
        if (!settingsRepository.getCurrentSettings().autoSyncEnabled) return
        repository.sync(userId)
    }
}

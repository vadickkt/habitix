package com.vadymdev.habitix.data.repository

import com.vadymdev.habitix.data.local.OnboardingPreferencesDataSource
import com.vadymdev.habitix.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow

class OnboardingRepositoryImpl(
    private val local: OnboardingPreferencesDataSource
) : OnboardingRepository {

    override fun observeOnboardingState(): Flow<com.vadymdev.habitix.domain.model.OnboardingState> {
        return local.observeOnboardingState()
    }

    override suspend fun updateInterests(values: Set<String>) {
        local.updateInterests(values)
    }

    override suspend fun updateHabits(values: Set<String>) {
        local.updateHabits(values)
    }

    override suspend fun completeOnboarding() {
        local.setCompleted()
    }
}

package com.vadymdev.habitix.data.repository

import com.vadymdev.habitix.data.local.OnboardingPreferencesDataSource
import com.vadymdev.habitix.domain.repository.HabitRepository
import com.vadymdev.habitix.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class OnboardingRepositoryImpl(
    private val local: OnboardingPreferencesDataSource,
    private val habitRepository: HabitRepository
) : OnboardingRepository {

    private val completionMutex = Mutex()

    override fun observeOnboardingState(): Flow<com.vadymdev.habitix.domain.model.OnboardingState> {
        return local.observeOnboardingState()
    }

    override suspend fun updateInterests(values: Set<String>) {
        local.updateInterests(values)
    }

    override suspend fun updateHabits(values: Set<String>) {
        local.updateHabits(values)
    }

    override suspend fun completeOnboarding(selectedHabitKeys: Set<String>) {
        completionMutex.withLock {
            val current = local.observeOnboardingState().first()
            if (current.completed) return

            habitRepository.seedOnboardingHabits(selectedHabitKeys)
            local.setCompleted()
        }
    }
}

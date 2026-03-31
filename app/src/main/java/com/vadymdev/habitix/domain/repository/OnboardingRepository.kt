package com.vadymdev.habitix.domain.repository

import com.vadymdev.habitix.domain.model.OnboardingState
import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    fun observeOnboardingState(): Flow<OnboardingState>
    suspend fun updateInterests(values: Set<String>)
    suspend fun updateHabits(values: Set<String>)
    suspend fun completeOnboarding()
}

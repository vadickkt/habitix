package com.vadymdev.habitix.domain.usecase

import com.vadymdev.habitix.domain.model.OnboardingState
import com.vadymdev.habitix.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow

class ObserveOnboardingUseCase(private val repository: OnboardingRepository) {
    operator fun invoke(): Flow<OnboardingState> = repository.observeOnboardingState()
}

class UpdateInterestsUseCase(private val repository: OnboardingRepository) {
    suspend operator fun invoke(values: Set<String>) = repository.updateInterests(values)
}

class UpdateHabitsUseCase(private val repository: OnboardingRepository) {
    suspend operator fun invoke(values: Set<String>) = repository.updateHabits(values)
}

class CompleteOnboardingUseCase(private val repository: OnboardingRepository) {
    suspend operator fun invoke(selectedHabitKeys: Set<String>) = repository.completeOnboarding(selectedHabitKeys)
}

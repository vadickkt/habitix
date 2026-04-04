package com.vadymdev.habitix.domain.usecase

import com.vadymdev.habitix.domain.model.OnboardingState
import com.vadymdev.habitix.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class OnboardingUseCasesTest {

    @Test
    fun observeOnboarding_delegatesToRepositoryFlow() = runBlocking {
        val expectedState = OnboardingState(
            completed = false,
            selectedInterests = setOf("fitness"),
            selectedHabits = setOf("drink_water")
        )
        val repository = FakeOnboardingRepository(observedState = expectedState)
        val useCase = ObserveOnboardingUseCase(repository)

        val result = useCase().first()

        assertEquals(expectedState, result)
        assertSame(repository.observedFlow, useCase())
    }

    @Test
    fun updateInterests_passesValuesToRepository() = runBlocking {
        val repository = FakeOnboardingRepository()
        val values = setOf("mindfulness", "fitness")
        val useCase = UpdateInterestsUseCase(repository)

        useCase(values)

        assertEquals(values, repository.lastInterests)
    }

    @Test
    fun updateHabits_passesValuesToRepository() = runBlocking {
        val repository = FakeOnboardingRepository()
        val values = setOf("read", "walk")
        val useCase = UpdateHabitsUseCase(repository)

        useCase(values)

        assertEquals(values, repository.lastHabits)
    }

    @Test
    fun completeOnboarding_passesSelectedHabitKeysToRepository() = runBlocking {
        val repository = FakeOnboardingRepository()
        val selected = setOf("run", "journal")
        val useCase = CompleteOnboardingUseCase(repository)

        useCase(selected)

        assertEquals(selected, repository.lastCompleted)
    }

    private class FakeOnboardingRepository(
        observedState: OnboardingState = OnboardingState(
            completed = false,
            selectedInterests = emptySet(),
            selectedHabits = emptySet()
        )
    ) : OnboardingRepository {
        val observedFlow: Flow<OnboardingState> = flowOf(observedState)
        var lastInterests: Set<String>? = null
        var lastHabits: Set<String>? = null
        var lastCompleted: Set<String>? = null

        override fun observeOnboardingState(): Flow<OnboardingState> = observedFlow

        override suspend fun updateInterests(values: Set<String>) {
            lastInterests = values
        }

        override suspend fun updateHabits(values: Set<String>) {
            lastHabits = values
        }

        override suspend fun completeOnboarding(selectedHabitKeys: Set<String>) {
            lastCompleted = selectedHabitKeys
        }
    }
}

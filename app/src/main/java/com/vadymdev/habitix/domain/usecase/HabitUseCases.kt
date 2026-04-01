package com.vadymdev.habitix.domain.usecase

import com.vadymdev.habitix.domain.model.Habit
import com.vadymdev.habitix.domain.model.HabitCreateDraft
import com.vadymdev.habitix.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class ObserveHabitsForDateUseCase(private val repository: HabitRepository) {
    operator fun invoke(date: LocalDate): Flow<List<Habit>> = repository.observeHabitsForDate(date)
}

class ToggleHabitCompletionUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(habitId: Long, date: LocalDate, completed: Boolean) {
        repository.toggleHabitCompletion(habitId, date, completed)
    }
}

class CreateHabitUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(draft: HabitCreateDraft) = repository.createHabit(draft)
}

class SeedOnboardingHabitsUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(habitKeys: Set<String>) = repository.seedOnboardingHabits(habitKeys)
}

class GetIncompleteHabitsForDateUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(date: LocalDate): List<Habit> = repository.getIncompleteHabitsForDate(date)
}

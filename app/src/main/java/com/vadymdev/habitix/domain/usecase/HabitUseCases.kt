package com.vadymdev.habitix.domain.usecase

import com.vadymdev.habitix.domain.model.Habit
import com.vadymdev.habitix.domain.model.HabitCreateDraft
import com.vadymdev.habitix.domain.model.HabitStatsSnapshot
import com.vadymdev.habitix.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class ObserveHabitsForDateUseCase(private val repository: HabitRepository) {
    operator fun invoke(date: LocalDate): Flow<List<Habit>> = repository.observeHabitsForDate(date)
}

class ObserveStatsUseCase(private val repository: HabitRepository) {
    operator fun invoke(periodDays: Int, heatmapPeriodDays: Int = periodDays): Flow<HabitStatsSnapshot> =
        repository.observeStats(periodDays, heatmapPeriodDays)
}

class ToggleHabitCompletionUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(habitId: Long, date: LocalDate, completed: Boolean) {
        repository.toggleHabitCompletion(habitId, date, completed)
    }
}

class UpdateHabitUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(habitId: Long, draft: HabitCreateDraft) {
        repository.updateHabit(habitId, draft)
    }
}

class DeactivateHabitFromDateUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(habitId: Long, date: LocalDate) {
        repository.deactivateHabitFromDate(habitId, date)
    }
}

class DeleteAllHabitsUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke() {
        repository.deleteAllHabits()
    }
}

class CreateHabitUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(draft: HabitCreateDraft) = repository.createHabit(draft)
}

class GetIncompleteHabitsForDateUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(date: LocalDate): List<Habit> = repository.getIncompleteHabitsForDate(date)
}

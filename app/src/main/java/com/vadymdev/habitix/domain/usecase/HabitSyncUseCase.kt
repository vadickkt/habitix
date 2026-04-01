package com.vadymdev.habitix.domain.usecase

import com.vadymdev.habitix.domain.repository.HabitSyncRepository

class SyncUserHabitsUseCase(private val repository: HabitSyncRepository) {
    suspend operator fun invoke(userId: String) = repository.syncUserHabits(userId)
}

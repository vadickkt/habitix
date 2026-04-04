package com.vadymdev.habitix.domain.usecase

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class SyncScope {
    FULL,
    SETTINGS_ONLY,
    PROFILE_ONLY,
    HABITS_AND_ACHIEVEMENTS
}

class SyncOrchestratorUseCase(
    private val syncSettingsUseCase: SyncSettingsUseCase,
    private val syncProfileUseCase: SyncProfileUseCase,
    private val syncUserHabitsUseCase: SyncUserHabitsUseCase,
    private val syncAchievementsUseCase: SyncAchievementsUseCase
) {
    private val mutex = Mutex()

    suspend operator fun invoke(userId: String, scope: SyncScope = SyncScope.FULL): Result<Unit> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("User id must not be blank"))
        }

        return runCatching {
            mutex.withLock {
                when (scope) {
                    SyncScope.FULL -> {
                        syncSettingsUseCase(userId)
                        syncProfileUseCase(userId)
                        syncUserHabitsUseCase(userId)
                        syncAchievementsUseCase(userId)
                    }

                    SyncScope.SETTINGS_ONLY -> {
                        syncSettingsUseCase(userId)
                    }

                    SyncScope.PROFILE_ONLY -> {
                        syncProfileUseCase(userId)
                    }

                    SyncScope.HABITS_AND_ACHIEVEMENTS -> {
                        syncUserHabitsUseCase(userId)
                        syncAchievementsUseCase(userId)
                    }
                }
            }
        }
    }
}

package com.vadymdev.habitix.domain.usecase

import com.vadymdev.habitix.domain.model.SyncDomainException
import com.vadymdev.habitix.domain.model.SyncFailureKind
import com.vadymdev.habitix.domain.model.SyncTarget
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
    private val syncAchievementsUseCase: SyncAchievementsUseCase,
    private val maxRetryAttempts: Int = 2,
    private val isNetworkAvailable: suspend () -> Boolean = { true },
    private val onDeferredSyncRequested: suspend () -> Unit = {}
) {
    private val mutex = Mutex()

    suspend operator fun invoke(userId: String, scope: SyncScope = SyncScope.FULL): Result<Unit> {
        if (userId.isBlank()) {
            return Result.failure(
                SyncDomainException(
                    kind = SyncFailureKind.PERMANENT,
                    target = SyncTarget.ORCHESTRATOR,
                    message = "User id must not be blank"
                )
            )
        }

        if (!isNetworkAvailable()) {
            onDeferredSyncRequested()
            return Result.success(Unit)
        }

        val attempts = maxRetryAttempts.coerceAtLeast(1)
        repeat(attempts) { index ->
            val result = runCatching {
                mutex.withLock {
                    runScope(userId, scope)
                }
            }

            if (result.isSuccess) {
                return Result.success(Unit)
            }

            val error = result.exceptionOrNull() ?: return Result.failure(IllegalStateException("Unknown sync failure"))
            if (index == attempts - 1 || !shouldRetry(error)) {
                return Result.failure(error)
            }
        }

        return Result.failure(IllegalStateException("Sync orchestration failed unexpectedly"))
    }

    private suspend fun runScope(userId: String, scope: SyncScope) {
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

    private fun shouldRetry(error: Throwable): Boolean {
        return when (error) {
            is SyncDomainException -> error.kind == SyncFailureKind.TRANSIENT
            is IllegalArgumentException -> false
            else -> true
        }
    }
}

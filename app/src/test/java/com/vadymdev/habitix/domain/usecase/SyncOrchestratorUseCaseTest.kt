package com.vadymdev.habitix.domain.usecase

import com.vadymdev.habitix.domain.model.AccentPalette
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.domain.model.AppSettings
import com.vadymdev.habitix.domain.model.SyncDomainException
import com.vadymdev.habitix.domain.model.SyncFailureKind
import com.vadymdev.habitix.domain.model.SyncTarget
import com.vadymdev.habitix.domain.model.ThemeMode
import com.vadymdev.habitix.domain.repository.AchievementSyncRepository
import com.vadymdev.habitix.domain.repository.HabitSyncRepository
import com.vadymdev.habitix.domain.repository.ProfileSyncRepository
import com.vadymdev.habitix.domain.repository.SettingsRepository
import com.vadymdev.habitix.domain.repository.SettingsSyncRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class SyncOrchestratorUseCaseTest {

    @Test
    fun blankUserId_returnsTypedError() = runBlocking {
        val orchestrator = buildOrchestrator()

        val result = orchestrator("", SyncScope.FULL)

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is SyncDomainException)
        assertEquals(SyncFailureKind.PERMANENT, (error as SyncDomainException).kind)
        assertEquals(SyncTarget.ORCHESTRATOR, error.target)
    }

    @Test
    fun settingsOnly_runsOnlySettingsSync() = runBlocking {
        val recorder = CallRecorder()
        val orchestrator = buildOrchestrator(recorder = recorder)

        val result = orchestrator("uid-1", SyncScope.SETTINGS_ONLY)

        assertTrue(result.isSuccess)
        assertEquals(listOf("settings"), recorder.calls)
    }

    @Test
    fun habitsAndAchievements_runsExpectedOrder() = runBlocking {
        val recorder = CallRecorder()
        val orchestrator = buildOrchestrator(recorder = recorder)

        val result = orchestrator("uid-1", SyncScope.HABITS_AND_ACHIEVEMENTS)

        assertTrue(result.isSuccess)
        assertEquals(listOf("habits", "achievements"), recorder.calls)
    }

    @Test
    fun profileOnly_runsOnlyProfileSync() = runBlocking {
        val recorder = CallRecorder()
        val orchestrator = buildOrchestrator(recorder = recorder)

        val result = orchestrator("uid-1", SyncScope.PROFILE_ONLY)

        assertTrue(result.isSuccess)
        assertEquals(listOf("profile"), recorder.calls)
    }

    @Test
    fun fullScope_runsInDeterministicOrder() = runBlocking {
        val recorder = CallRecorder()
        val orchestrator = buildOrchestrator(recorder = recorder)

        val result = orchestrator("uid-1", SyncScope.FULL)

        assertTrue(result.isSuccess)
        assertEquals(listOf("settings", "profile", "habits", "achievements"), recorder.calls)
    }

    @Test
    fun fullScope_retriesTransientFailure_andEventuallySucceeds() = runBlocking {
        val recorder = CallRecorder()
        val settingsAttempts = AtomicInteger(0)

        val orchestrator = buildOrchestrator(
            recorder = recorder,
            onSettingsSync = {
                if (settingsAttempts.getAndIncrement() == 0) {
                    throw RuntimeException("Transient")
                }
            }
        )

        val result = orchestrator("uid-1", SyncScope.FULL)

        assertTrue(result.isSuccess)
        assertEquals(2, settingsAttempts.get())
        assertTrue(recorder.calls.count { it == "settings" } >= 2)
    }

    @Test
    fun fullScope_mergesConcurrentCalls_withSingleFlight() = runBlocking {
        val inFlight = AtomicInteger(0)
        val maxInFlight = AtomicInteger(0)
        val orchestrator = buildOrchestrator(
            onSettingsSync = {
                val running = inFlight.incrementAndGet()
                while (true) {
                    val currentMax = maxInFlight.get()
                    if (running <= currentMax || maxInFlight.compareAndSet(currentMax, running)) break
                }
                delay(80)
                inFlight.decrementAndGet()
            }
        )

        withContext(Dispatchers.Default) {
            awaitAll(
                async { orchestrator("uid-1", SyncScope.FULL) },
                async { orchestrator("uid-1", SyncScope.FULL) }
            )
        }

        assertEquals(1, maxInFlight.get())
    }

    @Test
    fun noRetryForPermanentSyncError() = runBlocking {
        val typedError = SyncDomainException(
            kind = SyncFailureKind.PERMANENT,
            target = SyncTarget.SETTINGS,
            message = "non-retryable"
        )
        val attempts = AtomicInteger(0)

        val orchestrator = buildOrchestrator(
            maxRetries = 3,
            onSettingsSync = {
                attempts.incrementAndGet()
                throw typedError
            }
        )

        val result = orchestrator("uid-1", SyncScope.SETTINGS_ONLY)

        assertFalse(result.isSuccess)
        assertEquals(1, attempts.get())
        assertTrue(result.exceptionOrNull() is SyncDomainException)
    }

    @Test
    fun noRetryForIllegalArgumentException() = runBlocking {
        val attempts = AtomicInteger(0)
        val orchestrator = buildOrchestrator(
            maxRetries = 4,
            onSettingsSync = {
                attempts.incrementAndGet()
                throw IllegalArgumentException("bad-request")
            }
        )

        val result = orchestrator("uid-1", SyncScope.SETTINGS_ONLY)

        assertTrue(result.isFailure)
        assertEquals(1, attempts.get())
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun transientSyncError_retriesUpToMaxAttempts_thenFails() = runBlocking {
        val attempts = AtomicInteger(0)
        val transientError = SyncDomainException(
            kind = SyncFailureKind.TRANSIENT,
            target = SyncTarget.SETTINGS,
            message = "temporary"
        )

        val orchestrator = buildOrchestrator(
            maxRetries = 3,
            onSettingsSync = {
                attempts.incrementAndGet()
                throw transientError
            }
        )

        val result = orchestrator("uid-1", SyncScope.SETTINGS_ONLY)

        assertTrue(result.isFailure)
        assertEquals(3, attempts.get())
        assertEquals(transientError, result.exceptionOrNull())
    }

    @Test
    fun maxRetriesLessThanOne_isCoercedToSingleAttempt() = runBlocking {
        val attempts = AtomicInteger(0)
        val orchestrator = buildOrchestrator(
            maxRetries = 0,
            onSettingsSync = {
                attempts.incrementAndGet()
                throw RuntimeException("boom")
            }
        )

        val result = orchestrator("uid-1", SyncScope.SETTINGS_ONLY)

        assertTrue(result.isFailure)
        assertEquals(1, attempts.get())
    }

    @Test
    fun offline_shortCircuitsSync_andRequestsDeferredSync() = runBlocking {
        val recorder = CallRecorder()
        val deferredCalls = AtomicInteger(0)

        val orchestrator = buildOrchestrator(
            recorder = recorder,
            networkAvailable = { false },
            onDeferredSyncRequested = { deferredCalls.incrementAndGet() }
        )

        val result = orchestrator("uid-1", SyncScope.FULL)

        assertTrue(result.isSuccess)
        assertTrue(recorder.calls.isEmpty())
        assertEquals(1, deferredCalls.get())
    }

    private fun buildOrchestrator(
        recorder: CallRecorder = CallRecorder(),
        maxRetries: Int = 2,
        networkAvailable: suspend () -> Boolean = { true },
        onDeferredSyncRequested: suspend () -> Unit = {},
        onSettingsSync: suspend () -> Unit = {},
        onProfileSync: suspend () -> Unit = {},
        onHabitsSync: suspend () -> Unit = {},
        onAchievementsSync: suspend () -> Unit = {}
    ): SyncOrchestratorUseCase {
        val settingsRepository = FakeSettingsRepository()
        val settingsSyncRepository = object : SettingsSyncRepository {
            override suspend fun sync(userId: String) {
                recorder.calls.add("settings")
                onSettingsSync()
            }

            override suspend fun clearUserData(userId: String) = Unit
        }

        val profileSyncRepository = object : ProfileSyncRepository {
            override suspend fun sync(userId: String) {
                recorder.calls.add("profile")
                onProfileSync()
            }

            override suspend fun clearUserData(userId: String) = Unit
        }

        val habitSyncRepository = object : HabitSyncRepository {
            override suspend fun syncUserHabits(userId: String) {
                recorder.calls.add("habits")
                onHabitsSync()
            }

            override suspend fun clearUserData(userId: String) = Unit
        }

        val achievementSyncRepository = object : AchievementSyncRepository {
            override suspend fun sync(userId: String) {
                recorder.calls.add("achievements")
                onAchievementsSync()
            }

            override suspend fun clearUserData(userId: String) = Unit
        }

        return SyncOrchestratorUseCase(
            syncSettingsUseCase = SyncSettingsUseCase(settingsSyncRepository, settingsRepository),
            syncProfileUseCase = SyncProfileUseCase(profileSyncRepository, settingsRepository),
            syncUserHabitsUseCase = SyncUserHabitsUseCase(habitSyncRepository, settingsRepository),
            syncAchievementsUseCase = SyncAchievementsUseCase(achievementSyncRepository, settingsRepository),
            maxRetryAttempts = maxRetries,
            isNetworkAvailable = networkAvailable,
            onDeferredSyncRequested = onDeferredSyncRequested
        )
    }

    private class FakeSettingsRepository : SettingsRepository {
        override fun observeSettings() = kotlinx.coroutines.flow.flowOf(AppSettings())
        override suspend fun getCurrentSettings(): AppSettings = AppSettings()
        override suspend fun replaceAll(settings: AppSettings) = Unit
        override suspend fun resetToDefaults() = Unit
        override suspend fun setThemeMode(mode: ThemeMode) = Unit
        override suspend fun setAccentPalette(palette: AccentPalette) = Unit
        override suspend fun setLanguage(language: AppLanguage) = Unit
        override suspend fun setPushEnabled(enabled: Boolean) = Unit
        override suspend fun setReminderTime(hour: Int, minute: Int) = Unit
        override suspend fun setSoundsEnabled(enabled: Boolean) = Unit
        override suspend fun setVibrationEnabled(enabled: Boolean) = Unit
        override suspend fun setBiometricEnabled(enabled: Boolean) = Unit
        override suspend fun setAutoSyncEnabled(enabled: Boolean) = Unit
    }

    private class CallRecorder {
        val calls = mutableListOf<String>()
    }
}

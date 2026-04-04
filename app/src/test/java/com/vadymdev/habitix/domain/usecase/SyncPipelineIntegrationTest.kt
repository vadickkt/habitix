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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class SyncPipelineIntegrationTest {

    @Test
    fun fullSync_autoSyncDisabled_skipsEntirePipeline() = runBlocking {
        val calls = mutableListOf<String>()
        val orchestrator = buildOrchestrator(
            autoSyncEnabled = false,
            calls = calls
        )

        val result = orchestrator("uid", SyncScope.FULL)

        assertTrue(result.isSuccess)
        assertTrue(calls.isEmpty())
    }

    @Test
    fun fullSync_transientSettingsFailure_retriesAndKeepsExecutionOrder() = runBlocking {
        val calls = mutableListOf<String>()
        val settingsAttempts = AtomicInteger(0)
        val orchestrator = buildOrchestrator(
            autoSyncEnabled = true,
            maxRetries = 2,
            calls = calls,
            onSettings = {
                calls.add("settings")
                if (settingsAttempts.getAndIncrement() == 0) {
                    throw SyncDomainException(
                        kind = SyncFailureKind.TRANSIENT,
                        target = SyncTarget.SETTINGS,
                        message = "offline"
                    )
                }
            }
        )

        val result = orchestrator("uid", SyncScope.FULL)

        assertTrue(result.isSuccess)
        assertEquals(2, settingsAttempts.get())
        assertEquals(
            listOf(
                "settings",
                "settings",
                "profile",
                "habits",
                "achievements"
            ),
            calls
        )
    }

    @Test
    fun fullSync_permanentProfileFailure_stopsPipelineWithoutRetry() = runBlocking {
        val calls = mutableListOf<String>()
        val profileAttempts = AtomicInteger(0)
        val orchestrator = buildOrchestrator(
            autoSyncEnabled = true,
            maxRetries = 3,
            calls = calls,
            onProfile = {
                calls.add("profile")
                profileAttempts.incrementAndGet()
                throw SyncDomainException(
                    kind = SyncFailureKind.PERMANENT,
                    target = SyncTarget.PROFILE,
                    message = "bad data"
                )
            }
        )

        val result = orchestrator("uid", SyncScope.FULL)

        assertTrue(result.isFailure)
        assertEquals(1, profileAttempts.get())
        assertEquals(listOf("settings", "profile"), calls)
    }

    @Test
    fun habitsAndAchievementsScope_runsOnlyMutatingScopes() = runBlocking {
        val calls = mutableListOf<String>()
        val orchestrator = buildOrchestrator(
            autoSyncEnabled = true,
            calls = calls
        )

        val result = orchestrator("uid", SyncScope.HABITS_AND_ACHIEVEMENTS)

        assertTrue(result.isSuccess)
        assertEquals(listOf("habits", "achievements"), calls)
    }

    private fun buildOrchestrator(
        autoSyncEnabled: Boolean,
        maxRetries: Int = 2,
        calls: MutableList<String>,
        onSettings: suspend () -> Unit = { calls.add("settings") },
        onProfile: suspend () -> Unit = { calls.add("profile") },
        onHabits: suspend () -> Unit = { calls.add("habits") },
        onAchievements: suspend () -> Unit = { calls.add("achievements") }
    ): SyncOrchestratorUseCase {
        val settingsRepository = FakeSettingsRepository(autoSyncEnabled)

        val settingsSyncRepository = object : SettingsSyncRepository {
            override suspend fun sync(userId: String) = onSettings()
            override suspend fun clearUserData(userId: String) = Unit
        }

        val profileSyncRepository = object : ProfileSyncRepository {
            override suspend fun sync(userId: String) = onProfile()
            override suspend fun clearUserData(userId: String) = Unit
        }

        val habitSyncRepository = object : HabitSyncRepository {
            override suspend fun syncUserHabits(userId: String) = onHabits()
            override suspend fun clearUserData(userId: String) = Unit
        }

        val achievementSyncRepository = object : AchievementSyncRepository {
            override suspend fun sync(userId: String) = onAchievements()
            override suspend fun clearUserData(userId: String) = Unit
        }

        return SyncOrchestratorUseCase(
            syncSettingsUseCase = SyncSettingsUseCase(settingsSyncRepository, settingsRepository),
            syncProfileUseCase = SyncProfileUseCase(profileSyncRepository, settingsRepository),
            syncUserHabitsUseCase = SyncUserHabitsUseCase(habitSyncRepository, settingsRepository),
            syncAchievementsUseCase = SyncAchievementsUseCase(achievementSyncRepository, settingsRepository),
            maxRetryAttempts = maxRetries
        )
    }

    private class FakeSettingsRepository(
        private val autoSyncEnabled: Boolean
    ) : SettingsRepository {
        override fun observeSettings() = flowOf(AppSettings(autoSyncEnabled = autoSyncEnabled))
        override suspend fun getCurrentSettings(): AppSettings = AppSettings(autoSyncEnabled = autoSyncEnabled)
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
}

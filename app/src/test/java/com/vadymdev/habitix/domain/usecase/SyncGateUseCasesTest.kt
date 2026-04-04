package com.vadymdev.habitix.domain.usecase

import com.vadymdev.habitix.domain.model.AccentPalette
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.domain.model.AppSettings
import com.vadymdev.habitix.domain.model.ThemeMode
import com.vadymdev.habitix.domain.repository.AchievementSyncRepository
import com.vadymdev.habitix.domain.repository.HabitSyncRepository
import com.vadymdev.habitix.domain.repository.ProfileSyncRepository
import com.vadymdev.habitix.domain.repository.SettingsRepository
import com.vadymdev.habitix.domain.repository.SettingsSyncRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean

class SyncGateUseCasesTest {

    @Test
    fun syncSettings_enabled_callsRepository() = runBlocking {
        val called = AtomicBoolean(false)
        val useCase = SyncSettingsUseCase(
            repository = object : SettingsSyncRepository {
                override suspend fun sync(userId: String) {
                    called.set(true)
                }

                override suspend fun clearUserData(userId: String) = Unit
            },
            settingsRepository = FakeSettingsRepository(autoSyncEnabled = true)
        )

        useCase("uid")

        assertTrue(called.get())
    }

    @Test
    fun syncSettings_disabled_skipsRepository() = runBlocking {
        val called = AtomicBoolean(false)
        val useCase = SyncSettingsUseCase(
            repository = object : SettingsSyncRepository {
                override suspend fun sync(userId: String) {
                    called.set(true)
                }

                override suspend fun clearUserData(userId: String) = Unit
            },
            settingsRepository = FakeSettingsRepository(autoSyncEnabled = false)
        )

        useCase("uid")

        assertFalse(called.get())
    }

    @Test
    fun syncProfile_enabled_callsRepository() = runBlocking {
        val called = AtomicBoolean(false)
        val useCase = SyncProfileUseCase(
            repository = object : ProfileSyncRepository {
                override suspend fun sync(userId: String) {
                    called.set(true)
                }

                override suspend fun clearUserData(userId: String) = Unit
            },
            settingsRepository = FakeSettingsRepository(autoSyncEnabled = true)
        )

        useCase("uid")

        assertTrue(called.get())
    }

    @Test
    fun syncProfile_disabled_skipsRepository() = runBlocking {
        val called = AtomicBoolean(false)
        val useCase = SyncProfileUseCase(
            repository = object : ProfileSyncRepository {
                override suspend fun sync(userId: String) {
                    called.set(true)
                }

                override suspend fun clearUserData(userId: String) = Unit
            },
            settingsRepository = FakeSettingsRepository(autoSyncEnabled = false)
        )

        useCase("uid")

        assertFalse(called.get())
    }

    @Test
    fun syncHabits_enabled_callsRepository() = runBlocking {
        val called = AtomicBoolean(false)
        val useCase = SyncUserHabitsUseCase(
            repository = object : HabitSyncRepository {
                override suspend fun syncUserHabits(userId: String) {
                    called.set(true)
                }

                override suspend fun clearUserData(userId: String) = Unit
            },
            settingsRepository = FakeSettingsRepository(autoSyncEnabled = true)
        )

        useCase("uid")

        assertTrue(called.get())
    }

    @Test
    fun syncHabits_disabled_skipsRepository() = runBlocking {
        val called = AtomicBoolean(false)
        val useCase = SyncUserHabitsUseCase(
            repository = object : HabitSyncRepository {
                override suspend fun syncUserHabits(userId: String) {
                    called.set(true)
                }

                override suspend fun clearUserData(userId: String) = Unit
            },
            settingsRepository = FakeSettingsRepository(autoSyncEnabled = false)
        )

        useCase("uid")

        assertFalse(called.get())
    }

    @Test
    fun syncAchievements_enabled_callsRepository() = runBlocking {
        val called = AtomicBoolean(false)
        val useCase = SyncAchievementsUseCase(
            repository = object : AchievementSyncRepository {
                override suspend fun sync(userId: String) {
                    called.set(true)
                }

                override suspend fun clearUserData(userId: String) = Unit
            },
            settingsRepository = FakeSettingsRepository(autoSyncEnabled = true)
        )

        useCase("uid")

        assertTrue(called.get())
    }

    @Test
    fun syncAchievements_disabled_skipsRepository() = runBlocking {
        val called = AtomicBoolean(false)
        val useCase = SyncAchievementsUseCase(
            repository = object : AchievementSyncRepository {
                override suspend fun sync(userId: String) {
                    called.set(true)
                }

                override suspend fun clearUserData(userId: String) = Unit
            },
            settingsRepository = FakeSettingsRepository(autoSyncEnabled = false)
        )

        useCase("uid")

        assertFalse(called.get())
    }

    private class FakeSettingsRepository(
        private val autoSyncEnabled: Boolean
    ) : SettingsRepository {
        override fun observeSettings() = flowOf(AppSettings(autoSyncEnabled = autoSyncEnabled))

        override suspend fun getCurrentSettings(): AppSettings =
            AppSettings(autoSyncEnabled = autoSyncEnabled)

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

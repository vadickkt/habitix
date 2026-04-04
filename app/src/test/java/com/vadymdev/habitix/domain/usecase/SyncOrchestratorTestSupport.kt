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

internal fun buildSyncOrchestrator(
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

internal class CallRecorder {
    val calls = mutableListOf<String>()
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

package com.vadymdev.habitix.presentation.settings

import com.vadymdev.habitix.domain.model.AccentPalette
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.domain.model.AppSettings
import com.vadymdev.habitix.domain.model.Habit
import com.vadymdev.habitix.domain.model.HabitCreateDraft
import com.vadymdev.habitix.domain.model.HabitStatsSnapshot
import com.vadymdev.habitix.domain.model.ProfileAnalytics
import com.vadymdev.habitix.domain.model.ProfileIdentity
import com.vadymdev.habitix.domain.model.ThemeMode
import com.vadymdev.habitix.domain.model.UserSession
import com.vadymdev.habitix.domain.repository.AchievementSyncRepository
import com.vadymdev.habitix.domain.repository.AuthRepository
import com.vadymdev.habitix.domain.repository.HabitRepository
import com.vadymdev.habitix.domain.repository.HabitSyncRepository
import com.vadymdev.habitix.domain.repository.ProfileRepository
import com.vadymdev.habitix.domain.repository.ProfileSyncRepository
import com.vadymdev.habitix.domain.repository.SettingsRepository
import com.vadymdev.habitix.domain.repository.SettingsSyncRepository
import com.vadymdev.habitix.domain.usecase.DeleteAccountUseCase
import com.vadymdev.habitix.domain.usecase.DeleteAllHabitsUseCase
import com.vadymdev.habitix.domain.usecase.DeleteDataUseCase
import com.vadymdev.habitix.domain.usecase.ObserveAuthSessionUseCase
import com.vadymdev.habitix.domain.usecase.ObserveSettingsUseCase
import com.vadymdev.habitix.domain.usecase.SetAccentPaletteUseCase
import com.vadymdev.habitix.domain.usecase.SetAutoSyncEnabledUseCase
import com.vadymdev.habitix.domain.usecase.SetBiometricEnabledUseCase
import com.vadymdev.habitix.domain.usecase.SetLanguageUseCase
import com.vadymdev.habitix.domain.usecase.SetPushEnabledUseCase
import com.vadymdev.habitix.domain.usecase.SetReminderTimeUseCase
import com.vadymdev.habitix.domain.usecase.SetSoundsEnabledUseCase
import com.vadymdev.habitix.domain.usecase.SetThemeModeUseCase
import com.vadymdev.habitix.domain.usecase.SetVibrationEnabledUseCase
import com.vadymdev.habitix.domain.usecase.SignOutUseCase
import com.vadymdev.habitix.domain.usecase.SyncAchievementsUseCase
import com.vadymdev.habitix.domain.usecase.SyncOrchestratorUseCase
import com.vadymdev.habitix.domain.usecase.SyncProfileUseCase
import com.vadymdev.habitix.domain.usecase.SyncSettingsUseCase
import com.vadymdev.habitix.domain.usecase.SyncUserHabitsUseCase
import com.vadymdev.habitix.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelIntegrationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun enablingAutoSync_withAuthorizedUser_triggersSettingsSyncScope() = runTest {
        val authRepo = FakeAuthRepository(UserSession("uid-1", null, null, null))
        val settingsRepo = FakeSettingsRepository(AppSettings(autoSyncEnabled = false))
        val settingsSyncCalls = AtomicInteger(0)

        val orchestrator = buildOrchestrator(
            settingsRepository = settingsRepo,
            onSettingsSync = { settingsSyncCalls.incrementAndGet() }
        )

        val viewModel = buildViewModel(authRepo, settingsRepo, orchestrator)

        viewModel.setAutoSyncEnabled(true)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.settings.autoSyncEnabled)
        assertEquals(1, settingsSyncCalls.get())
    }

    @Test
    fun enablingAutoSync_withGuestSession_doesNotTriggerServerSync() = runTest {
        val authRepo = FakeAuthRepository(null)
        val settingsRepo = FakeSettingsRepository(AppSettings(autoSyncEnabled = false))
        val settingsSyncCalls = AtomicInteger(0)

        val orchestrator = buildOrchestrator(
            settingsRepository = settingsRepo,
            onSettingsSync = { settingsSyncCalls.incrementAndGet() }
        )

        val viewModel = buildViewModel(authRepo, settingsRepo, orchestrator)

        viewModel.setAutoSyncEnabled(true)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.settings.autoSyncEnabled)
        assertEquals(0, settingsSyncCalls.get())
    }

    @Test
    fun changingLanguage_withGuestSession_doesNotTriggerServerSync() = runTest {
        val authRepo = FakeAuthRepository(null)
        val settingsRepo = FakeSettingsRepository(AppSettings(autoSyncEnabled = true))
        val settingsSyncCalls = AtomicInteger(0)

        val orchestrator = buildOrchestrator(
            settingsRepository = settingsRepo,
            onSettingsSync = { settingsSyncCalls.incrementAndGet() }
        )

        val viewModel = buildViewModel(authRepo, settingsRepo, orchestrator)

        viewModel.setLanguage(AppLanguage.EN)
        advanceUntilIdle()

        assertEquals(AppLanguage.EN, viewModel.state.value.settings.language)
        assertEquals(0, settingsSyncCalls.get())
    }

    @Test
    fun changingPushSetting_withGuestSession_doesNotTriggerServerSync() = runTest {
        val authRepo = FakeAuthRepository(null)
        val settingsRepo = FakeSettingsRepository(AppSettings(autoSyncEnabled = true, pushEnabled = true))
        val settingsSyncCalls = AtomicInteger(0)

        val orchestrator = buildOrchestrator(
            settingsRepository = settingsRepo,
            onSettingsSync = { settingsSyncCalls.incrementAndGet() }
        )

        val viewModel = buildViewModel(authRepo, settingsRepo, orchestrator)

        viewModel.setPushEnabled(false)
        advanceUntilIdle()

        assertTrue(!viewModel.state.value.settings.pushEnabled)
        assertEquals(0, settingsSyncCalls.get())
    }

    @Test
    fun changingReminderTime_withGuestSession_doesNotTriggerServerSync() = runTest {
        val authRepo = FakeAuthRepository(null)
        val settingsRepo = FakeSettingsRepository(AppSettings(autoSyncEnabled = true, reminderHour = 9, reminderMinute = 0))
        val settingsSyncCalls = AtomicInteger(0)

        val orchestrator = buildOrchestrator(
            settingsRepository = settingsRepo,
            onSettingsSync = { settingsSyncCalls.incrementAndGet() }
        )

        val viewModel = buildViewModel(authRepo, settingsRepo, orchestrator)

        viewModel.setReminderTime(7, 45)
        advanceUntilIdle()

        assertEquals(7, viewModel.state.value.settings.reminderHour)
        assertEquals(45, viewModel.state.value.settings.reminderMinute)
        assertEquals(0, settingsSyncCalls.get())
    }

    @Test
    fun deleteData_success_transitionsToSuccessPhase() = runTest {
        val authRepo = FakeAuthRepository(UserSession("uid-2", null, null, null))
        val settingsRepo = FakeSettingsRepository(AppSettings(autoSyncEnabled = true))
        val orchestrator = buildOrchestrator(settingsRepo)

        val viewModel = buildViewModel(authRepo, settingsRepo, orchestrator)

        viewModel.deleteData()
        advanceUntilIdle()

        assertEquals(DeleteDataPhase.SUCCESS, viewModel.state.value.deleteData.phase)
        assertEquals(3, viewModel.state.value.deleteData.stepIndex)
    }

    private fun buildViewModel(
        authRepo: FakeAuthRepository,
        settingsRepo: FakeSettingsRepository,
        orchestrator: SyncOrchestratorUseCase
    ): SettingsViewModel {
        val habitRepo = object : HabitRepository {
            override fun observeHabitsForDate(date: LocalDate): Flow<List<Habit>> = emptyFlow()
            override fun observeStats(periodDays: Int): Flow<HabitStatsSnapshot> = emptyFlow()
            override fun observeProfileAnalytics(): Flow<ProfileAnalytics> = emptyFlow()
            override suspend fun toggleHabitCompletion(habitId: Long, date: LocalDate, completed: Boolean) = Unit
            override suspend fun updateHabit(habitId: Long, draft: HabitCreateDraft) = Unit
            override suspend fun hideHabitForDate(habitId: Long, date: LocalDate) = Unit
            override suspend fun deactivateHabitFromDate(habitId: Long, date: LocalDate) = Unit
            override suspend fun deleteAllHabits() = Unit
            override suspend fun createHabit(draft: HabitCreateDraft) = Unit
            override suspend fun seedOnboardingHabits(habitKeys: Set<String>) = Unit
            override suspend fun getIncompleteHabitsForDate(date: LocalDate): List<Habit> = emptyList()
        }
        val profileRepo = object : ProfileRepository {
            private val state = MutableStateFlow(ProfileIdentity("", "", "", null, 0L))
            override fun observeProfileIdentity(): Flow<ProfileIdentity> = state
            override suspend fun getCurrentProfileIdentity(): ProfileIdentity = state.value
            override suspend fun replaceProfileIdentity(displayName: String, bio: String, updatedAtMillis: Long) = Unit
            override suspend fun updateDisplayName(name: String) = Unit
            override suspend fun updateBio(bio: String) = Unit
            override suspend fun updateAvatarUri(uri: String?) = Unit
            override suspend fun clearLocalData() = Unit
        }
        val habitSync = object : HabitSyncRepository {
            override suspend fun syncUserHabits(userId: String) = Unit
            override suspend fun clearUserData(userId: String) = Unit
        }
        val profileSync = object : ProfileSyncRepository {
            override suspend fun sync(userId: String) = Unit
            override suspend fun clearUserData(userId: String) = Unit
        }
        val settingsSync = object : SettingsSyncRepository {
            override suspend fun sync(userId: String) = Unit
            override suspend fun clearUserData(userId: String) = Unit
        }
        val achievementSync = object : AchievementSyncRepository {
            override suspend fun sync(userId: String) = Unit
            override suspend fun clearUserData(userId: String) = Unit
        }

        val deleteAllHabitsUseCase = DeleteAllHabitsUseCase(habitRepo)

        val deleteDataUseCase = DeleteDataUseCase(
            deleteAllHabitsUseCase = deleteAllHabitsUseCase,
            profileRepository = profileRepo,
            settingsRepository = settingsRepo,
            habitSyncRepository = habitSync,
            profileSyncRepository = profileSync,
            settingsSyncRepository = settingsSync,
            achievementSyncRepository = achievementSync
        )

        return SettingsViewModel(
            observeSettingsUseCase = ObserveSettingsUseCase(settingsRepo),
            observeAuthSessionUseCase = ObserveAuthSessionUseCase(authRepo),
            setThemeModeUseCase = SetThemeModeUseCase(settingsRepo),
            setAccentPaletteUseCase = SetAccentPaletteUseCase(settingsRepo),
            setLanguageUseCase = SetLanguageUseCase(settingsRepo),
            setPushEnabledUseCase = SetPushEnabledUseCase(settingsRepo),
            setReminderTimeUseCase = SetReminderTimeUseCase(settingsRepo),
            setSoundsEnabledUseCase = SetSoundsEnabledUseCase(settingsRepo),
            setVibrationEnabledUseCase = SetVibrationEnabledUseCase(settingsRepo),
            setBiometricEnabledUseCase = SetBiometricEnabledUseCase(settingsRepo),
            setAutoSyncEnabledUseCase = SetAutoSyncEnabledUseCase(settingsRepo),
            syncOrchestratorUseCase = orchestrator,
            signOutUseCase = SignOutUseCase(authRepo),
            deleteAccountUseCase = DeleteAccountUseCase(authRepo),
            deleteDataUseCase = deleteDataUseCase
        )
    }

    private fun buildOrchestrator(
        settingsRepository: SettingsRepository,
        onSettingsSync: suspend () -> Unit = {}
    ): SyncOrchestratorUseCase {
        val settingsSyncRepository = object : SettingsSyncRepository {
            override suspend fun sync(userId: String) = onSettingsSync()
            override suspend fun clearUserData(userId: String) = Unit
        }
        val profileSyncRepository = object : ProfileSyncRepository {
            override suspend fun sync(userId: String) = Unit
            override suspend fun clearUserData(userId: String) = Unit
        }
        val habitSyncRepository = object : HabitSyncRepository {
            override suspend fun syncUserHabits(userId: String) = Unit
            override suspend fun clearUserData(userId: String) = Unit
        }
        val achievementSyncRepository = object : AchievementSyncRepository {
            override suspend fun sync(userId: String) = Unit
            override suspend fun clearUserData(userId: String) = Unit
        }

        return SyncOrchestratorUseCase(
            syncSettingsUseCase = SyncSettingsUseCase(settingsSyncRepository, settingsRepository),
            syncProfileUseCase = SyncProfileUseCase(profileSyncRepository, settingsRepository),
            syncUserHabitsUseCase = SyncUserHabitsUseCase(habitSyncRepository, settingsRepository),
            syncAchievementsUseCase = SyncAchievementsUseCase(achievementSyncRepository, settingsRepository)
        )
    }

    private class FakeAuthRepository(initialSession: UserSession?) : AuthRepository {
        private val session = MutableStateFlow(initialSession)
        private val guest = MutableStateFlow(false)

        override fun observeSession(): Flow<UserSession?> = session
        override fun observeGuestMode(): Flow<Boolean> = guest
        override suspend fun signInWithGoogle(idToken: String): Result<UserSession> = Result.failure(UnsupportedOperationException())
        override suspend fun continueAsGuest() {
            guest.value = true
        }

        override fun getCurrentSession(): UserSession? = session.value

        override suspend fun signOut() {
            session.value = null
            guest.value = false
        }

        override suspend fun deleteAccount(): Result<Unit> = Result.success(Unit)
    }

    private class FakeSettingsRepository(initial: AppSettings) : SettingsRepository {
        private val state = MutableStateFlow(initial)

        override fun observeSettings(): Flow<AppSettings> = state
        override suspend fun getCurrentSettings(): AppSettings = state.value
        override suspend fun replaceAll(settings: AppSettings) {
            state.value = settings
        }

        override suspend fun resetToDefaults() {
            state.value = AppSettings()
        }

        override suspend fun setThemeMode(mode: ThemeMode) {
            state.value = state.value.copy(themeMode = mode)
        }

        override suspend fun setAccentPalette(palette: AccentPalette) {
            state.value = state.value.copy(accentPalette = palette)
        }

        override suspend fun setLanguage(language: AppLanguage) {
            state.value = state.value.copy(language = language)
        }

        override suspend fun setPushEnabled(enabled: Boolean) {
            state.value = state.value.copy(pushEnabled = enabled)
        }

        override suspend fun setReminderTime(hour: Int, minute: Int) {
            state.value = state.value.copy(reminderHour = hour, reminderMinute = minute)
        }

        override suspend fun setSoundsEnabled(enabled: Boolean) {
            state.value = state.value.copy(soundsEnabled = enabled)
        }

        override suspend fun setVibrationEnabled(enabled: Boolean) {
            state.value = state.value.copy(vibrationEnabled = enabled)
        }

        override suspend fun setBiometricEnabled(enabled: Boolean) {
            state.value = state.value.copy(biometricEnabled = enabled)
        }

        override suspend fun setAutoSyncEnabled(enabled: Boolean) {
            state.value = state.value.copy(autoSyncEnabled = enabled)
        }
    }
}

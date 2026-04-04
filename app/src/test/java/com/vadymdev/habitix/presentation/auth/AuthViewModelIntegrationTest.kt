package com.vadymdev.habitix.presentation.auth

import com.vadymdev.habitix.domain.model.AccentPalette
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.domain.model.AppSettings
import com.vadymdev.habitix.domain.model.ThemeMode
import com.vadymdev.habitix.domain.model.UserSession
import com.vadymdev.habitix.domain.repository.AchievementSyncRepository
import com.vadymdev.habitix.domain.repository.AuthRepository
import com.vadymdev.habitix.domain.repository.HabitSyncRepository
import com.vadymdev.habitix.domain.repository.ProfileSyncRepository
import com.vadymdev.habitix.domain.repository.SettingsRepository
import com.vadymdev.habitix.domain.repository.SettingsSyncRepository
import com.vadymdev.habitix.domain.usecase.ContinueAsGuestUseCase
import com.vadymdev.habitix.domain.usecase.SignInWithGoogleUseCase
import com.vadymdev.habitix.domain.usecase.SyncAchievementsUseCase
import com.vadymdev.habitix.domain.usecase.SyncOrchestratorUseCase
import com.vadymdev.habitix.domain.usecase.SyncProfileUseCase
import com.vadymdev.habitix.domain.usecase.SyncSettingsUseCase
import com.vadymdev.habitix.domain.usecase.SyncUserHabitsUseCase
import com.vadymdev.habitix.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelIntegrationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun signInSuccess_transitionsToAuthorizedState() = runTest {
        val authRepository = FakeAuthRepository(signInResult = Result.success(UserSession("uid", "User", "u@e.com", null)))
        val viewModel = AuthViewModel(
            signInWithGoogleUseCase = SignInWithGoogleUseCase(authRepository),
            continueAsGuestUseCase = ContinueAsGuestUseCase(authRepository),
            syncOrchestratorUseCase = buildNoOpOrchestrator()
        )

        viewModel.signInWithGoogleToken("token")
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isAuthorized)
        assertFalse(viewModel.state.value.isLoading)
        assertFalse(viewModel.state.value.showLoadingFlow)
    }

    @Test
    fun signInFailure_setsErrorAndStopsLoading() = runTest {
        val authRepository = FakeAuthRepository(signInResult = Result.failure(RuntimeException("Google failed")))
        val viewModel = AuthViewModel(
            signInWithGoogleUseCase = SignInWithGoogleUseCase(authRepository),
            continueAsGuestUseCase = ContinueAsGuestUseCase(authRepository),
            syncOrchestratorUseCase = buildNoOpOrchestrator()
        )

        viewModel.signInWithGoogleToken("token")
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isAuthorized)
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.error?.contains("Google failed") == true)
    }

    private fun buildNoOpOrchestrator(): SyncOrchestratorUseCase {
        val settingsRepository = object : SettingsRepository {
            override fun observeSettings(): Flow<AppSettings> = flowOf(AppSettings(autoSyncEnabled = true))
            override suspend fun getCurrentSettings(): AppSettings = AppSettings(autoSyncEnabled = true)
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

        val settingsSyncRepository = object : SettingsSyncRepository {
            override suspend fun sync(userId: String) = Unit
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

    private class FakeAuthRepository(
        private val signInResult: Result<UserSession>
    ) : AuthRepository {
        private val sessionFlow = MutableStateFlow<UserSession?>(null)
        private val guestFlow = MutableStateFlow(false)

        override fun observeSession(): Flow<UserSession?> = sessionFlow
        override fun observeGuestMode(): Flow<Boolean> = guestFlow

        override suspend fun signInWithGoogle(idToken: String): Result<UserSession> {
            signInResult.onSuccess { sessionFlow.value = it }
            return signInResult
        }

        override suspend fun continueAsGuest() {
            guestFlow.value = true
        }

        override fun getCurrentSession(): UserSession? = sessionFlow.value

        override suspend fun signOut() {
            sessionFlow.value = null
            guestFlow.value = false
        }

        override suspend fun deleteAccount(): Result<Unit> = Result.success(Unit)
    }
}

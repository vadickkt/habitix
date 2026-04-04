package com.vadymdev.habitix.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.domain.model.UserSession
import com.vadymdev.habitix.domain.usecase.ContinueAsGuestUseCase
import com.vadymdev.habitix.domain.usecase.SignInWithGoogleUseCase
import com.vadymdev.habitix.domain.usecase.SyncOrchestratorUseCase
import com.vadymdev.habitix.presentation.auth.AuthScreen
import com.vadymdev.habitix.presentation.auth.AuthViewModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class AuthFallbackGuestUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun googleFailure_showsGuestFallback_andGuestFlowWorks() {
        val guestDoneCalls = AtomicInteger(0)
        val viewModel = AuthViewModel(
            signInWithGoogleUseCase = SignInWithGoogleUseCase(
                authRepository = object : com.vadymdev.habitix.domain.repository.AuthRepository {
                    override fun observeSession() = kotlinx.coroutines.flow.flowOf<UserSession?>(null)
                    override fun observeGuestMode() = kotlinx.coroutines.flow.flowOf(false)
                    override suspend fun signInWithGoogle(idToken: String) = Result.failure<UserSession>(IllegalStateException("not used"))
                    override suspend fun continueAsGuest() = Unit
                    override fun getCurrentSession(): UserSession? = null
                    override suspend fun signOut() = Unit
                    override suspend fun deleteAccount() = Result.success(Unit)
                }
            ),
            continueAsGuestUseCase = ContinueAsGuestUseCase(
                authRepository = object : com.vadymdev.habitix.domain.repository.AuthRepository {
                    override fun observeSession() = kotlinx.coroutines.flow.flowOf<UserSession?>(null)
                    override fun observeGuestMode() = kotlinx.coroutines.flow.flowOf(false)
                    override suspend fun signInWithGoogle(idToken: String) = Result.failure<UserSession>(IllegalStateException("not used"))
                    override suspend fun continueAsGuest() = Unit
                    override fun getCurrentSession(): UserSession? = null
                    override suspend fun signOut() = Unit
                    override suspend fun deleteAccount() = Result.success(Unit)
                }
            ),
            syncOrchestratorUseCase = SyncOrchestratorUseCase(
                syncSettingsUseCase = com.vadymdev.habitix.domain.usecase.SyncSettingsUseCase(
                    repository = object : com.vadymdev.habitix.domain.repository.SettingsSyncRepository {
                        override suspend fun sync(userId: String) = Unit
                        override suspend fun clearUserData(userId: String) = Unit
                    },
                    settingsRepository = object : com.vadymdev.habitix.domain.repository.SettingsRepository {
                        override fun observeSettings() = kotlinx.coroutines.flow.flowOf(com.vadymdev.habitix.domain.model.AppSettings())
                        override suspend fun getCurrentSettings() = com.vadymdev.habitix.domain.model.AppSettings()
                        override suspend fun replaceAll(settings: com.vadymdev.habitix.domain.model.AppSettings) = Unit
                        override suspend fun resetToDefaults() = Unit
                        override suspend fun setThemeMode(mode: com.vadymdev.habitix.domain.model.ThemeMode) = Unit
                        override suspend fun setAccentPalette(palette: com.vadymdev.habitix.domain.model.AccentPalette) = Unit
                        override suspend fun setLanguage(language: AppLanguage) = Unit
                        override suspend fun setPushEnabled(enabled: Boolean) = Unit
                        override suspend fun setReminderTime(hour: Int, minute: Int) = Unit
                        override suspend fun setSoundsEnabled(enabled: Boolean) = Unit
                        override suspend fun setVibrationEnabled(enabled: Boolean) = Unit
                        override suspend fun setBiometricEnabled(enabled: Boolean) = Unit
                        override suspend fun setAutoSyncEnabled(enabled: Boolean) = Unit
                    }
                ),
                syncProfileUseCase = com.vadymdev.habitix.domain.usecase.SyncProfileUseCase(
                    repository = object : com.vadymdev.habitix.domain.repository.ProfileSyncRepository {
                        override suspend fun sync(userId: String) = Unit
                        override suspend fun clearUserData(userId: String) = Unit
                    },
                    settingsRepository = object : com.vadymdev.habitix.domain.repository.SettingsRepository {
                        override fun observeSettings() = kotlinx.coroutines.flow.flowOf(com.vadymdev.habitix.domain.model.AppSettings())
                        override suspend fun getCurrentSettings() = com.vadymdev.habitix.domain.model.AppSettings()
                        override suspend fun replaceAll(settings: com.vadymdev.habitix.domain.model.AppSettings) = Unit
                        override suspend fun resetToDefaults() = Unit
                        override suspend fun setThemeMode(mode: com.vadymdev.habitix.domain.model.ThemeMode) = Unit
                        override suspend fun setAccentPalette(palette: com.vadymdev.habitix.domain.model.AccentPalette) = Unit
                        override suspend fun setLanguage(language: AppLanguage) = Unit
                        override suspend fun setPushEnabled(enabled: Boolean) = Unit
                        override suspend fun setReminderTime(hour: Int, minute: Int) = Unit
                        override suspend fun setSoundsEnabled(enabled: Boolean) = Unit
                        override suspend fun setVibrationEnabled(enabled: Boolean) = Unit
                        override suspend fun setBiometricEnabled(enabled: Boolean) = Unit
                        override suspend fun setAutoSyncEnabled(enabled: Boolean) = Unit
                    }
                ),
                syncUserHabitsUseCase = com.vadymdev.habitix.domain.usecase.SyncUserHabitsUseCase(
                    repository = object : com.vadymdev.habitix.domain.repository.HabitSyncRepository {
                        override suspend fun syncUserHabits(userId: String) = Unit
                        override suspend fun clearUserData(userId: String) = Unit
                    },
                    settingsRepository = object : com.vadymdev.habitix.domain.repository.SettingsRepository {
                        override fun observeSettings() = kotlinx.coroutines.flow.flowOf(com.vadymdev.habitix.domain.model.AppSettings())
                        override suspend fun getCurrentSettings() = com.vadymdev.habitix.domain.model.AppSettings()
                        override suspend fun replaceAll(settings: com.vadymdev.habitix.domain.model.AppSettings) = Unit
                        override suspend fun resetToDefaults() = Unit
                        override suspend fun setThemeMode(mode: com.vadymdev.habitix.domain.model.ThemeMode) = Unit
                        override suspend fun setAccentPalette(palette: com.vadymdev.habitix.domain.model.AccentPalette) = Unit
                        override suspend fun setLanguage(language: AppLanguage) = Unit
                        override suspend fun setPushEnabled(enabled: Boolean) = Unit
                        override suspend fun setReminderTime(hour: Int, minute: Int) = Unit
                        override suspend fun setSoundsEnabled(enabled: Boolean) = Unit
                        override suspend fun setVibrationEnabled(enabled: Boolean) = Unit
                        override suspend fun setBiometricEnabled(enabled: Boolean) = Unit
                        override suspend fun setAutoSyncEnabled(enabled: Boolean) = Unit
                    }
                ),
                syncAchievementsUseCase = com.vadymdev.habitix.domain.usecase.SyncAchievementsUseCase(
                    repository = object : com.vadymdev.habitix.domain.repository.AchievementSyncRepository {
                        override suspend fun sync(userId: String) = Unit
                        override suspend fun clearUserData(userId: String) = Unit
                    },
                    settingsRepository = object : com.vadymdev.habitix.domain.repository.SettingsRepository {
                        override fun observeSettings() = kotlinx.coroutines.flow.flowOf(com.vadymdev.habitix.domain.model.AppSettings())
                        override suspend fun getCurrentSettings() = com.vadymdev.habitix.domain.model.AppSettings()
                        override suspend fun replaceAll(settings: com.vadymdev.habitix.domain.model.AppSettings) = Unit
                        override suspend fun resetToDefaults() = Unit
                        override suspend fun setThemeMode(mode: com.vadymdev.habitix.domain.model.ThemeMode) = Unit
                        override suspend fun setAccentPalette(palette: com.vadymdev.habitix.domain.model.AccentPalette) = Unit
                        override suspend fun setLanguage(language: AppLanguage) = Unit
                        override suspend fun setPushEnabled(enabled: Boolean) = Unit
                        override suspend fun setReminderTime(hour: Int, minute: Int) = Unit
                        override suspend fun setSoundsEnabled(enabled: Boolean) = Unit
                        override suspend fun setVibrationEnabled(enabled: Boolean) = Unit
                        override suspend fun setBiometricEnabled(enabled: Boolean) = Unit
                        override suspend fun setAutoSyncEnabled(enabled: Boolean) = Unit
                    }
                )
            )
        )

        composeRule.setContent {
            AuthScreen(
                viewModel = viewModel,
                language = AppLanguage.EN,
                onAuthorized = {},
                onContinueAsGuest = { guestDoneCalls.incrementAndGet() },
                googleSignInRequest = { _, _ -> Result.failure(IllegalStateException("GetGoogleIdOperation failed with status CANCELED")) }
            )
        }

        composeRule.onNodeWithText("Sign in with Google").assertIsDisplayed().performClick()
        composeRule.onAllNodesWithText("Continue as guest")[0].assertIsDisplayed().performClick()

        assertEquals(1, guestDoneCalls.get())
    }
}

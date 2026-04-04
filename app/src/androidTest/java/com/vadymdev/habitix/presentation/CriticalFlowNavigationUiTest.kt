package com.vadymdev.habitix.presentation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.domain.model.AppSettings
import com.vadymdev.habitix.domain.model.Habit
import com.vadymdev.habitix.domain.model.HabitFrequencyType
import com.vadymdev.habitix.presentation.auth.AuthScreen
import com.vadymdev.habitix.presentation.auth.AuthViewModel
import com.vadymdev.habitix.presentation.dashboard.DashboardScreen
import com.vadymdev.habitix.presentation.dashboard.DashboardUiState
import com.vadymdev.habitix.presentation.settings.SettingsScreen
import com.vadymdev.habitix.presentation.settings.SettingsUiState
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class CriticalFlowNavigationUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun authToDashboardToSettings_criticalNavigationFlow() {
        composeRule.setContent {
            CriticalFlowHost()
        }

        composeRule.onNodeWithText("Continue as guest").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Habits for today").assertIsDisplayed()
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithText("Settings").assertIsDisplayed()
    }

    @Composable
    private fun CriticalFlowHost() {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "auth") {
            composable("auth") {
                val viewModel = AuthViewModel(
                    signInWithGoogleUseCase = com.vadymdev.habitix.domain.usecase.SignInWithGoogleUseCase(
                        authRepository = object : com.vadymdev.habitix.domain.repository.AuthRepository {
                            override fun observeSession() = kotlinx.coroutines.flow.flowOf<com.vadymdev.habitix.domain.model.UserSession?>(null)
                            override fun observeGuestMode() = kotlinx.coroutines.flow.flowOf(false)
                            override suspend fun signInWithGoogle(idToken: String) = Result.failure<com.vadymdev.habitix.domain.model.UserSession>(UnsupportedOperationException())
                            override suspend fun continueAsGuest() = Unit
                            override fun getCurrentSession() = null
                            override suspend fun signOut() = Unit
                            override suspend fun deleteAccount() = Result.success(Unit)
                        }
                    ),
                    continueAsGuestUseCase = com.vadymdev.habitix.domain.usecase.ContinueAsGuestUseCase(
                        authRepository = object : com.vadymdev.habitix.domain.repository.AuthRepository {
                            override fun observeSession() = kotlinx.coroutines.flow.flowOf<com.vadymdev.habitix.domain.model.UserSession?>(null)
                            override fun observeGuestMode() = kotlinx.coroutines.flow.flowOf(false)
                            override suspend fun signInWithGoogle(idToken: String) = Result.failure<com.vadymdev.habitix.domain.model.UserSession>(UnsupportedOperationException())
                            override suspend fun continueAsGuest() = Unit
                            override fun getCurrentSession() = null
                            override suspend fun signOut() = Unit
                            override suspend fun deleteAccount() = Result.success(Unit)
                        }
                    ),
                    syncOrchestratorUseCase = com.vadymdev.habitix.domain.usecase.SyncOrchestratorUseCase(
                        syncSettingsUseCase = com.vadymdev.habitix.domain.usecase.SyncSettingsUseCase(
                            repository = object : com.vadymdev.habitix.domain.repository.SettingsSyncRepository {
                                override suspend fun sync(userId: String) = Unit
                                override suspend fun clearUserData(userId: String) = Unit
                            },
                            settingsRepository = object : com.vadymdev.habitix.domain.repository.SettingsRepository {
                                override fun observeSettings() = kotlinx.coroutines.flow.flowOf(AppSettings(autoSyncEnabled = true))
                                override suspend fun getCurrentSettings() = AppSettings(autoSyncEnabled = true)
                                override suspend fun replaceAll(settings: AppSettings) = Unit
                                override suspend fun resetToDefaults() = Unit
                                override suspend fun setThemeMode(mode: com.vadymdev.habitix.domain.model.ThemeMode) = Unit
                                override suspend fun setAccentPalette(palette: com.vadymdev.habitix.domain.model.AccentPalette) = Unit
                                override suspend fun setLanguage(language: com.vadymdev.habitix.domain.model.AppLanguage) = Unit
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
                                override fun observeSettings() = kotlinx.coroutines.flow.flowOf(AppSettings(autoSyncEnabled = true))
                                override suspend fun getCurrentSettings() = AppSettings(autoSyncEnabled = true)
                                override suspend fun replaceAll(settings: AppSettings) = Unit
                                override suspend fun resetToDefaults() = Unit
                                override suspend fun setThemeMode(mode: com.vadymdev.habitix.domain.model.ThemeMode) = Unit
                                override suspend fun setAccentPalette(palette: com.vadymdev.habitix.domain.model.AccentPalette) = Unit
                                override suspend fun setLanguage(language: com.vadymdev.habitix.domain.model.AppLanguage) = Unit
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
                                override fun observeSettings() = kotlinx.coroutines.flow.flowOf(AppSettings(autoSyncEnabled = true))
                                override suspend fun getCurrentSettings() = AppSettings(autoSyncEnabled = true)
                                override suspend fun replaceAll(settings: AppSettings) = Unit
                                override suspend fun resetToDefaults() = Unit
                                override suspend fun setThemeMode(mode: com.vadymdev.habitix.domain.model.ThemeMode) = Unit
                                override suspend fun setAccentPalette(palette: com.vadymdev.habitix.domain.model.AccentPalette) = Unit
                                override suspend fun setLanguage(language: com.vadymdev.habitix.domain.model.AppLanguage) = Unit
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
                                override fun observeSettings() = kotlinx.coroutines.flow.flowOf(AppSettings(autoSyncEnabled = true))
                                override suspend fun getCurrentSettings() = AppSettings(autoSyncEnabled = true)
                                override suspend fun replaceAll(settings: AppSettings) = Unit
                                override suspend fun resetToDefaults() = Unit
                                override suspend fun setThemeMode(mode: com.vadymdev.habitix.domain.model.ThemeMode) = Unit
                                override suspend fun setAccentPalette(palette: com.vadymdev.habitix.domain.model.AccentPalette) = Unit
                                override suspend fun setLanguage(language: com.vadymdev.habitix.domain.model.AppLanguage) = Unit
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

                AuthScreen(
                    viewModel = viewModel,
                    language = AppLanguage.EN,
                    onAuthorized = { navController.navigate("dashboard") },
                    onContinueAsGuest = { navController.navigate("dashboard") }
                )
            }

            composable("dashboard") {
                DashboardScreen(
                    state = DashboardUiState(
                        selectedDate = LocalDate.of(2026, 4, 4),
                        habits = listOf(
                            Habit(
                                id = 1,
                                title = "Read",
                                iconKey = "reading",
                                colorKey = "mint",
                                frequencyType = HabitFrequencyType.DAILY,
                                customDays = emptySet(),
                                reminderEnabled = false,
                                isCompletedForSelectedDate = false,
                                streakDays = 1
                            )
                        ),
                        completedCount = 0,
                        totalCount = 1
                    ),
                    language = AppLanguage.EN,
                    onDateSelected = {},
                    onToggleHabit = {},
                    vibrationEnabled = false,
                    onConsumeAchievementEvent = {},
                    onDeleteHabit = {},
                    onEditHabit = {},
                    onCreateHabit = {},
                    onOpenSettings = { navController.navigate("settings") },
                    onOpenStats = {},
                    onOpenProfile = {}
                )
            }

            composable("settings") {
                SettingsScreen(
                    state = SettingsUiState(settings = AppSettings(language = AppLanguage.EN)),
                    onOpenDashboard = { navController.navigate("dashboard") },
                    onOpenStats = {},
                    onOpenProfile = {},
                    onAccent = {},
                    onLanguage = {},
                    onPushToggle = {},
                    onTimePicked = { _, _ -> },
                    onSoundsToggle = {},
                    onVibrationToggle = {},
                    onAutoSyncToggle = {},
                    onOpenPrivacyPolicy = {},
                    onSignOut = {},
                    onDeleteData = {},
                    onResetDeleteDataState = {}
                )
            }
        }
    }
}

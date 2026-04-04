package com.vadymdev.habitix.presentation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import com.vadymdev.habitix.presentation.dashboard.DashboardScreen
import com.vadymdev.habitix.presentation.dashboard.DashboardUiState
import com.vadymdev.habitix.presentation.settings.SettingsScreen
import com.vadymdev.habitix.presentation.settings.SettingsUiState
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class EndToEndSettingsDashboardFlowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun changeLanguageInSettings_andReturnToDashboard_persistsUiState() {
        composeRule.setContent {
            FlowHost()
        }

        composeRule.onNodeWithText("Habits for today").assertIsDisplayed()
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithText("Language").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Українська").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Головна").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Звички на сьогодні").assertIsDisplayed()
    }

    @Composable
    private fun FlowHost() {
        val navController = rememberNavController()
        var settings by mutableStateOf(AppSettings(language = AppLanguage.EN))
        val dashboardState = DashboardUiState(
            selectedDate = LocalDate.of(2026, 4, 4),
            habits = listOf(
                Habit(
                    id = 1L,
                    title = "Read",
                    iconKey = "reading",
                    colorKey = "mint",
                    frequencyType = HabitFrequencyType.DAILY,
                    customDays = emptySet(),
                    reminderEnabled = false,
                    isCompletedForSelectedDate = false,
                    streakDays = 2
                )
            ),
            completedCount = 0,
            totalCount = 1
        )

        NavHost(navController = navController, startDestination = "dashboard") {
            composable("dashboard") {
                DashboardScreen(
                    state = dashboardState,
                    language = settings.language,
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
                    state = SettingsUiState(settings = settings),
                    onOpenDashboard = { navController.navigate("dashboard") },
                    onOpenStats = {},
                    onOpenProfile = {},
                    onAccent = { settings = settings.copy(accentPalette = it) },
                    onLanguage = { settings = settings.copy(language = it) },
                    onPushToggle = { settings = settings.copy(pushEnabled = it) },
                    onTimePicked = { h, m -> settings = settings.copy(reminderHour = h, reminderMinute = m) },
                    onSoundsToggle = { settings = settings.copy(soundsEnabled = it) },
                    onVibrationToggle = { settings = settings.copy(vibrationEnabled = it) },
                    onAutoSyncToggle = { settings = settings.copy(autoSyncEnabled = it) },
                    onOpenPrivacyPolicy = {},
                    onSignOut = {},
                    onDeleteData = {},
                    onResetDeleteDataState = {}
                )
            }
        }
    }
}

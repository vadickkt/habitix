package com.vadymdev.habitix.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.domain.model.AppSettings
import com.vadymdev.habitix.domain.model.Habit
import com.vadymdev.habitix.domain.model.HabitFrequencyType
import com.vadymdev.habitix.presentation.auth.AuthLoadingContent
import com.vadymdev.habitix.presentation.dashboard.DashboardScreen
import com.vadymdev.habitix.presentation.dashboard.DashboardUiState
import com.vadymdev.habitix.presentation.settings.SettingsScreen
import com.vadymdev.habitix.presentation.settings.SettingsUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger

class AuthDashboardSettingsUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun authLoading_showsExpectedEnglishSteps() {
        composeRule.setContent {
            AuthLoadingContent(currentStepIndex = 1, isUk = false)
        }

        composeRule.onNodeWithText("Connecting to Google...").assertIsDisplayed()
        composeRule.onNodeWithText("Fetching profile data...").assertIsDisplayed()
        composeRule.onNodeWithText("Almost done!").assertIsDisplayed()
    }

    @Test
    fun dashboard_addHabitButton_invokesCallback() {
        val createClicks = AtomicInteger(0)
        val state = DashboardUiState(
            selectedDate = LocalDate.of(2026, 4, 4),
            habits = listOf(
                Habit(
                    id = 1L,
                    title = "Drink water",
                    iconKey = "water",
                    colorKey = "mint",
                    frequencyType = HabitFrequencyType.DAILY,
                    customDays = emptySet(),
                    reminderEnabled = false,
                    isCompletedForSelectedDate = false,
                    streakDays = 3
                )
            ),
            completedCount = 0,
            totalCount = 1
        )

        composeRule.setContent {
            DashboardScreen(
                state = state,
                language = AppLanguage.EN,
                onDateSelected = {},
                onToggleHabit = {},
                vibrationEnabled = false,
                onConsumeAchievementEvent = {},
                onDeleteHabit = {},
                onEditHabit = {},
                onCreateHabit = { createClicks.incrementAndGet() },
                onOpenSettings = {},
                onOpenStats = {},
                onOpenProfile = {}
            )
        }

        composeRule.onNodeWithText("Add a new habit", substring = true).assertIsDisplayed().performClick()

        assertEquals(1, createClicks.get())
    }

    @Test
    fun dashboard_withNoHabits_showsZeroOfZeroCounter() {
        val state = DashboardUiState(
            selectedDate = LocalDate.of(2026, 4, 4),
            habits = emptyList(),
            completedCount = 0,
            totalCount = 0
        )

        composeRule.setContent {
            DashboardScreen(
                state = state,
                language = AppLanguage.UK,
                onDateSelected = {},
                onToggleHabit = {},
                vibrationEnabled = false,
                onConsumeAchievementEvent = {},
                onDeleteHabit = {},
                onEditHabit = {},
                onCreateHabit = {},
                onOpenSettings = {},
                onOpenStats = {},
                onOpenProfile = {}
            )
        }

        composeRule.onNodeWithText("0/0").assertIsDisplayed()
    }

    @Test
    fun settings_screen_rendersCoreActions() {

        composeRule.setContent {
            SettingsScreen(
                state = SettingsUiState(settings = AppSettings(language = AppLanguage.EN)),
                onOpenDashboard = {},
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

        composeRule.onNodeWithText("Settings").assertIsDisplayed()
        composeRule.onNodeWithText("Language").assertIsDisplayed()
    }
}

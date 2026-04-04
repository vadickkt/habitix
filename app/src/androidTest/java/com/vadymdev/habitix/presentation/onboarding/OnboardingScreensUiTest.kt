package com.vadymdev.habitix.presentation.onboarding

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.domain.model.InterestCategory
import com.vadymdev.habitix.presentation.onboarding.screens.OnboardingInterestsScreen
import com.vadymdev.habitix.presentation.onboarding.screens.OnboardingIntroScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class OnboardingScreensUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun introScreen_startButtonVisibleAndClickable() {
        val clicks = AtomicInteger(0)

        composeRule.setContent {
            OnboardingIntroScreen(
                language = AppLanguage.EN,
                onContinue = { clicks.incrementAndGet() }
            )
        }

        composeRule.onNodeWithText("Start").assertIsDisplayed().assertIsEnabled().performClick()

        assertEquals(1, clicks.get())
    }

    @Test
    fun interestsScreen_nextDisabledWhenNothingSelected() {
        composeRule.setContent {
            OnboardingInterestsScreen(
                language = AppLanguage.EN,
                interests = sampleInterests(),
                selectedKeys = emptySet(),
                onToggle = {},
                onContinue = {}
            )
        }

        composeRule.onNodeWithText("Next").assertIsDisplayed().assertIsNotEnabled()
    }

    @Test
    fun interestsScreen_nextEnabledWhenSelectionExists() {
        composeRule.setContent {
            OnboardingInterestsScreen(
                language = AppLanguage.EN,
                interests = sampleInterests(),
                selectedKeys = setOf("health"),
                onToggle = {},
                onContinue = {}
            )
        }

        composeRule.onNodeWithText("Next").assertIsDisplayed().assertIsEnabled()
    }

    private fun sampleInterests(): List<InterestCategory> {
        return listOf(
            InterestCategory("health", "Здоров'я", "💚", 0xFF5DB075),
            InterestCategory("sport", "Спорт", "🏃", 0xFF4C9AFF)
        )
    }
}

package com.vadymdev.habitix.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.vadymdev.habitix.di.AppContainer
import com.vadymdev.habitix.presentation.auth.AuthViewModel
import com.vadymdev.habitix.presentation.auth.AuthViewModelFactory
import com.vadymdev.habitix.presentation.dashboard.DashboardViewModel
import com.vadymdev.habitix.presentation.dashboard.DashboardViewModelFactory
import com.vadymdev.habitix.presentation.habit.create.CreateHabitViewModel
import com.vadymdev.habitix.presentation.habit.create.CreateHabitViewModelFactory
import com.vadymdev.habitix.presentation.navigation.AppRoute
import com.vadymdev.habitix.presentation.onboarding.OnboardingViewModel
import com.vadymdev.habitix.presentation.onboarding.OnboardingViewModelFactory
import com.vadymdev.habitix.presentation.profile.ProfileViewModel
import com.vadymdev.habitix.presentation.profile.ProfileViewModelFactory
import com.vadymdev.habitix.presentation.settings.SettingsViewModel
import com.vadymdev.habitix.presentation.settings.SettingsViewModelFactory
import com.vadymdev.habitix.presentation.stats.StatsViewModel
import com.vadymdev.habitix.presentation.stats.StatsViewModelFactory
import com.vadymdev.habitix.ui.theme.HabitixTheme

@Composable
fun HabitixApp() {
    val context = LocalContext.current
    val container = remember(context) { AppContainer(context) }

    val onboardingViewModel: OnboardingViewModel = viewModel(
        factory = OnboardingViewModelFactory(
            observeOnboardingUseCase = container.observeOnboardingUseCase,
            updateInterestsUseCase = container.updateInterestsUseCase,
            updateHabitsUseCase = container.updateHabitsUseCase,
            completeOnboardingUseCase = container.completeOnboardingUseCase
        )
    )

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            signInWithGoogleUseCase = container.signInWithGoogleUseCase,
            continueAsGuestUseCase = container.continueAsGuestUseCase,
            syncOrchestratorUseCase = container.syncOrchestratorUseCase
        )
    )

    val appViewModel: AppViewModel = viewModel(
        factory = AppViewModelFactory(
            observeOnboardingUseCase = container.observeOnboardingUseCase,
            observeAuthSessionUseCase = container.observeAuthSessionUseCase,
            observeGuestModeUseCase = container.observeGuestModeUseCase
        )
    )

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(
            observeSettingsUseCase = container.observeSettingsUseCase,
            observeAuthSessionUseCase = container.observeAuthSessionUseCase,
            setThemeModeUseCase = container.setThemeModeUseCase,
            setAccentPaletteUseCase = container.setAccentPaletteUseCase,
            setLanguageUseCase = container.setLanguageUseCase,
            setPushEnabledUseCase = container.setPushEnabledUseCase,
            setReminderTimeUseCase = container.setReminderTimeUseCase,
            setSoundsEnabledUseCase = container.setSoundsEnabledUseCase,
            setVibrationEnabledUseCase = container.setVibrationEnabledUseCase,
            setBiometricEnabledUseCase = container.setBiometricEnabledUseCase,
            setAutoSyncEnabledUseCase = container.setAutoSyncEnabledUseCase,
            syncOrchestratorUseCase = container.syncOrchestratorUseCase,
            signOutUseCase = container.signOutUseCase,
            deleteAccountUseCase = container.deleteAccountUseCase,
            deleteDataUseCase = container.deleteDataUseCase
        )
    )

    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(
            observeHabitsForDateUseCase = container.observeHabitsForDateUseCase,
            observeProfileAnalyticsUseCase = container.observeProfileAnalyticsUseCase,
            toggleHabitCompletionUseCase = container.toggleHabitCompletionUseCase,
            deactivateHabitFromDateUseCase = container.deactivateHabitFromDateUseCase,
            observeAuthSessionUseCase = container.observeAuthSessionUseCase,
            syncOrchestratorUseCase = container.syncOrchestratorUseCase
        )
    )

    val createHabitViewModel: CreateHabitViewModel = viewModel(
        factory = CreateHabitViewModelFactory(
            createHabitUseCase = container.createHabitUseCase,
            updateHabitUseCase = container.updateHabitUseCase,
            validateHabitTitleUseCase = container.validateHabitTitleUseCase,
            observeAuthSessionUseCase = container.observeAuthSessionUseCase,
            syncUserHabitsUseCase = container.syncUserHabitsUseCase
        )
    )

    val statsViewModel: StatsViewModel = viewModel(
        factory = StatsViewModelFactory(
            observeStatsUseCase = container.observeStatsUseCase
        )
    )

    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(
            observeProfileIdentityUseCase = container.observeProfileIdentityUseCase,
            observeProfileAnalyticsUseCase = container.observeProfileAnalyticsUseCase,
            observeAuthSessionUseCase = container.observeAuthSessionUseCase,
            syncOrchestratorUseCase = container.syncOrchestratorUseCase,
            updateProfileNameUseCase = container.updateProfileNameUseCase,
            updateProfileBioUseCase = container.updateProfileBioUseCase,
            updateProfileAvatarUseCase = container.updateProfileAvatarUseCase
        )
    )

    val navController = rememberNavController()
    val onboardingState by onboardingViewModel.state.collectAsStateWithLifecycle()
    val dashboardState by dashboardViewModel.state.collectAsStateWithLifecycle()
    val createHabitState by createHabitViewModel.state.collectAsStateWithLifecycle()
    val settingsState by settingsViewModel.state.collectAsStateWithLifecycle()
    val statsState by statsViewModel.state.collectAsStateWithLifecycle()
    val profileState by profileViewModel.state.collectAsStateWithLifecycle()
    val startDestination by appViewModel.startDestination.collectAsStateWithLifecycle()

    HabitixTheme(
        themeMode = settingsState.settings.themeMode,
        accentPalette = settingsState.settings.accentPalette
    ) {
        if (startDestination == AppRoute.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(com.vadymdev.habitix.ui.theme.AppBackground),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = com.vadymdev.habitix.ui.theme.BrandGreen)
            }
            return@HabitixTheme
        }

        HabitixNavGraph(
            navController = navController,
            startDestination = startDestination,
            settingsState = settingsState,
            onboardingState = onboardingState,
            dashboardState = dashboardState,
            createHabitState = createHabitState,
            statsState = statsState,
            profileState = profileState,
            onboardingViewModel = onboardingViewModel,
            authViewModel = authViewModel,
            settingsViewModel = settingsViewModel,
            dashboardViewModel = dashboardViewModel,
            createHabitViewModel = createHabitViewModel,
            statsViewModel = statsViewModel,
            profileViewModel = profileViewModel
        )
    }
}

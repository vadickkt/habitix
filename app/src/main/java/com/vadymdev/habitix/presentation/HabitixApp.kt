package com.vadymdev.habitix.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.vadymdev.habitix.presentation.auth.AuthViewModel
import com.vadymdev.habitix.presentation.dashboard.DashboardViewModel
import com.vadymdev.habitix.presentation.habit.create.CreateHabitViewModel
import com.vadymdev.habitix.presentation.navigation.AppRoute
import com.vadymdev.habitix.presentation.onboarding.OnboardingViewModel
import com.vadymdev.habitix.presentation.profile.ProfileViewModel
import com.vadymdev.habitix.presentation.settings.SettingsViewModel
import com.vadymdev.habitix.presentation.stats.StatsViewModel
import com.vadymdev.habitix.ui.theme.HabitixTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun HabitixApp() {
    val onboardingViewModel: OnboardingViewModel = koinViewModel()
    val authViewModel: AuthViewModel = koinViewModel()
    val appViewModel: AppViewModel = koinViewModel()
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val dashboardViewModel: DashboardViewModel = koinViewModel()
    val createHabitViewModel: CreateHabitViewModel = koinViewModel()
    val statsViewModel: StatsViewModel = koinViewModel()
    val profileViewModel: ProfileViewModel = koinViewModel()

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

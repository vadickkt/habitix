package com.vadymdev.habitix.presentation

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vadymdev.habitix.di.AppContainer
import com.vadymdev.habitix.presentation.auth.AuthScreen
import com.vadymdev.habitix.presentation.auth.AuthViewModel
import com.vadymdev.habitix.presentation.auth.AuthViewModelFactory
import com.vadymdev.habitix.presentation.dashboard.DashboardPlaceholderScreen
import com.vadymdev.habitix.presentation.navigation.AppRoute
import com.vadymdev.habitix.presentation.onboarding.OnboardingViewModel
import com.vadymdev.habitix.presentation.onboarding.OnboardingViewModelFactory
import com.vadymdev.habitix.presentation.onboarding.screens.OnboardingHabitsScreen
import com.vadymdev.habitix.presentation.onboarding.screens.OnboardingInterestsScreen
import com.vadymdev.habitix.presentation.onboarding.screens.OnboardingIntroScreen

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
            continueAsGuestUseCase = container.continueAsGuestUseCase
        )
    )

    val navController = rememberNavController()
    val onboardingState by onboardingViewModel.state.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = AppRoute.OnboardingIntro
    ) {
        composable(AppRoute.OnboardingIntro) {
            OnboardingIntroScreen(
                onContinue = { navController.navigate(AppRoute.OnboardingInterests) }
            )
        }

        composable(AppRoute.OnboardingInterests) {
            OnboardingInterestsScreen(
                interests = onboardingState.interests,
                selectedKeys = onboardingState.selectedInterestKeys,
                onToggle = onboardingViewModel::toggleInterest,
                onContinue = { navController.navigate(AppRoute.OnboardingHabits) }
            )
        }

        composable(AppRoute.OnboardingHabits) {
            OnboardingHabitsScreen(
                habits = onboardingState.habits,
                selected = onboardingState.selectedHabitKeys,
                onToggle = onboardingViewModel::toggleHabit,
                onComplete = {
                    onboardingViewModel.completeOnboarding {
                        navController.navigate(AppRoute.Auth) {
                            popUpTo(AppRoute.OnboardingIntro) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(AppRoute.Auth) {
            AuthScreen(
                viewModel = authViewModel,
                onAuthorized = {
                    navController.navigate(AppRoute.Dashboard) {
                        popUpTo(AppRoute.Auth) { inclusive = true }
                    }
                },
                onContinueAsGuest = {
                    navController.navigate(AppRoute.Dashboard) {
                        popUpTo(AppRoute.Auth) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoute.Dashboard) {
            DashboardPlaceholderScreen()
        }
    }
}

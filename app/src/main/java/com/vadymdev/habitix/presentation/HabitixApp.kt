package com.vadymdev.habitix.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.CircularProgressIndicator
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vadymdev.habitix.di.AppContainer
import com.vadymdev.habitix.presentation.auth.AuthScreen
import com.vadymdev.habitix.presentation.auth.AuthViewModel
import com.vadymdev.habitix.presentation.auth.AuthViewModelFactory
import com.vadymdev.habitix.presentation.dashboard.DashboardScreen
import com.vadymdev.habitix.presentation.dashboard.DashboardViewModel
import com.vadymdev.habitix.presentation.dashboard.DashboardViewModelFactory
import com.vadymdev.habitix.presentation.habit.create.CreateHabitScreen
import com.vadymdev.habitix.presentation.habit.create.CreateHabitViewModel
import com.vadymdev.habitix.presentation.habit.create.CreateHabitViewModelFactory
import com.vadymdev.habitix.presentation.habit.edit.EditHabitScreen
import com.vadymdev.habitix.presentation.navigation.AppRoute
import com.vadymdev.habitix.presentation.onboarding.OnboardingViewModel
import com.vadymdev.habitix.presentation.onboarding.OnboardingViewModelFactory
import com.vadymdev.habitix.presentation.onboarding.screens.OnboardingHabitsScreen
import com.vadymdev.habitix.presentation.onboarding.screens.OnboardingInterestsScreen
import com.vadymdev.habitix.presentation.onboarding.screens.OnboardingIntroScreen
import com.vadymdev.habitix.presentation.profile.AchievementsScreen
import com.vadymdev.habitix.presentation.profile.ProfileScreen
import com.vadymdev.habitix.presentation.profile.ProfileViewModel
import com.vadymdev.habitix.presentation.profile.ProfileViewModelFactory
import com.vadymdev.habitix.presentation.settings.SettingsScreen
import com.vadymdev.habitix.presentation.settings.SettingsViewModel
import com.vadymdev.habitix.presentation.settings.SettingsViewModelFactory
import com.vadymdev.habitix.presentation.settings.PrivacyPolicyScreen
import com.vadymdev.habitix.presentation.stats.StatsScreen
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
            syncUserHabitsUseCase = container.syncUserHabitsUseCase,
            syncSettingsUseCase = container.syncSettingsUseCase
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
            syncSettingsUseCase = container.syncSettingsUseCase,
            signOutUseCase = container.signOutUseCase,
            deleteAccountUseCase = container.deleteAccountUseCase
        )
    )

    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(
            observeHabitsForDateUseCase = container.observeHabitsForDateUseCase,
            observeProfileAnalyticsUseCase = container.observeProfileAnalyticsUseCase,
            toggleHabitCompletionUseCase = container.toggleHabitCompletionUseCase,
            deactivateHabitFromDateUseCase = container.deactivateHabitFromDateUseCase,
            observeAuthSessionUseCase = container.observeAuthSessionUseCase,
            syncUserHabitsUseCase = container.syncUserHabitsUseCase
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
            syncProfileUseCase = container.syncProfileUseCase,
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
        themeMode = com.vadymdev.habitix.domain.model.ThemeMode.LIGHT,
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

        NavHost(
            navController = navController,
            startDestination = startDestination
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
                DashboardScreen(
                    state = dashboardState,
                    onDateSelected = dashboardViewModel::onDateSelected,
                    onToggleHabit = dashboardViewModel::onToggleHabit,
                    vibrationEnabled = settingsState.settings.vibrationEnabled,
                    onConsumeAchievementEvent = dashboardViewModel::consumeAchievementEvent,
                    onDeleteHabit = dashboardViewModel::deleteHabitFromToday,
                    onEditHabit = { habit ->
                        createHabitViewModel.startEditing(habit)
                        navController.navigate(AppRoute.EditHabit)
                    },
                    onCreateHabit = { navController.navigate(AppRoute.CreateHabit) },
                    onOpenSettings = { navController.navigate(AppRoute.Settings) },
                    onOpenStats = { navController.navigate(AppRoute.Stats) },
                    onOpenProfile = { navController.navigate(AppRoute.Profile) }
                )
            }

            composable(AppRoute.Stats) {
                StatsScreen(
                    state = statsState,
                    onSelectPeriod = statsViewModel::setPeriod,
                    onMetricClick = statsViewModel::openMetric,
                    onMetricDismiss = statsViewModel::closeMetric,
                    onCategoryClick = statsViewModel::openCategory,
                    onCategoryDismiss = statsViewModel::closeCategory,
                    onBadgeClick = statsViewModel::openBadge,
                    onBadgeDismiss = statsViewModel::closeBadge,
                    onHeatmapDayClick = statsViewModel::openHeatmapDay,
                    onHeatmapDayDismiss = statsViewModel::closeHeatmapDay,
                    onOpenDashboard = { navController.navigate(AppRoute.Dashboard) },
                    onOpenProfile = { navController.navigate(AppRoute.Profile) },
                    onOpenSettings = { navController.navigate(AppRoute.Settings) }
                )
            }

            composable(AppRoute.Profile) {
                ProfileScreen(
                    state = profileState,
                    onUpdateName = profileViewModel::updateName,
                    onUpdateBio = profileViewModel::updateBio,
                    onUpdateAvatar = profileViewModel::updateAvatar,
                    onOpenAllAchievements = { navController.navigate(AppRoute.Achievements) },
                    onOpenDashboard = { navController.navigate(AppRoute.Dashboard) },
                    onOpenStats = { navController.navigate(AppRoute.Stats) },
                    onOpenSettings = { navController.navigate(AppRoute.Settings) }
                )
            }

            composable(AppRoute.Achievements) {
                AchievementsScreen(
                    state = profileState,
                    onBack = { navController.popBackStack() },
                    onSelectCategory = profileViewModel::setAchievementCategory
                )
            }

            composable(AppRoute.Settings) {
                SettingsScreen(
                    state = settingsState,
                    onOpenDashboard = { navController.navigate(AppRoute.Dashboard) },
                    onOpenStats = { navController.navigate(AppRoute.Stats) },
                    onOpenProfile = { navController.navigate(AppRoute.Profile) },
                    onAccent = settingsViewModel::setAccentPalette,
                    onLanguage = settingsViewModel::setLanguage,
                    onPushToggle = settingsViewModel::setPushEnabled,
                    onTimePicked = settingsViewModel::setReminderTime,
                    onSoundsToggle = settingsViewModel::setSoundsEnabled,
                    onVibrationToggle = settingsViewModel::setVibrationEnabled,
                    onAutoSyncToggle = settingsViewModel::setAutoSyncEnabled,
                    onOpenPrivacyPolicy = { navController.navigate(AppRoute.PrivacyPolicy) },
                    onSignOut = {
                        settingsViewModel.signOut {
                            navController.navigate(AppRoute.Auth) {
                                popUpTo(AppRoute.Dashboard) { inclusive = true }
                            }
                        }
                    },
                    onDeleteAccount = {
                        settingsViewModel.deleteAccount(
                            onDone = {
                                navController.navigate(AppRoute.Auth) {
                                    popUpTo(AppRoute.Dashboard) { inclusive = true }
                                }
                            },
                            onError = {}
                        )
                    }
                )
            }

            composable(AppRoute.PrivacyPolicy) {
                PrivacyPolicyScreen(onBack = { navController.popBackStack() })
            }

            composable(AppRoute.CreateHabit) {
                CreateHabitScreen(
                    state = createHabitState,
                    onBack = {
                        createHabitViewModel.resetDraft()
                        navController.popBackStack()
                    },
                    onTitle = createHabitViewModel::setTitle,
                    onIcon = createHabitViewModel::setIcon,
                    onColor = createHabitViewModel::setColor,
                    onFrequency = createHabitViewModel::setFrequency,
                    onToggleDay = createHabitViewModel::toggleCustomDay,
                    onToggleReminder = createHabitViewModel::toggleReminder,
                    onSave = {
                        createHabitViewModel.saveHabit {
                            navController.popBackStack()
                        }
                    }
                )
            }

            composable(AppRoute.EditHabit) {
                EditHabitScreen(
                    state = createHabitState,
                    onBack = {
                        createHabitViewModel.resetDraft()
                        navController.popBackStack()
                    },
                    onTitle = createHabitViewModel::setTitle,
                    onIcon = createHabitViewModel::setIcon,
                    onColor = createHabitViewModel::setColor,
                    onFrequency = createHabitViewModel::setFrequency,
                    onToggleDay = createHabitViewModel::toggleCustomDay,
                    onToggleReminder = createHabitViewModel::toggleReminder,
                    onSave = {
                        createHabitViewModel.saveHabit {
                            navController.popBackStack()
                        }
                    }
                )
            }
        }
    }
}

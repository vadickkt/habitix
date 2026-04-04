package com.vadymdev.habitix.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.vadymdev.habitix.data.local.SettingsPreferencesDataSource
import com.vadymdev.habitix.presentation.auth.AuthScreen
import com.vadymdev.habitix.presentation.auth.AuthViewModel
import com.vadymdev.habitix.presentation.dashboard.DashboardScreen
import com.vadymdev.habitix.presentation.dashboard.DashboardUiState
import com.vadymdev.habitix.presentation.dashboard.DashboardViewModel
import com.vadymdev.habitix.presentation.habit.create.CreateHabitScreen
import com.vadymdev.habitix.presentation.habit.create.CreateHabitUiState
import com.vadymdev.habitix.presentation.habit.create.CreateHabitViewModel
import com.vadymdev.habitix.presentation.habit.edit.EditHabitScreen
import com.vadymdev.habitix.presentation.navigation.AppRoute
import com.vadymdev.habitix.presentation.onboarding.OnboardingUiState
import com.vadymdev.habitix.presentation.onboarding.OnboardingViewModel
import com.vadymdev.habitix.presentation.onboarding.screens.OnboardingHabitsScreen
import com.vadymdev.habitix.presentation.onboarding.screens.OnboardingInterestsScreen
import com.vadymdev.habitix.presentation.onboarding.screens.OnboardingIntroScreen
import com.vadymdev.habitix.presentation.profile.AchievementsScreen
import com.vadymdev.habitix.presentation.profile.ProfileScreen
import com.vadymdev.habitix.presentation.profile.ProfileUiState
import com.vadymdev.habitix.presentation.profile.ProfileViewModel
import com.vadymdev.habitix.presentation.settings.PrivacyPolicyScreen
import com.vadymdev.habitix.presentation.settings.SettingsScreen
import com.vadymdev.habitix.presentation.settings.SettingsUiState
import com.vadymdev.habitix.presentation.settings.SettingsViewModel
import com.vadymdev.habitix.presentation.stats.StatsScreen
import com.vadymdev.habitix.presentation.stats.StatsUiState
import com.vadymdev.habitix.presentation.stats.StatsViewModel

@Composable
internal fun HabitixNavGraph(
    navController: NavHostController,
    startDestination: String,
    settingsState: SettingsUiState,
    onboardingState: OnboardingUiState,
    dashboardState: DashboardUiState,
    createHabitState: CreateHabitUiState,
    statsState: StatsUiState,
    profileState: ProfileUiState,
    onboardingViewModel: OnboardingViewModel,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    dashboardViewModel: DashboardViewModel,
    createHabitViewModel: CreateHabitViewModel,
    statsViewModel: StatsViewModel,
    profileViewModel: ProfileViewModel
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppRoute.OnboardingIntro) {
            OnboardingIntroScreen(
                language = settingsState.settings.language,
                onContinue = { navController.navigate(AppRoute.OnboardingInterests) }
            )
        }

        composable(AppRoute.OnboardingInterests) {
            OnboardingInterestsScreen(
                language = settingsState.settings.language,
                interests = onboardingState.interests,
                selectedKeys = onboardingState.selectedInterestKeys,
                onToggle = onboardingViewModel::toggleInterest,
                onContinue = { navController.navigate(AppRoute.OnboardingHabits) }
            )
        }

        composable(AppRoute.OnboardingHabits) {
            OnboardingHabitsScreen(
                language = settingsState.settings.language,
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
                language = settingsState.settings.language,
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
            val dashboardContext = LocalContext.current
            val permissionSettingsDataSource = remember(dashboardContext) {
                SettingsPreferencesDataSource(dashboardContext)
            }
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { }

            LaunchedEffect(settingsState.settings.pushEnabled) {
                if (!settingsState.settings.pushEnabled) return@LaunchedEffect
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return@LaunchedEffect

                val granted = ContextCompat.checkSelfPermission(
                    dashboardContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

                val askedOnce = permissionSettingsDataSource.hasAskedNotificationPermission()
                if (!granted && !askedOnce) {
                    permissionSettingsDataSource.markNotificationPermissionAsked()
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            DashboardScreen(
                state = dashboardState,
                language = settingsState.settings.language,
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
                language = settingsState.settings.language,
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
                language = settingsState.settings.language,
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
                language = settingsState.settings.language,
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
                onDeleteData = settingsViewModel::deleteData,
                onResetDeleteDataState = settingsViewModel::dismissDeleteDataState
            )
        }

        composable(AppRoute.PrivacyPolicy) {
            PrivacyPolicyScreen(
                language = settingsState.settings.language,
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoute.CreateHabit) {
            CreateHabitScreen(
                state = createHabitState,
                language = settingsState.settings.language,
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
                vibrationEnabled = settingsState.settings.vibrationEnabled,
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
                language = settingsState.settings.language,
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

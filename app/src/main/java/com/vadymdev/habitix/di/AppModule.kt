package com.vadymdev.habitix.di

import androidx.work.WorkerFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vadymdev.habitix.data.local.AuthPreferencesDataSource
import com.vadymdev.habitix.data.local.OnboardingPreferencesDataSource
import com.vadymdev.habitix.data.local.ProfilePreferencesDataSource
import com.vadymdev.habitix.data.local.SettingsPreferencesDataSource
import com.vadymdev.habitix.data.local.room.HabitixDatabase
import com.vadymdev.habitix.data.repository.FirebaseAuthRepository
import com.vadymdev.habitix.data.repository.FirestoreAchievementSyncRepository
import com.vadymdev.habitix.data.repository.FirestoreHabitSyncRepository
import com.vadymdev.habitix.data.repository.FirestoreProfileSyncRepository
import com.vadymdev.habitix.data.repository.FirestoreSettingsSyncRepository
import com.vadymdev.habitix.data.repository.HabitRepositoryImpl
import com.vadymdev.habitix.data.repository.OnboardingRepositoryImpl
import com.vadymdev.habitix.data.repository.ProfileRepositoryImpl
import com.vadymdev.habitix.data.repository.SettingsRepositoryImpl
import com.vadymdev.habitix.data.sync.NetworkConnectivityChecker
import com.vadymdev.habitix.domain.repository.AchievementSyncRepository
import com.vadymdev.habitix.domain.repository.AuthRepository
import com.vadymdev.habitix.domain.repository.HabitRepository
import com.vadymdev.habitix.domain.repository.HabitSyncRepository
import com.vadymdev.habitix.domain.repository.OnboardingRepository
import com.vadymdev.habitix.domain.repository.ProfileRepository
import com.vadymdev.habitix.domain.repository.ProfileSyncRepository
import com.vadymdev.habitix.domain.repository.SettingsRepository
import com.vadymdev.habitix.domain.repository.SettingsSyncRepository
import com.vadymdev.habitix.domain.usecase.CompleteOnboardingUseCase
import com.vadymdev.habitix.domain.usecase.ContinueAsGuestUseCase
import com.vadymdev.habitix.domain.usecase.CreateHabitUseCase
import com.vadymdev.habitix.domain.usecase.DeactivateHabitFromDateUseCase
import com.vadymdev.habitix.domain.usecase.DeleteAccountUseCase
import com.vadymdev.habitix.domain.usecase.DeleteAllHabitsUseCase
import com.vadymdev.habitix.domain.usecase.DeleteDataUseCase
import com.vadymdev.habitix.domain.usecase.GetCurrentSettingsUseCase
import com.vadymdev.habitix.domain.usecase.GetIncompleteHabitsForDateUseCase
import com.vadymdev.habitix.domain.usecase.ObserveAuthSessionUseCase
import com.vadymdev.habitix.domain.usecase.ObserveGuestModeUseCase
import com.vadymdev.habitix.domain.usecase.ObserveHabitsForDateUseCase
import com.vadymdev.habitix.domain.usecase.ObserveOnboardingUseCase
import com.vadymdev.habitix.domain.usecase.ObserveProfileAnalyticsUseCase
import com.vadymdev.habitix.domain.usecase.ObserveProfileIdentityUseCase
import com.vadymdev.habitix.domain.usecase.ObserveSettingsUseCase
import com.vadymdev.habitix.domain.usecase.ObserveStatsUseCase
import com.vadymdev.habitix.domain.usecase.SetAccentPaletteUseCase
import com.vadymdev.habitix.domain.usecase.SetAutoSyncEnabledUseCase
import com.vadymdev.habitix.domain.usecase.SetBiometricEnabledUseCase
import com.vadymdev.habitix.domain.usecase.SetLanguageUseCase
import com.vadymdev.habitix.domain.usecase.SetPushEnabledUseCase
import com.vadymdev.habitix.domain.usecase.SetReminderTimeUseCase
import com.vadymdev.habitix.domain.usecase.SetSoundsEnabledUseCase
import com.vadymdev.habitix.domain.usecase.SetThemeModeUseCase
import com.vadymdev.habitix.domain.usecase.SetVibrationEnabledUseCase
import com.vadymdev.habitix.domain.usecase.SignInWithGoogleUseCase
import com.vadymdev.habitix.domain.usecase.SignOutUseCase
import com.vadymdev.habitix.domain.usecase.SyncAchievementsUseCase
import com.vadymdev.habitix.domain.usecase.SyncOrchestratorUseCase
import com.vadymdev.habitix.domain.usecase.SyncProfileUseCase
import com.vadymdev.habitix.domain.usecase.SyncSettingsUseCase
import com.vadymdev.habitix.domain.usecase.SyncUserHabitsUseCase
import com.vadymdev.habitix.domain.usecase.ToggleHabitCompletionUseCase
import com.vadymdev.habitix.domain.usecase.UpdateHabitUseCase
import com.vadymdev.habitix.domain.usecase.UpdateHabitsUseCase
import com.vadymdev.habitix.domain.usecase.UpdateInterestsUseCase
import com.vadymdev.habitix.domain.usecase.UpdateProfileAvatarUseCase
import com.vadymdev.habitix.domain.usecase.UpdateProfileBioUseCase
import com.vadymdev.habitix.domain.usecase.UpdateProfileNameUseCase
import com.vadymdev.habitix.domain.usecase.ValidateHabitTitleUseCase
import com.vadymdev.habitix.domain.usecase.ValidateProfileNameUseCase
import com.vadymdev.habitix.presentation.AppViewModel
import com.vadymdev.habitix.presentation.auth.AuthViewModel
import com.vadymdev.habitix.presentation.dashboard.DashboardViewModel
import com.vadymdev.habitix.presentation.habit.create.CreateHabitViewModel
import com.vadymdev.habitix.presentation.onboarding.OnboardingViewModel
import com.vadymdev.habitix.presentation.profile.ProfileViewModel
import com.vadymdev.habitix.presentation.settings.SettingsViewModel
import com.vadymdev.habitix.presentation.startup.StartupViewModel
import com.vadymdev.habitix.presentation.stats.StatsViewModel
import com.vadymdev.habitix.sync.CloudSyncScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { HabitixDatabase.get(androidContext()) }
    single { NetworkConnectivityChecker(androidContext()) }

    single { AuthPreferencesDataSource(androidContext()) }
    single { OnboardingPreferencesDataSource(androidContext()) }
    single { ProfilePreferencesDataSource(androidContext()) }
    single { SettingsPreferencesDataSource(androidContext()) }

    single {
        FirebaseAuthRepository(
            auth = get(),
            authLocal = get()
        )
    } bind AuthRepository::class

    single {
        HabitRepositoryImpl(
            habitDao = get<HabitixDatabase>().habitDao(),
            completionDao = get<HabitixDatabase>().habitCompletionDao(),
            hiddenDayDao = get<HabitixDatabase>().hiddenHabitDayDao(),
            achievementUnlockDao = get<HabitixDatabase>().achievementUnlockDao()
        )
    } bind HabitRepository::class

    single {
        OnboardingRepositoryImpl(
            local = get(),
            habitRepository = get()
        )
    } bind OnboardingRepository::class

    single { SettingsRepositoryImpl(get()) } bind SettingsRepository::class
    single { ProfileRepositoryImpl(get()) } bind ProfileRepository::class

    single {
        FirestoreHabitSyncRepository(
            firestore = get(),
            habitDao = get<HabitixDatabase>().habitDao(),
            completionDao = get<HabitixDatabase>().habitCompletionDao(),
            hiddenDayDao = get<HabitixDatabase>().hiddenHabitDayDao()
        )
    } bind HabitSyncRepository::class

    single { FirestoreProfileSyncRepository(get(), get()) } bind ProfileSyncRepository::class
    single { FirestoreSettingsSyncRepository(get(), get()) } bind SettingsSyncRepository::class
    single {
        FirestoreAchievementSyncRepository(
            firestore = get(),
            achievementUnlockDao = get<HabitixDatabase>().achievementUnlockDao()
        )
    } bind AchievementSyncRepository::class

    single { ObserveAuthSessionUseCase(get()) }
    single { ObserveGuestModeUseCase(get()) }
    single { SignInWithGoogleUseCase(get()) }
    single { ContinueAsGuestUseCase(get()) }
    single { SignOutUseCase(get()) }
    single { DeleteAccountUseCase(get()) }

    single { ObserveOnboardingUseCase(get()) }
    single { UpdateInterestsUseCase(get()) }
    single { UpdateHabitsUseCase(get()) }
    single { CompleteOnboardingUseCase(get()) }

    single { ObserveHabitsForDateUseCase(get()) }
    single { ObserveStatsUseCase(get()) }
    single { ToggleHabitCompletionUseCase(get()) }
    single { DeactivateHabitFromDateUseCase(get()) }
    single { CreateHabitUseCase(get()) }
    single { UpdateHabitUseCase(get()) }
    single { DeleteAllHabitsUseCase(get()) }
    single { GetIncompleteHabitsForDateUseCase(get()) }
    single { SyncUserHabitsUseCase(get(), get()) }

    single { ObserveSettingsUseCase(get()) }
    single { GetCurrentSettingsUseCase(get()) }
    single { ObserveProfileIdentityUseCase(get()) }
    single { ObserveProfileAnalyticsUseCase(get()) }

    single { SetThemeModeUseCase(get()) }
    single { SetAccentPaletteUseCase(get()) }
    single { SetLanguageUseCase(get()) }
    single { SetPushEnabledUseCase(get()) }
    single { SetReminderTimeUseCase(get()) }
    single { SetSoundsEnabledUseCase(get()) }
    single { SetVibrationEnabledUseCase(get()) }
    single { SetBiometricEnabledUseCase(get()) }
    single { SetAutoSyncEnabledUseCase(get()) }

    single { SyncSettingsUseCase(get(), get()) }
    single { SyncProfileUseCase(get(), get()) }
    single { SyncAchievementsUseCase(get(), get()) }

    single {
        SyncOrchestratorUseCase(
            syncSettingsUseCase = get(),
            syncProfileUseCase = get(),
            syncUserHabitsUseCase = get(),
            syncAchievementsUseCase = get(),
            isNetworkAvailable = { get<NetworkConnectivityChecker>().isOnline() },
            onDeferredSyncRequested = { CloudSyncScheduler.scheduleCatchUp(androidContext()) }
        )
    }

    single {
        HabitixWorkerFactory(
            firebaseAuth = get(),
            syncOrchestratorUseCase = get(),
            getCurrentSettingsUseCase = get(),
            getIncompleteHabitsForDateUseCase = get()
        )
    } bind WorkerFactory::class

    single {
        DeleteDataUseCase(
            deleteAllHabitsUseCase = get(),
            profileRepository = get(),
            settingsRepository = get(),
            habitSyncRepository = get(),
            profileSyncRepository = get(),
            settingsSyncRepository = get(),
            achievementSyncRepository = get()
        )
    }

    single { UpdateProfileNameUseCase(get()) }
    single { UpdateProfileBioUseCase(get()) }
    single { UpdateProfileAvatarUseCase(get()) }
    singleOf(::ValidateHabitTitleUseCase)
    singleOf(::ValidateProfileNameUseCase)

    viewModel { AppViewModel(get(), get(), get()) }
    viewModel { AuthViewModel(get(), get(), get()) }
    viewModel { DashboardViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { OnboardingViewModel(get(), get(), get(), get()) }
    viewModel { CreateHabitViewModel(get(), get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { StartupViewModel(get(), get()) }
    viewModel { StatsViewModel(get()) }
    viewModel { ProfileViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
}

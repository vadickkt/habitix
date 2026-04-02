package com.vadymdev.habitix.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vadymdev.habitix.data.local.AuthPreferencesDataSource
import com.vadymdev.habitix.data.local.OnboardingPreferencesDataSource
import com.vadymdev.habitix.data.local.SettingsPreferencesDataSource
import com.vadymdev.habitix.data.local.room.HabitixDatabase
import com.vadymdev.habitix.data.repository.FirebaseAuthRepository
import com.vadymdev.habitix.data.repository.FirestoreHabitSyncRepository
import com.vadymdev.habitix.data.repository.FirestoreSettingsSyncRepository
import com.vadymdev.habitix.data.repository.HabitRepositoryImpl
import com.vadymdev.habitix.data.repository.OnboardingRepositoryImpl
import com.vadymdev.habitix.data.repository.SettingsRepositoryImpl
import com.vadymdev.habitix.domain.repository.AuthRepository
import com.vadymdev.habitix.domain.repository.HabitRepository
import com.vadymdev.habitix.domain.repository.HabitSyncRepository
import com.vadymdev.habitix.domain.repository.OnboardingRepository
import com.vadymdev.habitix.domain.repository.SettingsRepository
import com.vadymdev.habitix.domain.repository.SettingsSyncRepository
import com.vadymdev.habitix.domain.usecase.CompleteOnboardingUseCase
import com.vadymdev.habitix.domain.usecase.ContinueAsGuestUseCase
import com.vadymdev.habitix.domain.usecase.CreateHabitUseCase
import com.vadymdev.habitix.domain.usecase.DeactivateHabitFromDateUseCase
import com.vadymdev.habitix.domain.usecase.DeleteAllHabitsUseCase
import com.vadymdev.habitix.domain.usecase.DeleteAccountUseCase
import com.vadymdev.habitix.domain.usecase.GetIncompleteHabitsForDateUseCase
import com.vadymdev.habitix.domain.usecase.HideHabitForDateUseCase
import com.vadymdev.habitix.domain.usecase.ObserveAuthSessionUseCase
import com.vadymdev.habitix.domain.usecase.ObserveGuestModeUseCase
import com.vadymdev.habitix.domain.usecase.ObserveHabitsForDateUseCase
import com.vadymdev.habitix.domain.usecase.ObserveOnboardingUseCase
import com.vadymdev.habitix.domain.usecase.ObserveStatsUseCase
import com.vadymdev.habitix.domain.usecase.ObserveSettingsUseCase
import com.vadymdev.habitix.domain.usecase.SetAccentPaletteUseCase
import com.vadymdev.habitix.domain.usecase.SetAutoSyncEnabledUseCase
import com.vadymdev.habitix.domain.usecase.SetBiometricEnabledUseCase
import com.vadymdev.habitix.domain.usecase.SetLanguageUseCase
import com.vadymdev.habitix.domain.usecase.SetPushEnabledUseCase
import com.vadymdev.habitix.domain.usecase.SetReminderTimeUseCase
import com.vadymdev.habitix.domain.usecase.SetSoundsEnabledUseCase
import com.vadymdev.habitix.domain.usecase.SetThemeModeUseCase
import com.vadymdev.habitix.domain.usecase.SetVibrationEnabledUseCase
import com.vadymdev.habitix.domain.usecase.SignOutUseCase
import com.vadymdev.habitix.domain.usecase.ToggleHabitCompletionUseCase
import com.vadymdev.habitix.domain.usecase.SignInWithGoogleUseCase
import com.vadymdev.habitix.domain.usecase.SyncUserHabitsUseCase
import com.vadymdev.habitix.domain.usecase.SyncSettingsUseCase
import com.vadymdev.habitix.domain.usecase.UpdateHabitUseCase
import com.vadymdev.habitix.domain.usecase.UpdateHabitsUseCase
import com.vadymdev.habitix.domain.usecase.UpdateInterestsUseCase

class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val database: HabitixDatabase by lazy { HabitixDatabase.get(appContext) }

    private val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository(
            auth = firebaseAuth,
            authLocal = AuthPreferencesDataSource(appContext)
        )
    }

    private val onboardingRepository: OnboardingRepository by lazy {
        OnboardingRepositoryImpl(
            local = OnboardingPreferencesDataSource(appContext),
            habitRepository = habitRepository
        )
    }

    private val habitRepository: HabitRepository by lazy {
        HabitRepositoryImpl(
            habitDao = database.habitDao(),
            completionDao = database.habitCompletionDao(),
            hiddenDayDao = database.hiddenHabitDayDao()
        )
    }

    private val habitSyncRepository: HabitSyncRepository by lazy {
        FirestoreHabitSyncRepository(
            firestore = firestore,
            habitDao = database.habitDao(),
            completionDao = database.habitCompletionDao(),
            hiddenDayDao = database.hiddenHabitDayDao()
        )
    }

    private val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(SettingsPreferencesDataSource(appContext))
    }

    private val settingsSyncRepository: SettingsSyncRepository by lazy {
        FirestoreSettingsSyncRepository(
            firestore = firestore,
            settingsRepository = settingsRepository
        )
    }

    val observeAuthSessionUseCase by lazy { ObserveAuthSessionUseCase(authRepository) }
    val observeGuestModeUseCase by lazy { ObserveGuestModeUseCase(authRepository) }
    val signInWithGoogleUseCase by lazy { SignInWithGoogleUseCase(authRepository) }
    val continueAsGuestUseCase by lazy { ContinueAsGuestUseCase(authRepository) }
    val signOutUseCase by lazy { SignOutUseCase(authRepository) }
    val deleteAccountUseCase by lazy { DeleteAccountUseCase(authRepository) }
    val observeOnboardingUseCase by lazy { ObserveOnboardingUseCase(onboardingRepository) }
    val updateInterestsUseCase by lazy { UpdateInterestsUseCase(onboardingRepository) }
    val updateHabitsUseCase by lazy { UpdateHabitsUseCase(onboardingRepository) }
    val completeOnboardingUseCase by lazy { CompleteOnboardingUseCase(onboardingRepository) }
    val observeHabitsForDateUseCase by lazy { ObserveHabitsForDateUseCase(habitRepository) }
    val observeStatsUseCase by lazy { ObserveStatsUseCase(habitRepository) }
    val toggleHabitCompletionUseCase by lazy { ToggleHabitCompletionUseCase(habitRepository) }
    val hideHabitForDateUseCase by lazy { HideHabitForDateUseCase(habitRepository) }
    val deactivateHabitFromDateUseCase by lazy { DeactivateHabitFromDateUseCase(habitRepository) }
    val deleteAllHabitsUseCase by lazy { DeleteAllHabitsUseCase(habitRepository) }
    val createHabitUseCase by lazy { CreateHabitUseCase(habitRepository) }
    val updateHabitUseCase by lazy { UpdateHabitUseCase(habitRepository) }
    val getIncompleteHabitsForDateUseCase by lazy { GetIncompleteHabitsForDateUseCase(habitRepository) }
    val syncUserHabitsUseCase by lazy { SyncUserHabitsUseCase(habitSyncRepository) }
    val observeSettingsUseCase by lazy { ObserveSettingsUseCase(settingsRepository) }
    val setThemeModeUseCase by lazy { SetThemeModeUseCase(settingsRepository) }
    val setAccentPaletteUseCase by lazy { SetAccentPaletteUseCase(settingsRepository) }
    val setLanguageUseCase by lazy { SetLanguageUseCase(settingsRepository) }
    val setPushEnabledUseCase by lazy { SetPushEnabledUseCase(settingsRepository) }
    val setReminderTimeUseCase by lazy { SetReminderTimeUseCase(settingsRepository) }
    val setSoundsEnabledUseCase by lazy { SetSoundsEnabledUseCase(settingsRepository) }
    val setVibrationEnabledUseCase by lazy { SetVibrationEnabledUseCase(settingsRepository) }
    val setBiometricEnabledUseCase by lazy { SetBiometricEnabledUseCase(settingsRepository) }
    val setAutoSyncEnabledUseCase by lazy { SetAutoSyncEnabledUseCase(settingsRepository) }
    val syncSettingsUseCase by lazy { SyncSettingsUseCase(settingsSyncRepository) }
}

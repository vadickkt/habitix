package com.vadymdev.habitix.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vadymdev.habitix.data.local.AuthPreferencesDataSource
import com.vadymdev.habitix.data.local.OnboardingPreferencesDataSource
import com.vadymdev.habitix.data.local.room.HabitixDatabase
import com.vadymdev.habitix.data.repository.FirebaseAuthRepository
import com.vadymdev.habitix.data.repository.FirestoreHabitSyncRepository
import com.vadymdev.habitix.data.repository.HabitRepositoryImpl
import com.vadymdev.habitix.data.repository.OnboardingRepositoryImpl
import com.vadymdev.habitix.domain.repository.AuthRepository
import com.vadymdev.habitix.domain.repository.HabitRepository
import com.vadymdev.habitix.domain.repository.HabitSyncRepository
import com.vadymdev.habitix.domain.repository.OnboardingRepository
import com.vadymdev.habitix.domain.usecase.CompleteOnboardingUseCase
import com.vadymdev.habitix.domain.usecase.ContinueAsGuestUseCase
import com.vadymdev.habitix.domain.usecase.CreateHabitUseCase
import com.vadymdev.habitix.domain.usecase.GetIncompleteHabitsForDateUseCase
import com.vadymdev.habitix.domain.usecase.ObserveAuthSessionUseCase
import com.vadymdev.habitix.domain.usecase.ObserveHabitsForDateUseCase
import com.vadymdev.habitix.domain.usecase.ObserveOnboardingUseCase
import com.vadymdev.habitix.domain.usecase.ToggleHabitCompletionUseCase
import com.vadymdev.habitix.domain.usecase.SignInWithGoogleUseCase
import com.vadymdev.habitix.domain.usecase.SyncUserHabitsUseCase
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
            completionDao = database.habitCompletionDao()
        )
    }

    private val habitSyncRepository: HabitSyncRepository by lazy {
        FirestoreHabitSyncRepository(
            firestore = firestore,
            habitDao = database.habitDao(),
            completionDao = database.habitCompletionDao()
        )
    }

    val observeAuthSessionUseCase by lazy { ObserveAuthSessionUseCase(authRepository) }
    val signInWithGoogleUseCase by lazy { SignInWithGoogleUseCase(authRepository) }
    val continueAsGuestUseCase by lazy { ContinueAsGuestUseCase(authRepository) }
    val observeOnboardingUseCase by lazy { ObserveOnboardingUseCase(onboardingRepository) }
    val updateInterestsUseCase by lazy { UpdateInterestsUseCase(onboardingRepository) }
    val updateHabitsUseCase by lazy { UpdateHabitsUseCase(onboardingRepository) }
    val completeOnboardingUseCase by lazy { CompleteOnboardingUseCase(onboardingRepository) }
    val observeHabitsForDateUseCase by lazy { ObserveHabitsForDateUseCase(habitRepository) }
    val toggleHabitCompletionUseCase by lazy { ToggleHabitCompletionUseCase(habitRepository) }
    val createHabitUseCase by lazy { CreateHabitUseCase(habitRepository) }
    val getIncompleteHabitsForDateUseCase by lazy { GetIncompleteHabitsForDateUseCase(habitRepository) }
    val syncUserHabitsUseCase by lazy { SyncUserHabitsUseCase(habitSyncRepository) }
}

package com.vadymdev.habitix.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.vadymdev.habitix.data.local.AuthPreferencesDataSource
import com.vadymdev.habitix.data.local.OnboardingPreferencesDataSource
import com.vadymdev.habitix.data.repository.FirebaseAuthRepository
import com.vadymdev.habitix.data.repository.OnboardingRepositoryImpl
import com.vadymdev.habitix.domain.repository.AuthRepository
import com.vadymdev.habitix.domain.repository.OnboardingRepository
import com.vadymdev.habitix.domain.usecase.CompleteOnboardingUseCase
import com.vadymdev.habitix.domain.usecase.ContinueAsGuestUseCase
import com.vadymdev.habitix.domain.usecase.ObserveAuthSessionUseCase
import com.vadymdev.habitix.domain.usecase.ObserveOnboardingUseCase
import com.vadymdev.habitix.domain.usecase.SignInWithGoogleUseCase
import com.vadymdev.habitix.domain.usecase.UpdateHabitsUseCase
import com.vadymdev.habitix.domain.usecase.UpdateInterestsUseCase

class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository(
            auth = firebaseAuth,
            authLocal = AuthPreferencesDataSource(appContext)
        )
    }

    private val onboardingRepository: OnboardingRepository by lazy {
        OnboardingRepositoryImpl(local = OnboardingPreferencesDataSource(appContext))
    }

    val observeAuthSessionUseCase by lazy { ObserveAuthSessionUseCase(authRepository) }
    val signInWithGoogleUseCase by lazy { SignInWithGoogleUseCase(authRepository) }
    val continueAsGuestUseCase by lazy { ContinueAsGuestUseCase(authRepository) }
    val observeOnboardingUseCase by lazy { ObserveOnboardingUseCase(onboardingRepository) }
    val updateInterestsUseCase by lazy { UpdateInterestsUseCase(onboardingRepository) }
    val updateHabitsUseCase by lazy { UpdateHabitsUseCase(onboardingRepository) }
    val completeOnboardingUseCase by lazy { CompleteOnboardingUseCase(onboardingRepository) }
}

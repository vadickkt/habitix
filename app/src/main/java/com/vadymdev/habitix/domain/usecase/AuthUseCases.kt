package com.vadymdev.habitix.domain.usecase

import com.vadymdev.habitix.domain.model.UserSession
import com.vadymdev.habitix.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class ObserveAuthSessionUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(): Flow<UserSession?> = authRepository.observeSession()
}

class ObserveGuestModeUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(): Flow<Boolean> = authRepository.observeGuestMode()
}

class SignInWithGoogleUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(idToken: String): Result<UserSession> = authRepository.signInWithGoogle(idToken)
}

class ContinueAsGuestUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke() = authRepository.continueAsGuest()
}

class SignOutUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke() = authRepository.signOut()
}

class DeleteAccountUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): Result<Unit> = authRepository.deleteAccount()
}
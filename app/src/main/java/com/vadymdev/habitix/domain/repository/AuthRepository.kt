package com.vadymdev.habitix.domain.repository

import com.vadymdev.habitix.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeSession(): Flow<UserSession?>
    suspend fun signInWithGoogle(idToken: String): Result<UserSession>
    suspend fun continueAsGuest()
    fun getCurrentSession(): UserSession?
    suspend fun signOut()
    suspend fun deleteAccount(): Result<Unit>
}

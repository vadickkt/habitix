package com.vadymdev.habitix.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.vadymdev.habitix.data.local.AuthPreferencesDataSource
import com.vadymdev.habitix.domain.model.UserSession
import com.vadymdev.habitix.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
    private val authLocal: AuthPreferencesDataSource
) : AuthRepository {

    override fun observeSession(): Flow<UserSession?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.toSession())
        }

        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun observeGuestMode(): Flow<Boolean> = authLocal.observeIsGuestAuth()

    override suspend fun signInWithGoogle(idToken: String): Result<UserSession> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val user = auth.signInWithCredential(credential).await().user
            ?: error("Google sign in failed")

        authLocal.markGoogleAuth()

        user.toSession()
    }

    override suspend fun continueAsGuest() {
        authLocal.markGuestAuth()
    }

    override fun getCurrentSession(): UserSession? = auth.currentUser?.toSession()

    override suspend fun signOut() {
        auth.signOut()
        authLocal.clearAuthMethod()
    }

    override suspend fun deleteAccount(): Result<Unit> = runCatching {
        val user = auth.currentUser ?: return@runCatching
        user.delete().await()
        auth.signOut()
        authLocal.clearAuthMethod()
    }

    private fun com.google.firebase.auth.FirebaseUser.toSession(): UserSession {
        return UserSession(
            uid = uid,
            displayName = displayName,
            email = email,
            photoUrl = photoUrl?.toString()
        )
    }
}

package com.vadymdev.habitix.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.vadymdev.habitix.domain.repository.ProfileRepository
import com.vadymdev.habitix.domain.repository.ProfileSyncRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

class FirestoreProfileSyncRepository(
    private val firestore: FirebaseFirestore,
    private val profileRepository: ProfileRepository
) : ProfileSyncRepository {

    companion object {
        private val syncMutex = Mutex()
    }

    override suspend fun clearUserData(userId: String) {
        firestore.collection("users")
            .document(userId)
            .collection("meta")
            .document("profile")
            .delete()
            .await()
    }

    override suspend fun sync(userId: String) {
        syncMutex.withLock {
            val local = profileRepository.getCurrentProfileIdentity()
            val docRef = firestore.collection("users").document(userId).collection("meta").document("profile")
            val cloudDoc = docRef.get().await()

            if (!cloudDoc.exists()) {
                upload(docPath = docRef.path, name = local.displayName, bio = local.bio, updatedAtMillis = local.updatedAtMillis)
                return
            }

            val remoteName = cloudDoc.getString("displayName").orEmpty()
            val remoteBio = cloudDoc.getString("bio").orEmpty()
            val remoteUpdatedAt = cloudDoc.getLong("updatedAtMillis") ?: 0L

            if (remoteUpdatedAt > local.updatedAtMillis) {
                profileRepository.replaceProfileIdentity(
                    displayName = remoteName,
                    bio = remoteBio.ifBlank { "Будую кращу версію себе" },
                    updatedAtMillis = remoteUpdatedAt
                )
            } else {
                upload(docPath = docRef.path, name = local.displayName, bio = local.bio, updatedAtMillis = local.updatedAtMillis)
            }
        }
    }

    private suspend fun upload(docPath: String, name: String, bio: String, updatedAtMillis: Long) {
        firestore.document(docPath)
            .set(
                mapOf(
                    "displayName" to name,
                    "bio" to bio,
                    "updatedAtMillis" to updatedAtMillis
                )
            )
            .await()
    }
}

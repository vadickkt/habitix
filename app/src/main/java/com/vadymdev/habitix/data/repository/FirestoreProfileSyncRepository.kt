package com.vadymdev.habitix.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.vadymdev.habitix.domain.repository.ProfileRepository
import com.vadymdev.habitix.domain.repository.ProfileSyncRepository
import kotlinx.coroutines.tasks.await

class FirestoreProfileSyncRepository(
    private val firestore: FirebaseFirestore,
    private val profileRepository: ProfileRepository
) : ProfileSyncRepository {

    override suspend fun clearUserData(userId: String) {
        firestore.collection("users")
            .document(userId)
            .collection("meta")
            .document("profile")
            .delete()
            .await()
    }

    override suspend fun sync(userId: String) {
        val local = profileRepository.getCurrentProfileIdentity()
        val docRef = firestore.collection("users").document(userId).collection("meta").document("profile")
        val cloudDoc = docRef.get().await()

        if (!cloudDoc.exists()) {
            upload(docPath = docRef.path, name = local.displayName, bio = local.bio)
            return
        }

        val remoteName = cloudDoc.getString("displayName").orEmpty()
        val remoteBio = cloudDoc.getString("bio").orEmpty()

        val localIsDefault = local.displayName == "Користувач" && local.bio == "Будую кращу версію себе"
        if (localIsDefault && remoteName.isNotBlank()) {
            profileRepository.replaceProfileIdentity(
                displayName = remoteName,
                bio = remoteBio.ifBlank { "Будую кращу версію себе" }
            )
        } else {
            upload(docPath = docRef.path, name = local.displayName, bio = local.bio)
        }
    }

    private suspend fun upload(docPath: String, name: String, bio: String) {
        firestore.document(docPath)
            .set(
                mapOf(
                    "displayName" to name,
                    "bio" to bio,
                    "updatedAtMillis" to System.currentTimeMillis()
                )
            )
            .await()
    }
}

package com.vadymdev.habitix.data.repository.sync

import com.google.firebase.firestore.FirebaseFirestore
import com.vadymdev.habitix.domain.repository.ProfileRepository
import kotlinx.coroutines.tasks.await

internal data class ProfileCloudRecord(
    val displayName: String,
    val bio: String,
    val updatedAtMillis: Long
)

internal interface ProfileCloudStore {
    suspend fun get(userId: String): ProfileCloudRecord?
    suspend fun set(userId: String, value: ProfileCloudRecord)
    suspend fun clear(userId: String)
}

internal class FirestoreProfileCloudStore(
    private val firestore: FirebaseFirestore
) : ProfileCloudStore {
    override suspend fun get(userId: String): ProfileCloudRecord? {
        val doc = firestore.collection("users")
            .document(userId)
            .collection("meta")
            .document("profile")
            .get()
            .await()

        if (!doc.exists()) return null

        return ProfileCloudRecord(
            displayName = doc.getString("displayName").orEmpty(),
            bio = doc.getString("bio").orEmpty(),
            updatedAtMillis = doc.getLong("updatedAtMillis") ?: 0L
        )
    }

    override suspend fun set(userId: String, value: ProfileCloudRecord) {
        firestore.collection("users")
            .document(userId)
            .collection("meta")
            .document("profile")
            .set(
                mapOf(
                    "displayName" to value.displayName,
                    "bio" to value.bio,
                    "updatedAtMillis" to value.updatedAtMillis
                )
            )
            .await()
    }

    override suspend fun clear(userId: String) {
        firestore.collection("users")
            .document(userId)
            .collection("meta")
            .document("profile")
            .delete()
            .await()
    }
}

internal class ProfileSyncContract(
    private val profileRepository: ProfileRepository,
    private val cloudStore: ProfileCloudStore
) {
    suspend fun clearUserData(userId: String) {
        cloudStore.clear(userId)
    }

    suspend fun sync(userId: String) {
        val local = profileRepository.getCurrentProfileIdentity()
        val remote = cloudStore.get(userId)

        if (remote == null) {
            cloudStore.set(
                userId,
                ProfileCloudRecord(
                    displayName = local.displayName,
                    bio = local.bio,
                    updatedAtMillis = local.updatedAtMillis
                )
            )
            return
        }

        if (remote.updatedAtMillis > local.updatedAtMillis) {
            profileRepository.replaceProfileIdentity(
                displayName = remote.displayName,
                bio = remote.bio.ifBlank { "Будую кращу версію себе" },
                updatedAtMillis = remote.updatedAtMillis
            )
        } else {
            cloudStore.set(
                userId,
                ProfileCloudRecord(
                    displayName = local.displayName,
                    bio = local.bio,
                    updatedAtMillis = local.updatedAtMillis
                )
            )
        }
    }
}

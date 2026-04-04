package com.vadymdev.habitix.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.vadymdev.habitix.data.repository.sync.FirestoreProfileCloudStore
import com.vadymdev.habitix.data.repository.sync.ProfileSyncContract
import com.vadymdev.habitix.data.repository.sync.mapSyncThrowable
import com.vadymdev.habitix.domain.model.SyncTarget
import com.vadymdev.habitix.domain.repository.ProfileRepository
import com.vadymdev.habitix.domain.repository.ProfileSyncRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FirestoreProfileSyncRepository(
    private val firestore: FirebaseFirestore,
    private val profileRepository: ProfileRepository
) : ProfileSyncRepository {

    companion object {
        private val syncMutex = Mutex()
    }

    override suspend fun clearUserData(userId: String) {
        runCatching {
            ProfileSyncContract(
                profileRepository = profileRepository,
                cloudStore = FirestoreProfileCloudStore(firestore)
            ).clearUserData(userId)
        }.getOrElse { throw mapSyncThrowable(SyncTarget.PROFILE, it) }
    }

    override suspend fun sync(userId: String) {
        runCatching {
            syncMutex.withLock {
                ProfileSyncContract(
                    profileRepository = profileRepository,
                    cloudStore = FirestoreProfileCloudStore(firestore)
                ).sync(userId)
            }
        }.getOrElse { throw mapSyncThrowable(SyncTarget.PROFILE, it) }
    }
}

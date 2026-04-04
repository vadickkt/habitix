package com.vadymdev.habitix.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.vadymdev.habitix.data.local.room.AchievementUnlockDao
import com.vadymdev.habitix.data.repository.sync.AchievementLocalStore
import com.vadymdev.habitix.data.repository.sync.AchievementSyncContract
import com.vadymdev.habitix.data.repository.sync.FirestoreAchievementCloudStore
import com.vadymdev.habitix.data.repository.sync.mapSyncThrowable
import com.vadymdev.habitix.domain.model.SyncTarget
import com.vadymdev.habitix.domain.repository.AchievementSyncRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FirestoreAchievementSyncRepository(
    private val firestore: FirebaseFirestore,
    private val achievementUnlockDao: AchievementUnlockDao
) : AchievementSyncRepository {

    companion object {
        private val syncMutex = Mutex()
    }

    private val contract = AchievementSyncContract(
        localStore = object : AchievementLocalStore {
            override suspend fun getAll() = achievementUnlockDao.getAllUnlocks()
            override suspend fun insertIgnore(entity: com.vadymdev.habitix.data.local.room.AchievementUnlockEntity) =
                achievementUnlockDao.insertIgnore(entity)

            override suspend fun insertOrReplace(entity: com.vadymdev.habitix.data.local.room.AchievementUnlockEntity) =
                achievementUnlockDao.insertOrReplace(entity)
        },
        cloudStore = FirestoreAchievementCloudStore(firestore)
    )

    override suspend fun clearUserData(userId: String) {
        runCatching {
            contract.clearUserData(userId)
        }.getOrElse { throw mapSyncThrowable(SyncTarget.ACHIEVEMENTS, it) }
    }

    override suspend fun sync(userId: String) {
        runCatching {
            syncMutex.withLock {
                contract.sync(userId)
            }
        }.getOrElse { throw mapSyncThrowable(SyncTarget.ACHIEVEMENTS, it) }
    }
}

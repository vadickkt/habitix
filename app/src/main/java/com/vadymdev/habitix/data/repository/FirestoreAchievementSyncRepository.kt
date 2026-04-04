package com.vadymdev.habitix.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.vadymdev.habitix.data.local.room.AchievementUnlockDao
import com.vadymdev.habitix.data.local.room.AchievementUnlockEntity
import com.vadymdev.habitix.data.repository.sync.mapSyncThrowable
import com.vadymdev.habitix.domain.model.SyncTarget
import com.vadymdev.habitix.domain.repository.AchievementSyncRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneOffset

class FirestoreAchievementSyncRepository(
    private val firestore: FirebaseFirestore,
    private val achievementUnlockDao: AchievementUnlockDao
) : AchievementSyncRepository {

    companion object {
        private val syncMutex = Mutex()
        private const val MAX_DELETE_RETRIES = 3
    }

    override suspend fun clearUserData(userId: String) {
        runCatching {
            deleteCollection("users/$userId/achievements")
        }.getOrElse { throw mapSyncThrowable(SyncTarget.ACHIEVEMENTS, it) }
    }

    override suspend fun sync(userId: String) {
        runCatching {
            syncMutex.withLock {
                val localUnlocks = achievementUnlockDao.getAllUnlocks().associateBy { it.achievementId }
                val remoteDocs = firestore.collection("users")
                    .document(userId)
                    .collection("achievements")
                    .get()
                    .await()
                    .documents

                val remoteById = remoteDocs.associateBy { it.id }

                localUnlocks.values.forEach { local ->
                    val remote = remoteById[local.achievementId]
                    val localUpdatedAt = epochDayToMillis(local.unlockedEpochDay)
                    val remoteUpdatedAt = remote?.getLong("updatedAtMillis") ?: 0L
                    if (remote == null || localUpdatedAt >= remoteUpdatedAt) {
                        uploadUnlock(userId = userId, unlock = local, updatedAtMillis = localUpdatedAt)
                    }
                }

                remoteDocs.forEach { doc ->
                    val achievementId = doc.getString("achievementId") ?: doc.id
                    val remoteEpochDay = doc.getLong("unlockedEpochDay") ?: return@forEach
                    val remoteUpdatedAt = doc.getLong("updatedAtMillis") ?: epochDayToMillis(remoteEpochDay)
                    val local = localUnlocks[achievementId]
                    if (local == null) {
                        achievementUnlockDao.insertIgnore(
                            AchievementUnlockEntity(
                                achievementId = achievementId,
                                unlockedEpochDay = remoteEpochDay
                            )
                        )
                        return@forEach
                    }

                    val localUpdatedAt = epochDayToMillis(local.unlockedEpochDay)
                    if (remoteUpdatedAt > localUpdatedAt) {
                        achievementUnlockDao.insertOrReplace(
                            AchievementUnlockEntity(
                                achievementId = achievementId,
                                unlockedEpochDay = remoteEpochDay
                            )
                        )
                    }
                }
            }
        }.getOrElse { throw mapSyncThrowable(SyncTarget.ACHIEVEMENTS, it) }
    }

    private suspend fun uploadUnlock(userId: String, unlock: AchievementUnlockEntity, updatedAtMillis: Long) {
        firestore.collection("users")
            .document(userId)
            .collection("achievements")
            .document(unlock.achievementId)
            .set(
                mapOf(
                    "achievementId" to unlock.achievementId,
                    "unlockedEpochDay" to unlock.unlockedEpochDay,
                    "updatedAtMillis" to updatedAtMillis,
                    "schemaVersion" to 1
                )
            )
            .await()
    }

    private fun epochDayToMillis(epochDay: Long): Long {
        return LocalDate.ofEpochDay(epochDay).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
    }

    private suspend fun deleteCollection(path: String) {
        var retries = 0
        while (true) {
            try {
                val batch = firestore.batch()
                val snapshot = firestore.collection(path)
                    .limit(200)
                    .get()
                    .await()

                if (snapshot.isEmpty) return

                snapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit().await()
                retries = 0
            } catch (error: FirebaseFirestoreException) {
                retries += 1
                if (retries >= MAX_DELETE_RETRIES) throw error
                delay(500L * retries)
            }
        }
    }
}

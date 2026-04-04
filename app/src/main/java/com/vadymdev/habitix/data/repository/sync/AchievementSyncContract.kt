package com.vadymdev.habitix.data.repository.sync

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.vadymdev.habitix.data.local.room.AchievementUnlockEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneOffset

internal data class AchievementCloudRecord(
    val achievementId: String,
    val unlockedEpochDay: Long,
    val updatedAtMillis: Long
)

internal interface AchievementCloudStore {
    suspend fun fetchAll(userId: String): List<AchievementCloudRecord>
    suspend fun upsert(userId: String, record: AchievementCloudRecord)
    suspend fun clearAll(userId: String)
}

internal interface AchievementLocalStore {
    suspend fun getAll(): List<AchievementUnlockEntity>
    suspend fun insertIgnore(entity: AchievementUnlockEntity)
    suspend fun insertOrReplace(entity: AchievementUnlockEntity)
}

internal class AchievementSyncContract(
    private val localStore: AchievementLocalStore,
    private val cloudStore: AchievementCloudStore
) {
    suspend fun clearUserData(userId: String) {
        cloudStore.clearAll(userId)
    }

    suspend fun sync(userId: String) {
        val localUnlocks = localStore.getAll().associateBy { it.achievementId }
        val remoteRecords = cloudStore.fetchAll(userId)
        val remoteById = remoteRecords.associateBy { it.achievementId }

        localUnlocks.values.forEach { local ->
            val remote = remoteById[local.achievementId]
            val localUpdatedAt = epochDayToMillis(local.unlockedEpochDay)
            val remoteUpdatedAt = remote?.updatedAtMillis ?: 0L

            if (remote == null || localUpdatedAt >= remoteUpdatedAt) {
                cloudStore.upsert(
                    userId,
                    AchievementCloudRecord(
                        achievementId = local.achievementId,
                        unlockedEpochDay = local.unlockedEpochDay,
                        updatedAtMillis = localUpdatedAt
                    )
                )
            }
        }

        remoteRecords.forEach { remote ->
            val local = localUnlocks[remote.achievementId]
            if (local == null) {
                localStore.insertIgnore(
                    AchievementUnlockEntity(
                        achievementId = remote.achievementId,
                        unlockedEpochDay = remote.unlockedEpochDay
                    )
                )
                return@forEach
            }

            val localUpdatedAt = epochDayToMillis(local.unlockedEpochDay)
            if (remote.updatedAtMillis > localUpdatedAt) {
                localStore.insertOrReplace(
                    AchievementUnlockEntity(
                        achievementId = remote.achievementId,
                        unlockedEpochDay = remote.unlockedEpochDay
                    )
                )
            }
        }
    }

    private fun epochDayToMillis(epochDay: Long): Long {
        return LocalDate.ofEpochDay(epochDay)
            .atStartOfDay()
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()
    }
}

internal class FirestoreAchievementCloudStore(
    private val firestore: FirebaseFirestore
) : AchievementCloudStore {

    companion object {
        private const val MAX_DELETE_RETRIES = 3
    }

    override suspend fun fetchAll(userId: String): List<AchievementCloudRecord> {
        val docs = firestore.collection("users")
            .document(userId)
            .collection("achievements")
            .get()
            .await()
            .documents

        return docs.mapNotNull { doc ->
            val achievementId = doc.getString("achievementId") ?: doc.id
            val unlockedEpochDay = doc.getLong("unlockedEpochDay") ?: return@mapNotNull null
            val updatedAtMillis = doc.getLong("updatedAtMillis") ?: 0L
            AchievementCloudRecord(
                achievementId = achievementId,
                unlockedEpochDay = unlockedEpochDay,
                updatedAtMillis = updatedAtMillis
            )
        }
    }

    override suspend fun upsert(userId: String, record: AchievementCloudRecord) {
        firestore.collection("users")
            .document(userId)
            .collection("achievements")
            .document(record.achievementId)
            .set(
                mapOf(
                    "achievementId" to record.achievementId,
                    "unlockedEpochDay" to record.unlockedEpochDay,
                    "updatedAtMillis" to record.updatedAtMillis,
                    "schemaVersion" to 1
                )
            )
            .await()
    }

    override suspend fun clearAll(userId: String) {
        deleteCollection("users/$userId/achievements")
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

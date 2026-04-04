package com.vadymdev.habitix.data.repository.sync

import com.vadymdev.habitix.data.local.room.AchievementUnlockEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementSyncContractTest {

    @Test
    fun localMissingInCloud_uploadsRecordToCloud() = runBlocking {
        val local = FakeAchievementLocalStore(
            listOf(AchievementUnlockEntity("week_7", unlockedEpochDay = 20L))
        )
        val cloud = FakeAchievementCloudStore(
            mutableListOf(
                AchievementCloudRecord("week_7", unlockedEpochDay = 10L, updatedAtMillis = 10L)
            )
        )

        AchievementSyncContract(local, cloud).sync("uid")

        assertTrue(cloud.upserted.any { it.achievementId == "week_7" && it.unlockedEpochDay == 20L })
    }

    @Test
    fun localNewerThanRemote_uploadsRecordToCloud() = runBlocking {
        val local = FakeAchievementLocalStore(
            listOf(AchievementUnlockEntity("week_7", unlockedEpochDay = 40L))
        )
        val cloud = FakeAchievementCloudStore(
            mutableListOf(
                AchievementCloudRecord("week_7", unlockedEpochDay = 10L, updatedAtMillis = 10L)
            )
        )

        AchievementSyncContract(local, cloud).sync("uid")

        assertTrue(cloud.upserted.any { it.achievementId == "week_7" && it.unlockedEpochDay == 40L })
    }

    @Test
    fun localOlderThanRemote_doesNotUploadLocalRecord() = runBlocking {
        val local = FakeAchievementLocalStore(
            listOf(AchievementUnlockEntity("month_30", unlockedEpochDay = 10L))
        )
        val cloud = FakeAchievementCloudStore(
            mutableListOf(
                AchievementCloudRecord("month_30", unlockedEpochDay = 40L, updatedAtMillis = Long.MAX_VALUE)
            )
        )

        AchievementSyncContract(local, cloud).sync("uid")

        assertTrue(cloud.upserted.none { it.achievementId == "month_30" })
    }

    @Test
    fun remoteNewer_replacesLocal() = runBlocking {
        val local = FakeAchievementLocalStore(
            listOf(AchievementUnlockEntity("month_30", unlockedEpochDay = 10L))
        )
        val cloud = FakeAchievementCloudStore(
            mutableListOf(
                AchievementCloudRecord("month_30", unlockedEpochDay = 40L, updatedAtMillis = Long.MAX_VALUE)
            )
        )

        AchievementSyncContract(local, cloud).sync("uid")

        assertEquals(40L, local.replaced.first().unlockedEpochDay)
    }

    @Test
    fun remoteMissingLocally_isInserted() = runBlocking {
        val local = FakeAchievementLocalStore(emptyList())
        val cloud = FakeAchievementCloudStore(
            mutableListOf(
                AchievementCloudRecord("first", unlockedEpochDay = 2L, updatedAtMillis = 200L)
            )
        )

        AchievementSyncContract(local, cloud).sync("uid")

        assertTrue(local.getAll().any { it.achievementId == "first" && it.unlockedEpochDay == 2L })
    }

    @Test
    fun clearUserData_clearsCloudCollection() = runBlocking {
        val local = FakeAchievementLocalStore(emptyList())
        val cloud = FakeAchievementCloudStore(mutableListOf())

        AchievementSyncContract(local, cloud).clearUserData("uid")

        assertTrue(cloud.cleared)
    }

    private class FakeAchievementLocalStore(initial: List<AchievementUnlockEntity>) : AchievementLocalStore {
        private val data = initial.toMutableList()
        val replaced = mutableListOf<AchievementUnlockEntity>()

        override suspend fun getAll(): List<AchievementUnlockEntity> = data.toList()

        override suspend fun insertIgnore(entity: AchievementUnlockEntity) {
            if (data.none { it.achievementId == entity.achievementId }) {
                data.add(entity)
            }
        }

        override suspend fun insertOrReplace(entity: AchievementUnlockEntity) {
            replaced.add(entity)
            data.removeAll { it.achievementId == entity.achievementId }
            data.add(entity)
        }
    }

    private class FakeAchievementCloudStore(
        private val data: MutableList<AchievementCloudRecord>
    ) : AchievementCloudStore {
        val upserted = mutableListOf<AchievementCloudRecord>()
        var cleared = false

        override suspend fun fetchAll(userId: String): List<AchievementCloudRecord> = data.toList()

        override suspend fun upsert(userId: String, record: AchievementCloudRecord) {
            upserted.add(record)
            data.removeAll { it.achievementId == record.achievementId }
            data.add(record)
        }

        override suspend fun clearAll(userId: String) {
            cleared = true
            data.clear()
        }
    }
}

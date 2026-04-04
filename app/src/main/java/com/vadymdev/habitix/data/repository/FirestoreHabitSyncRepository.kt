package com.vadymdev.habitix.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.vadymdev.habitix.data.local.room.HabitCompletionDao
import com.vadymdev.habitix.data.local.room.HabitCompletionEntity
import com.vadymdev.habitix.data.local.room.HabitDao
import com.vadymdev.habitix.data.local.room.HabitEntity
import com.vadymdev.habitix.data.local.room.HiddenHabitDayDao
import com.vadymdev.habitix.data.local.room.HiddenHabitDayEntity
import com.vadymdev.habitix.data.repository.sync.FirestoreHabitCloudStore
import com.vadymdev.habitix.data.repository.sync.HabitCloudRecord
import com.vadymdev.habitix.data.repository.sync.HabitSyncContract
import com.vadymdev.habitix.data.repository.sync.HabitSyncLocalStore
import com.vadymdev.habitix.data.repository.sync.mapSyncThrowable
import com.vadymdev.habitix.domain.model.SyncTarget
import com.vadymdev.habitix.domain.repository.HabitSyncRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FirestoreHabitSyncRepository(
    private val firestore: FirebaseFirestore,
    private val habitDao: HabitDao,
    private val completionDao: HabitCompletionDao,
    private val hiddenDayDao: HiddenHabitDayDao
) : HabitSyncRepository {

    companion object {
        private val syncMutex = Mutex()
    }

    private val contract = HabitSyncContract(
        localStore = object : HabitSyncLocalStore {
            override suspend fun getAllHabits() = habitDao.getAllHabits()
            override suspend fun findHabitByCloudId(cloudId: String) = habitDao.findByCloudId(cloudId)
            override suspend fun findHabitByTitle(title: String) = habitDao.findByTitle(title)
            override suspend fun updateCloudId(id: Long, cloudId: String) = habitDao.updateCloudId(id, cloudId)

            override suspend fun updateHabitFromCloud(record: HabitCloudRecord, localId: Long) {
                habitDao.updateFromCloud(
                    id = localId,
                    title = record.title,
                    iconKey = record.iconKey,
                    colorKey = record.colorKey,
                    frequencyType = record.frequencyType,
                    customDaysCsv = record.customDaysCsv,
                    reminderEnabled = record.reminderEnabled,
                    reminderHour = record.reminderHour,
                    reminderMinute = record.reminderMinute,
                    startEpochDay = record.startEpochDay,
                    activeUntilEpochDay = record.activeUntilEpochDay,
                    isArchived = record.isArchived
                )
            }

            override suspend fun insertHabitFromCloud(record: HabitCloudRecord) {
                habitDao.insertHabit(
                    HabitEntity(
                        cloudId = record.cloudId,
                        title = record.title,
                        iconKey = record.iconKey,
                        colorKey = record.colorKey,
                        frequencyType = record.frequencyType,
                        customDaysCsv = record.customDaysCsv,
                        reminderEnabled = record.reminderEnabled,
                        reminderHour = record.reminderHour,
                        reminderMinute = record.reminderMinute,
                        createdAt = System.currentTimeMillis(),
                        startEpochDay = record.startEpochDay,
                        activeUntilEpochDay = record.activeUntilEpochDay,
                        isArchived = record.isArchived,
                        source = record.source
                    )
                )
            }

            override suspend fun getAllCompletions() = completionDao.getAllCompletions()
            override suspend fun upsertCompletion(entity: HabitCompletionEntity) = completionDao.upsertCompletion(entity)
            override suspend fun getAllHiddenDays() = hiddenDayDao.getAllHiddenDays()
            override suspend fun upsertHiddenDay(entity: HiddenHabitDayEntity) = hiddenDayDao.upsert(entity)
        },
        cloudStore = FirestoreHabitCloudStore(firestore)
    )

    override suspend fun clearUserData(userId: String) {
        runCatching {
            contract.clearUserData(userId)
        }.getOrElse { throw mapSyncThrowable(SyncTarget.HABITS, it) }
    }

    override suspend fun syncUserHabits(userId: String) {
        runCatching {
            syncMutex.withLock {
                contract.syncUserHabits(userId)
            }
        }.getOrElse { throw mapSyncThrowable(SyncTarget.HABITS, it) }
    }
}

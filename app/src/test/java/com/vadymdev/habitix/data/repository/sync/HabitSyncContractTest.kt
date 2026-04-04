package com.vadymdev.habitix.data.repository.sync

import com.vadymdev.habitix.data.local.room.HabitCompletionEntity
import com.vadymdev.habitix.data.local.room.HabitEntity
import com.vadymdev.habitix.data.local.room.HiddenHabitDayEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitSyncContractTest {

    @Test
    fun guestLocalAndCloudHabits_mergeWithoutDataLoss() = runBlocking {
        val localStore = FakeHabitLocalStore(
            habits = mutableListOf(
                HabitEntity(
                    id = 1,
                    cloudId = "",
                    title = "Read",
                    iconKey = "book",
                    colorKey = "mint",
                    frequencyType = "DAILY",
                    customDaysCsv = "",
                    reminderEnabled = true,
                    reminderHour = 8,
                    reminderMinute = 0,
                    createdAt = 1L,
                    startEpochDay = 1L,
                    activeUntilEpochDay = null,
                    isArchived = false,
                    source = "guest"
                )
            )
        )
        val cloudStore = FakeHabitCloudStore().apply {
            habits.add(
                HabitCloudRecord(
                    cloudId = "remote-1",
                    title = "Workout",
                    iconKey = "sport",
                    colorKey = "sky",
                    frequencyType = "DAILY",
                    customDaysCsv = "",
                    reminderEnabled = true,
                    reminderHour = 9,
                    reminderMinute = 0,
                    startEpochDay = 2L,
                    activeUntilEpochDay = null,
                    isArchived = false,
                    source = "cloud"
                )
            )
        }

        HabitSyncContract(localStore, cloudStore, cloudIdGenerator = { "generated-read" }).syncUserHabits("uid")

        assertTrue(localStore.habits.any { it.title == "Read" })
        assertTrue(localStore.habits.any { it.title == "Workout" })
        assertTrue(localStore.habits.any { it.title == "Read" && it.cloudId == "generated-read" })
        assertTrue(cloudStore.habits.any { it.cloudId == "generated-read" && it.title == "Read" })
    }

    @Test
    fun localHabitWithoutCloudId_getsGeneratedAndUploaded() = runBlocking {
        val localStore = FakeHabitLocalStore(
            habits = mutableListOf(
                HabitEntity(
                    id = 1,
                    cloudId = "",
                    title = "Read",
                    iconKey = "reading",
                    colorKey = "mint",
                    frequencyType = "DAILY",
                    customDaysCsv = "",
                    reminderEnabled = true,
                    reminderHour = 8,
                    reminderMinute = 0,
                    createdAt = 1L,
                    startEpochDay = 1L,
                    activeUntilEpochDay = null,
                    isArchived = false,
                    source = "manual"
                )
            )
        )
        val cloudStore = FakeHabitCloudStore()

        HabitSyncContract(localStore, cloudStore, cloudIdGenerator = { "generated-id" }).syncUserHabits("uid")

        assertEquals("generated-id", localStore.habits.first().cloudId)
        assertTrue(cloudStore.habits.any { it.cloudId == "generated-id" })
    }

    @Test
    fun localHabitWithGeneratedCloudId_overwritesCloudRecord() = runBlocking {
        val localStore = FakeHabitLocalStore(
            habits = mutableListOf(
                HabitEntity(
                    id = 10,
                    cloudId = "",
                    title = "Meditate",
                    iconKey = "mind",
                    colorKey = "sky",
                    frequencyType = "DAILY",
                    customDaysCsv = "",
                    reminderEnabled = true,
                    reminderHour = 9,
                    reminderMinute = 0,
                    createdAt = 1L,
                    startEpochDay = 1L,
                    activeUntilEpochDay = null,
                    isArchived = false,
                    source = "manual"
                )
            )
        )
        val cloudStore = FakeHabitCloudStore().apply {
            habits.add(
                HabitCloudRecord(
                    cloudId = "generated-id",
                    title = "Meditate",
                    iconKey = "mind",
                    colorKey = "sky",
                    frequencyType = "DAILY",
                    customDaysCsv = "",
                    reminderEnabled = true,
                    reminderHour = 10,
                    reminderMinute = 30,
                    startEpochDay = 5L,
                    activeUntilEpochDay = null,
                    isArchived = false,
                    source = "cloud"
                )
            )
        }

        HabitSyncContract(localStore, cloudStore, cloudIdGenerator = { "generated-id" }).syncUserHabits("uid")

        assertEquals("generated-id", localStore.habits.first().cloudId)
        val cloudHabit = cloudStore.habits.first { it.cloudId == "generated-id" }
        assertEquals(9, cloudHabit.reminderHour)
        assertEquals(0, cloudHabit.reminderMinute)
    }

    @Test
    fun clearUserData_callsCloudClear() = runBlocking {
        val localStore = FakeHabitLocalStore()
        val cloudStore = FakeHabitCloudStore()

        HabitSyncContract(localStore, cloudStore).clearUserData("uid")

        assertTrue(cloudStore.cleared)
    }

    private class FakeHabitLocalStore(
        val habits: MutableList<HabitEntity> = mutableListOf(),
        val completions: MutableList<HabitCompletionEntity> = mutableListOf(),
        val hiddenDays: MutableList<HiddenHabitDayEntity> = mutableListOf()
    ) : HabitSyncLocalStore {
        override suspend fun getAllHabits(): List<HabitEntity> = habits.toList()

        override suspend fun findHabitByCloudId(cloudId: String): HabitEntity? = habits.firstOrNull { it.cloudId == cloudId }

        override suspend fun findHabitByTitle(title: String): HabitEntity? = habits.firstOrNull { it.title == title }

        override suspend fun updateCloudId(id: Long, cloudId: String) {
            habits.replaceAll { if (it.id == id) it.copy(cloudId = cloudId) else it }
        }

        override suspend fun updateHabitFromCloud(record: HabitCloudRecord, localId: Long) {
            habits.replaceAll {
                if (it.id == localId) {
                    it.copy(
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
                } else {
                    it
                }
            }
        }

        override suspend fun insertHabitFromCloud(record: HabitCloudRecord) {
            habits.add(
                HabitEntity(
                    id = (habits.maxOfOrNull { it.id } ?: 0L) + 1L,
                    cloudId = record.cloudId,
                    title = record.title,
                    iconKey = record.iconKey,
                    colorKey = record.colorKey,
                    frequencyType = record.frequencyType,
                    customDaysCsv = record.customDaysCsv,
                    reminderEnabled = record.reminderEnabled,
                    reminderHour = record.reminderHour,
                    reminderMinute = record.reminderMinute,
                    createdAt = 0L,
                    startEpochDay = record.startEpochDay,
                    activeUntilEpochDay = record.activeUntilEpochDay,
                    isArchived = record.isArchived,
                    source = record.source
                )
            )
        }

        override suspend fun getAllCompletions(): List<HabitCompletionEntity> = completions.toList()

        override suspend fun upsertCompletion(entity: HabitCompletionEntity) {
            completions.removeAll { it.habitId == entity.habitId && it.dateEpochDay == entity.dateEpochDay }
            completions.add(entity)
        }

        override suspend fun getAllHiddenDays(): List<HiddenHabitDayEntity> = hiddenDays.toList()

        override suspend fun upsertHiddenDay(entity: HiddenHabitDayEntity) {
            hiddenDays.removeAll { it.habitId == entity.habitId && it.dateEpochDay == entity.dateEpochDay }
            hiddenDays.add(entity)
        }
    }

    private class FakeHabitCloudStore : HabitSyncCloudStore {
        val habits = mutableListOf<HabitCloudRecord>()
        val completions = mutableListOf<CompletionCloudRecord>()
        val hidden = mutableListOf<HiddenDayCloudRecord>()
        var cleared = false

        override suspend fun fetchHabits(userId: String): List<HabitCloudRecord> = habits.toList()

        override suspend fun upsertHabit(userId: String, record: HabitCloudRecord) {
            habits.removeAll { it.cloudId == record.cloudId }
            habits.add(record)
        }

        override suspend fun fetchCompletions(userId: String): List<CompletionCloudRecord> = completions.toList()

        override suspend fun upsertCompletion(userId: String, record: CompletionCloudRecord) {
            completions.removeAll { it.cloudId == record.cloudId && it.dateEpochDay == record.dateEpochDay }
            completions.add(record)
        }

        override suspend fun fetchHiddenDays(userId: String): List<HiddenDayCloudRecord> = hidden.toList()

        override suspend fun upsertHiddenDay(userId: String, record: HiddenDayCloudRecord) {
            hidden.removeAll { it.cloudId == record.cloudId && it.dateEpochDay == record.dateEpochDay }
            hidden.add(record)
        }

        override suspend fun clearAllData(userId: String) {
            cleared = true
            habits.clear()
            completions.clear()
            hidden.clear()
        }
    }
}

package com.vadymdev.habitix.data.repository.sync

import com.google.firebase.firestore.FirebaseFirestore
import com.vadymdev.habitix.data.local.room.HabitCompletionEntity
import com.vadymdev.habitix.data.local.room.HabitEntity
import com.vadymdev.habitix.data.local.room.HiddenHabitDayEntity
import java.util.UUID

internal data class HabitCloudRecord(
    val cloudId: String,
    val title: String,
    val iconKey: String,
    val colorKey: String,
    val frequencyType: String,
    val customDaysCsv: String,
    val reminderEnabled: Boolean,
    val reminderHour: Int,
    val reminderMinute: Int,
    val startEpochDay: Long,
    val activeUntilEpochDay: Long?,
    val isArchived: Boolean,
    val source: String
)

internal data class CompletionCloudRecord(
    val cloudId: String,
    val dateEpochDay: Long,
    val completedAtMillis: Long
)

internal data class HiddenDayCloudRecord(
    val cloudId: String,
    val dateEpochDay: Long
)

internal interface HabitSyncLocalStore {
    suspend fun getAllHabits(): List<HabitEntity>
    suspend fun findHabitByCloudId(cloudId: String): HabitEntity?
    suspend fun findHabitByTitle(title: String): HabitEntity?
    suspend fun updateCloudId(id: Long, cloudId: String)
    suspend fun updateHabitFromCloud(record: HabitCloudRecord, localId: Long)
    suspend fun insertHabitFromCloud(record: HabitCloudRecord)

    suspend fun getAllCompletions(): List<HabitCompletionEntity>
    suspend fun upsertCompletion(entity: HabitCompletionEntity)

    suspend fun getAllHiddenDays(): List<HiddenHabitDayEntity>
    suspend fun upsertHiddenDay(entity: HiddenHabitDayEntity)
}

internal interface HabitSyncCloudStore {
    suspend fun fetchHabits(userId: String): List<HabitCloudRecord>
    suspend fun upsertHabit(userId: String, record: HabitCloudRecord)

    suspend fun fetchCompletions(userId: String): List<CompletionCloudRecord>
    suspend fun upsertCompletion(userId: String, record: CompletionCloudRecord)

    suspend fun fetchHiddenDays(userId: String): List<HiddenDayCloudRecord>
    suspend fun upsertHiddenDay(userId: String, record: HiddenDayCloudRecord)

    suspend fun clearAllData(userId: String)
}

internal class HabitSyncContract(
    private val localStore: HabitSyncLocalStore,
    private val cloudStore: HabitSyncCloudStore,
    private val cloudIdGenerator: () -> String = { UUID.randomUUID().toString() }
) {
    suspend fun clearUserData(userId: String) {
        cloudStore.clearAllData(userId)
    }

    suspend fun syncUserHabits(userId: String) {
        uploadLocalHabits(userId)
        downloadCloudHabits(userId)
        uploadLocalCompletions(userId)
        downloadCloudCompletions(userId)
        uploadLocalHiddenDays(userId)
        downloadCloudHiddenDays(userId)
    }

    private suspend fun uploadLocalHabits(userId: String) {
        localStore.getAllHabits().forEach { habit ->
            val resolvedCloudId = ensureCloudId(habit)
            cloudStore.upsertHabit(
                userId,
                HabitCloudRecord(
                    cloudId = resolvedCloudId,
                    title = habit.title,
                    iconKey = habit.iconKey,
                    colorKey = habit.colorKey,
                    frequencyType = habit.frequencyType,
                    customDaysCsv = habit.customDaysCsv,
                    reminderEnabled = habit.reminderEnabled,
                    reminderHour = habit.reminderHour,
                    reminderMinute = habit.reminderMinute,
                    startEpochDay = habit.startEpochDay,
                    activeUntilEpochDay = habit.activeUntilEpochDay,
                    isArchived = habit.isArchived,
                    source = habit.source
                )
            )
        }
    }

    private suspend fun downloadCloudHabits(userId: String) {
        cloudStore.fetchHabits(userId).forEach { remote ->
            val existingCloud = localStore.findHabitByCloudId(remote.cloudId)
            if (existingCloud != null) {
                localStore.updateHabitFromCloud(remote, existingCloud.id)
                return@forEach
            }

            val existingByTitle = localStore.findHabitByTitle(remote.title)
            if (existingByTitle != null && existingByTitle.cloudId.isBlank()) {
                localStore.updateCloudId(existingByTitle.id, remote.cloudId)
                localStore.updateHabitFromCloud(remote, existingByTitle.id)
                return@forEach
            }

            localStore.insertHabitFromCloud(remote)
        }
    }

    private suspend fun uploadLocalCompletions(userId: String) {
        val habitsById = localStore.getAllHabits().associateBy { it.id }
        localStore.getAllCompletions().forEach { completion ->
            val habit = habitsById[completion.habitId] ?: return@forEach
            val resolvedCloudId = ensureCloudId(habit)
            cloudStore.upsertCompletion(
                userId,
                CompletionCloudRecord(
                    cloudId = resolvedCloudId,
                    dateEpochDay = completion.dateEpochDay,
                    completedAtMillis = completion.completedAtMillis
                )
            )
        }
    }

    private suspend fun downloadCloudCompletions(userId: String) {
        cloudStore.fetchCompletions(userId).forEach { remote ->
            val localHabit = localStore.findHabitByCloudId(remote.cloudId) ?: return@forEach
            localStore.upsertCompletion(
                HabitCompletionEntity(
                    habitId = localHabit.id,
                    dateEpochDay = remote.dateEpochDay,
                    completedAtMillis = remote.completedAtMillis
                )
            )
        }
    }

    private suspend fun uploadLocalHiddenDays(userId: String) {
        val habitsById = localStore.getAllHabits().associateBy { it.id }
        localStore.getAllHiddenDays().forEach { hidden ->
            val habit = habitsById[hidden.habitId] ?: return@forEach
            val resolvedCloudId = ensureCloudId(habit)
            cloudStore.upsertHiddenDay(
                userId,
                HiddenDayCloudRecord(
                    cloudId = resolvedCloudId,
                    dateEpochDay = hidden.dateEpochDay
                )
            )
        }
    }

    private suspend fun downloadCloudHiddenDays(userId: String) {
        cloudStore.fetchHiddenDays(userId).forEach { remote ->
            val localHabit = localStore.findHabitByCloudId(remote.cloudId) ?: return@forEach
            localStore.upsertHiddenDay(
                HiddenHabitDayEntity(
                    habitId = localHabit.id,
                    dateEpochDay = remote.dateEpochDay
                )
            )
        }
    }

    private suspend fun ensureCloudId(habit: HabitEntity): String {
        if (habit.cloudId.isNotBlank()) return habit.cloudId
        val generated = cloudIdGenerator()
        localStore.updateCloudId(habit.id, generated)
        return generated
    }
}

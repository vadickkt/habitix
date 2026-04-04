package com.vadymdev.habitix.data.repository.sync

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.vadymdev.habitix.data.local.room.HabitCompletionEntity
import com.vadymdev.habitix.data.local.room.HabitEntity
import com.vadymdev.habitix.data.local.room.HiddenHabitDayEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
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

internal class FirestoreHabitCloudStore(
    private val firestore: FirebaseFirestore
) : HabitSyncCloudStore {

    companion object {
        private const val MAX_DELETE_RETRIES = 3
    }

    override suspend fun fetchHabits(userId: String): List<HabitCloudRecord> {
        val docs = firestore.collection("users")
            .document(userId)
            .collection("habits")
            .get()
            .await()
            .documents

        return docs.mapNotNull { doc ->
            val title = doc.getString("title") ?: return@mapNotNull null
            HabitCloudRecord(
                cloudId = doc.id,
                title = title,
                iconKey = doc.getString("iconKey") ?: "water",
                colorKey = doc.getString("colorKey") ?: "mint",
                frequencyType = doc.getString("frequencyType") ?: "DAILY",
                customDaysCsv = doc.getString("customDaysCsv") ?: "",
                reminderEnabled = doc.getBoolean("reminderEnabled") ?: true,
                reminderHour = (doc.getLong("reminderHour") ?: 20L).toInt(),
                reminderMinute = (doc.getLong("reminderMinute") ?: 0L).toInt(),
                startEpochDay = doc.getLong("startEpochDay") ?: LocalDate.now().toEpochDay(),
                activeUntilEpochDay = doc.getLong("activeUntilEpochDay"),
                isArchived = doc.getBoolean("isArchived") ?: false,
                source = doc.getString("source") ?: "cloud"
            )
        }
    }

    override suspend fun upsertHabit(userId: String, record: HabitCloudRecord) {
        firestore.collection("users")
            .document(userId)
            .collection("habits")
            .document(record.cloudId)
            .set(
                mapOf(
                    "title" to record.title,
                    "iconKey" to record.iconKey,
                    "colorKey" to record.colorKey,
                    "frequencyType" to record.frequencyType,
                    "customDaysCsv" to record.customDaysCsv,
                    "reminderEnabled" to record.reminderEnabled,
                    "reminderHour" to record.reminderHour,
                    "reminderMinute" to record.reminderMinute,
                    "startEpochDay" to record.startEpochDay,
                    "activeUntilEpochDay" to record.activeUntilEpochDay,
                    "isArchived" to record.isArchived,
                    "source" to record.source,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .await()
    }

    override suspend fun fetchCompletions(userId: String): List<CompletionCloudRecord> {
        val docs = firestore.collection("users")
            .document(userId)
            .collection("habit_completions")
            .get()
            .await()
            .documents

        return docs.mapNotNull { doc ->
            val cloudId = doc.getString("cloudId") ?: return@mapNotNull null
            val dateEpochDay = doc.getLong("dateEpochDay") ?: return@mapNotNull null
            CompletionCloudRecord(
                cloudId = cloudId,
                dateEpochDay = dateEpochDay,
                completedAtMillis = doc.getLong("completedAtMillis") ?: System.currentTimeMillis()
            )
        }
    }

    override suspend fun upsertCompletion(userId: String, record: CompletionCloudRecord) {
        val docId = "${record.cloudId}_${record.dateEpochDay}"
        firestore.collection("users")
            .document(userId)
            .collection("habit_completions")
            .document(docId)
            .set(
                mapOf(
                    "cloudId" to record.cloudId,
                    "dateEpochDay" to record.dateEpochDay,
                    "completedAtMillis" to record.completedAtMillis
                )
            )
            .await()
    }

    override suspend fun fetchHiddenDays(userId: String): List<HiddenDayCloudRecord> {
        val docs = firestore.collection("users")
            .document(userId)
            .collection("habit_hidden_days")
            .get()
            .await()
            .documents

        return docs.mapNotNull { doc ->
            val cloudId = doc.getString("cloudId") ?: return@mapNotNull null
            val dateEpochDay = doc.getLong("dateEpochDay") ?: return@mapNotNull null
            HiddenDayCloudRecord(
                cloudId = cloudId,
                dateEpochDay = dateEpochDay
            )
        }
    }

    override suspend fun upsertHiddenDay(userId: String, record: HiddenDayCloudRecord) {
        val docId = "${record.cloudId}_${record.dateEpochDay}"
        firestore.collection("users")
            .document(userId)
            .collection("habit_hidden_days")
            .document(docId)
            .set(
                mapOf(
                    "cloudId" to record.cloudId,
                    "dateEpochDay" to record.dateEpochDay
                )
            )
            .await()
    }

    override suspend fun clearAllData(userId: String) {
        deleteCollection("users/$userId/habits")
        deleteCollection("users/$userId/habit_completions")
        deleteCollection("users/$userId/habit_hidden_days")
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

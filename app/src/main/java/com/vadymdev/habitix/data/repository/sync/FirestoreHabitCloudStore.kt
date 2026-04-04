package com.vadymdev.habitix.data.repository.sync

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

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

package com.vadymdev.habitix.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.vadymdev.habitix.data.local.room.HabitCompletionDao
import com.vadymdev.habitix.data.local.room.HabitCompletionEntity
import com.vadymdev.habitix.data.local.room.HabitDao
import com.vadymdev.habitix.data.local.room.HabitEntity
import com.vadymdev.habitix.data.local.room.HiddenHabitDayDao
import com.vadymdev.habitix.data.local.room.HiddenHabitDayEntity
import com.vadymdev.habitix.domain.repository.HabitSyncRepository
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.util.UUID

class FirestoreHabitSyncRepository(
    private val firestore: FirebaseFirestore,
    private val habitDao: HabitDao,
    private val completionDao: HabitCompletionDao,
    private val hiddenDayDao: HiddenHabitDayDao
) : HabitSyncRepository {

    override suspend fun clearUserData(userId: String) {
        deleteCollection("users/$userId/habits")
        deleteCollection("users/$userId/habit_completions")
        deleteCollection("users/$userId/habit_hidden_days")
    }

    override suspend fun syncUserHabits(userId: String) {
        uploadLocalHabits(userId)
        downloadCloudHabits(userId)
        uploadLocalCompletions(userId)
        downloadCloudCompletions(userId)
        uploadLocalHiddenDays(userId)
        downloadCloudHiddenDays(userId)
    }

    private suspend fun uploadLocalHabits(userId: String) {
        val habits = habitDao.getAllHabits()
        habits.forEach { habit ->
            val cloudId = habit.cloudId.ifBlank {
                UUID.randomUUID().toString().also { generated ->
                    habitDao.updateCloudId(habit.id, generated)
                }
            }

            firestore.collection("users")
                .document(userId)
                .collection("habits")
                .document(cloudId)
                .set(
                    mapOf(
                        "title" to habit.title,
                        "iconKey" to habit.iconKey,
                        "colorKey" to habit.colorKey,
                        "frequencyType" to habit.frequencyType,
                        "customDaysCsv" to habit.customDaysCsv,
                        "reminderEnabled" to habit.reminderEnabled,
                        "reminderHour" to habit.reminderHour,
                        "reminderMinute" to habit.reminderMinute,
                        "startEpochDay" to habit.startEpochDay,
                        "activeUntilEpochDay" to habit.activeUntilEpochDay,
                        "isArchived" to habit.isArchived,
                        "source" to habit.source,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
        }
    }

    private suspend fun downloadCloudHabits(userId: String) {
        val docs = firestore.collection("users")
            .document(userId)
            .collection("habits")
            .get()
            .await()
            .documents

        docs.forEach { doc ->
            val cloudId = doc.id
            val title = doc.getString("title") ?: return@forEach
            val iconKey = doc.getString("iconKey") ?: "water"
            val colorKey = doc.getString("colorKey") ?: "mint"
            val frequencyType = doc.getString("frequencyType") ?: "DAILY"
            val customDaysCsv = doc.getString("customDaysCsv") ?: ""
            val reminderEnabled = doc.getBoolean("reminderEnabled") ?: true
            val reminderHour = (doc.getLong("reminderHour") ?: 20L).toInt()
            val reminderMinute = (doc.getLong("reminderMinute") ?: 0L).toInt()
            val startEpochDay = doc.getLong("startEpochDay") ?: LocalDate.now().toEpochDay()
            val activeUntilEpochDay = doc.getLong("activeUntilEpochDay")
            val isArchived = doc.getBoolean("isArchived") ?: false

            val existingCloud = habitDao.findByCloudId(cloudId)
            if (existingCloud != null) {
                habitDao.updateFromCloud(
                    id = existingCloud.id,
                    title = title,
                    iconKey = iconKey,
                    colorKey = colorKey,
                    frequencyType = frequencyType,
                    customDaysCsv = customDaysCsv,
                    reminderEnabled = reminderEnabled,
                    reminderHour = reminderHour,
                    reminderMinute = reminderMinute,
                    startEpochDay = startEpochDay,
                    activeUntilEpochDay = activeUntilEpochDay,
                    isArchived = isArchived
                )
                return@forEach
            }

            val existingByTitle = habitDao.findByTitle(title)
            if (existingByTitle != null && existingByTitle.cloudId.isBlank()) {
                habitDao.updateCloudId(existingByTitle.id, cloudId)
                habitDao.updateFromCloud(
                    id = existingByTitle.id,
                    title = title,
                    iconKey = iconKey,
                    colorKey = colorKey,
                    frequencyType = frequencyType,
                    customDaysCsv = customDaysCsv,
                    reminderEnabled = reminderEnabled,
                    reminderHour = reminderHour,
                    reminderMinute = reminderMinute,
                    startEpochDay = startEpochDay,
                    activeUntilEpochDay = activeUntilEpochDay,
                    isArchived = isArchived
                )
                return@forEach
            }

            habitDao.insertHabit(
                HabitEntity(
                    cloudId = cloudId,
                    title = title,
                    iconKey = iconKey,
                    colorKey = colorKey,
                    frequencyType = frequencyType,
                    customDaysCsv = customDaysCsv,
                    reminderEnabled = reminderEnabled,
                    reminderHour = reminderHour,
                    reminderMinute = reminderMinute,
                    createdAt = System.currentTimeMillis(),
                    startEpochDay = startEpochDay,
                    activeUntilEpochDay = activeUntilEpochDay,
                    isArchived = isArchived,
                    source = doc.getString("source") ?: "cloud"
                )
            )
        }
    }

    private suspend fun uploadLocalCompletions(userId: String) {
        val habits = habitDao.getAllHabits().associateBy { it.id }
        val completions = completionDao.getAllCompletions()

        completions.forEach { completion ->
            val habit = habits[completion.habitId] ?: return@forEach
            val cloudId = habit.cloudId.ifBlank {
                UUID.randomUUID().toString().also { generated ->
                    habitDao.updateCloudId(habit.id, generated)
                }
            }

            val docId = "${cloudId}_${completion.dateEpochDay}"
            firestore.collection("users")
                .document(userId)
                .collection("habit_completions")
                .document(docId)
                .set(
                    mapOf(
                        "cloudId" to cloudId,
                        "dateEpochDay" to completion.dateEpochDay,
                        "completedAtMillis" to completion.completedAtMillis
                    )
                )
                .await()
        }
    }

    private suspend fun downloadCloudCompletions(userId: String) {
        val docs = firestore.collection("users")
            .document(userId)
            .collection("habit_completions")
            .get()
            .await()
            .documents

        docs.forEach { doc ->
            val cloudId = doc.getString("cloudId") ?: return@forEach
            val dateEpochDay = doc.getLong("dateEpochDay") ?: return@forEach
            val completedAt = doc.getLong("completedAtMillis") ?: System.currentTimeMillis()
            val localHabit = habitDao.findByCloudId(cloudId) ?: return@forEach

            completionDao.upsertCompletion(
                HabitCompletionEntity(
                    habitId = localHabit.id,
                    dateEpochDay = dateEpochDay,
                    completedAtMillis = completedAt
                )
            )
        }
    }

    private suspend fun uploadLocalHiddenDays(userId: String) {
        val habits = habitDao.getAllHabits().associateBy { it.id }
        val hiddenDays = hiddenDayDao.getAllHiddenDays()

        hiddenDays.forEach { hidden ->
            val habit = habits[hidden.habitId] ?: return@forEach
            val cloudId = habit.cloudId.ifBlank {
                UUID.randomUUID().toString().also { generated ->
                    habitDao.updateCloudId(habit.id, generated)
                }
            }
            val docId = "${cloudId}_${hidden.dateEpochDay}"

            firestore.collection("users")
                .document(userId)
                .collection("habit_hidden_days")
                .document(docId)
                .set(
                    mapOf(
                        "cloudId" to cloudId,
                        "dateEpochDay" to hidden.dateEpochDay
                    )
                )
                .await()
        }
    }

    private suspend fun downloadCloudHiddenDays(userId: String) {
        val docs = firestore.collection("users")
            .document(userId)
            .collection("habit_hidden_days")
            .get()
            .await()
            .documents

        docs.forEach { doc ->
            val cloudId = doc.getString("cloudId") ?: return@forEach
            val dateEpochDay = doc.getLong("dateEpochDay") ?: return@forEach
            val localHabit = habitDao.findByCloudId(cloudId) ?: return@forEach

            hiddenDayDao.upsert(
                HiddenHabitDayEntity(
                    habitId = localHabit.id,
                    dateEpochDay = dateEpochDay
                )
            )
        }
    }

    private suspend fun deleteCollection(path: String) {
        while (true) {
            val batch = firestore.batch()
            val snapshot = firestore.collection(path)
                .limit(400)
                .get()
                .await()

            if (snapshot.isEmpty) return

            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
        }
    }
}

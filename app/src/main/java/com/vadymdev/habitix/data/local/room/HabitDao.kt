package com.vadymdev.habitix.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun observeAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun observeActiveHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits")
    suspend fun getAllHabits(): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE isArchived = 0")
    suspend fun getActiveHabits(): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE cloudId = :cloudId LIMIT 1")
    suspend fun findByCloudId(cloudId: String): HabitEntity?

    @Query("SELECT * FROM habits WHERE title = :title LIMIT 1")
    suspend fun findByTitle(title: String): HabitEntity?

    @Query("UPDATE habits SET cloudId = :cloudId WHERE id = :id")
    suspend fun updateCloudId(id: Long, cloudId: String)

    @Query("UPDATE habits SET activeUntilEpochDay = :activeUntilEpochDay WHERE id = :id")
    suspend fun updateActiveUntil(id: Long, activeUntilEpochDay: Long?)

    @Query(
        "UPDATE habits SET title = :title, iconKey = :iconKey, colorKey = :colorKey, frequencyType = :frequencyType, customDaysCsv = :customDaysCsv, reminderEnabled = :reminderEnabled, activeUntilEpochDay = NULL WHERE id = :id"
    )
    suspend fun updateHabit(
        id: Long,
        title: String,
        iconKey: String,
        colorKey: String,
        frequencyType: String,
        customDaysCsv: String,
        reminderEnabled: Boolean
    )

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabitById(id: Long)

    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()

    @Query(
        "UPDATE habits SET title = :title, iconKey = :iconKey, colorKey = :colorKey, frequencyType = :frequencyType, customDaysCsv = :customDaysCsv, reminderEnabled = :reminderEnabled, reminderHour = :reminderHour, reminderMinute = :reminderMinute, startEpochDay = :startEpochDay, activeUntilEpochDay = :activeUntilEpochDay, isArchived = :isArchived WHERE id = :id"
    )
    suspend fun updateFromCloud(
        id: Long,
        title: String,
        iconKey: String,
        colorKey: String,
        frequencyType: String,
        customDaysCsv: String,
        reminderEnabled: Boolean,
        reminderHour: Int,
        reminderMinute: Int,
        startEpochDay: Long,
        activeUntilEpochDay: Long?,
        isArchived: Boolean
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(entity: HabitEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHabits(entities: List<HabitEntity>)

    @Query("SELECT COUNT(*) FROM habits")
    suspend fun countAll(): Int

    @Query("SELECT COUNT(*) FROM habits WHERE title = :title AND source = 'onboarding'")
    suspend fun countOnboardingHabitByTitle(title: String): Int

    @Query("SELECT COUNT(*) FROM habit_completions WHERE habitId = :habitId")
    suspend fun getCompletionCountByHabit(habitId: Long): Int
}

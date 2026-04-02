package com.vadymdev.habitix.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCompletionDao {

    @Query("SELECT * FROM habit_completions")
    fun observeAllCompletions(): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions")
    suspend fun getAllCompletions(): List<HabitCompletionEntity>

    @Query("SELECT * FROM habit_completions WHERE dateEpochDay = :dateEpochDay")
    fun observeCompletionsForDate(dateEpochDay: Long): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE dateEpochDay = :dateEpochDay")
    suspend fun getCompletionsForDate(dateEpochDay: Long): List<HabitCompletionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCompletion(entity: HabitCompletionEntity)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND dateEpochDay = :dateEpochDay")
    suspend fun removeCompletion(habitId: Long, dateEpochDay: Long)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId")
    suspend fun removeByHabit(habitId: Long)

    @Query("DELETE FROM habit_completions")
    suspend fun removeAll()
}

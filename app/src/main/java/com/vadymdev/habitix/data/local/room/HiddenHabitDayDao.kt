package com.vadymdev.habitix.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HiddenHabitDayDao {

    @Query("SELECT * FROM hidden_habit_days")
    suspend fun getAllHiddenDays(): List<HiddenHabitDayEntity>

    @Query("SELECT * FROM hidden_habit_days WHERE dateEpochDay = :dateEpochDay")
    fun observeHiddenForDate(dateEpochDay: Long): Flow<List<HiddenHabitDayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: HiddenHabitDayEntity)

    @Query("DELETE FROM hidden_habit_days WHERE habitId = :habitId")
    suspend fun deleteByHabit(habitId: Long)

    @Query("DELETE FROM hidden_habit_days")
    suspend fun deleteAll()
}

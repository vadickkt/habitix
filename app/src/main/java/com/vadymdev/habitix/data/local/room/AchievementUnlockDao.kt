package com.vadymdev.habitix.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementUnlockDao {

    @Query("SELECT * FROM achievement_unlocks")
    fun observeAllUnlocks(): Flow<List<AchievementUnlockEntity>>

    @Query("SELECT * FROM achievement_unlocks")
    suspend fun getAllUnlocks(): List<AchievementUnlockEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(entity: AchievementUnlockEntity)

    @Query("DELETE FROM achievement_unlocks")
    suspend fun deleteAll()
}

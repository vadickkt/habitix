package com.vadymdev.habitix.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [HabitEntity::class, HabitCompletionEntity::class, HiddenHabitDayEntity::class, AchievementUnlockEntity::class],
    version = 6,
    exportSchema = false
)
abstract class HabitixDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun habitCompletionDao(): HabitCompletionDao
    abstract fun hiddenHabitDayDao(): HiddenHabitDayDao
    abstract fun achievementUnlockDao(): AchievementUnlockDao

    companion object {
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // No schema changes; explicit migration removes destructive fallback strategy.
            }
        }

        @Volatile
        private var instance: HabitixDatabase? = null

        fun get(context: Context): HabitixDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    HabitixDatabase::class.java,
                    "habitix.db"
                ).addMigrations(MIGRATION_5_6).build().also { instance = it }
            }
        }
    }
}

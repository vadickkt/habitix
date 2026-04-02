package com.vadymdev.habitix.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [HabitEntity::class, HabitCompletionEntity::class, HiddenHabitDayEntity::class],
    version = 3,
    exportSchema = false
)
abstract class HabitixDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun habitCompletionDao(): HabitCompletionDao
    abstract fun hiddenHabitDayDao(): HiddenHabitDayDao

    companion object {
        @Volatile
        private var instance: HabitixDatabase? = null

        fun get(context: Context): HabitixDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    HabitixDatabase::class.java,
                    "habitix.db"
                ).fallbackToDestructiveMigration(true).build().also { instance = it }
            }
        }
    }
}

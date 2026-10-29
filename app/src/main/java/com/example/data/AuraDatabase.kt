package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CategoryEntity::class,
        HabitEntity::class,
        CompletionHistoryEntity::class,
        JournalEntryEntity::class,
        UserSettingsEntity::class,
        AchievementEntity::class,
        StatisticsCacheEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun auraDao(): AuraDao

    companion object {
        @Volatile
        private var INSTANCE: AuraDatabase? = null

        fun getInstance(context: Context): AuraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AuraDatabase::class.java,
                    "aura_rituals_sqlite.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

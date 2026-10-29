package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AuraDao {

    // --- Categories ---
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, id ASC")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, id ASC")
    suspend fun getCategoriesSnapshot(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    // --- Habits ---
    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY priority DESC, createdAt ASC")
    fun observeActiveHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY createdAt ASC")
    suspend fun getAllHabitsSnapshot(): List<HabitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<HabitEntity>)

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabitById(habitId: Long)

    @Query("DELETE FROM completion_history WHERE habitId = :habitId")
    suspend fun deleteCompletionsForHabit(habitId: Long)

    // --- Completion History ---
    @Query("SELECT * FROM completion_history ORDER BY dateEpochDay DESC, completedAtTimestamp DESC")
    fun observeAllCompletions(): Flow<List<CompletionHistoryEntity>>

    @Query("SELECT * FROM completion_history ORDER BY dateEpochDay DESC")
    suspend fun getAllCompletionsSnapshot(): List<CompletionHistoryEntity>

    @Query("SELECT * FROM completion_history WHERE habitId = :habitId AND dateEpochDay = :dateEpochDay LIMIT 1")
    suspend fun getCompletionForHabitOnDate(habitId: Long, dateEpochDay: Long): CompletionHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCompletion(completion: CompletionHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletions(completions: List<CompletionHistoryEntity>)

    @Query("DELETE FROM completion_history WHERE habitId = :habitId AND dateEpochDay = :dateEpochDay")
    suspend fun deleteCompletionOnDate(habitId: Long, dateEpochDay: Long)

    // --- Journal Entries ---
    @Query("SELECT * FROM journal_entries ORDER BY dateEpochDay DESC, timestamp DESC")
    fun observeJournalEntries(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries ORDER BY dateEpochDay DESC, timestamp DESC")
    suspend fun getAllJournalEntriesSnapshot(): List<JournalEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntries(entries: List<JournalEntryEntity>)

    @Query("DELETE FROM journal_entries WHERE id = :entryId")
    suspend fun deleteJournalEntryById(entryId: Long)

    // --- User Settings ---
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun observeUserSettings(): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getUserSettingsSnapshot(): UserSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserSettings(settings: UserSettingsEntity)

    // --- Achievements ---
    @Query("SELECT * FROM achievements ORDER BY unlockedAtTimestamp DESC, code ASC")
    fun observeAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements ORDER BY code ASC")
    suspend fun getAchievementsSnapshot(): List<AchievementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAchievements(achievements: List<AchievementEntity>)

    // --- Statistics Cache ---
    @Query("SELECT * FROM statistics_cache WHERE id = 1 LIMIT 1")
    fun observeStatisticsCache(): Flow<StatisticsCacheEntity?>

    @Query("SELECT * FROM statistics_cache WHERE id = 1 LIMIT 1")
    suspend fun getStatisticsCacheSnapshot(): StatisticsCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStatisticsCache(cache: StatisticsCacheEntity)

    // --- Clear & Restore Transactions ---
    @Query("DELETE FROM habits")
    suspend fun clearHabits()

    @Query("DELETE FROM categories")
    suspend fun clearCategories()

    @Query("DELETE FROM completion_history")
    suspend fun clearCompletions()

    @Query("DELETE FROM journal_entries")
    suspend fun clearJournalEntries()

    @Query("DELETE FROM achievements")
    suspend fun clearAchievements()

    @Transaction
    suspend fun replaceAllDataTransaction(
        categories: List<CategoryEntity>,
        habits: List<HabitEntity>,
        completions: List<CompletionHistoryEntity>,
        journals: List<JournalEntryEntity>,
        settings: UserSettingsEntity,
        achievements: List<AchievementEntity>,
        statsCache: StatisticsCacheEntity
    ) {
        clearCompletions()
        clearJournalEntries()
        clearHabits()
        clearCategories()
        clearAchievements()
        insertCategories(categories)
        insertHabits(habits)
        insertCompletions(completions)
        insertJournalEntries(journals)
        upsertUserSettings(settings)
        upsertAchievements(achievements)
        upsertStatisticsCache(statsCache)
    }
}

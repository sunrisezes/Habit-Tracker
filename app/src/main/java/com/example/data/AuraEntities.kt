package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class FrequencyType(val label: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    CUSTOM("Custom Days")
}

enum class HabitDifficulty(val label: String, val weight: Int) {
    EASY("Gentle", 1),
    MEDIUM("Balanced", 2),
    HARD("Deep Challenge", 3)
}

enum class HabitPriority(val label: String, val sortRank: Int) {
    HIGH("Essential", 3),
    MEDIUM("Intentional", 2),
    LOW("Optional", 1)
}

enum class ThemeModePreference(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

enum class JournalMood(val label: String, val subtitle: String, val colorHex: String, val valence: Int) {
    CALM("Serene", "Centered & peaceful", "#1E7A5F", 4),
    FOCUSED("Focused", "Clear & intentional", "#3F4E8C", 5),
    ENERGIZED("Radiant", "High vitality & drive", "#C28429", 5),
    REFLECTIVE("Contemplative", "Thoughtful & inward", "#665687", 3),
    DRAINED("Low Tide", "Needing rest & grace", "#B55D4C", 2)
}

enum class AlarmChimeStyle(val label: String, val description: String) {
    SINGING_BOWL("Resonant Bowl", "Warm harmonic overtone"),
    MORNING_CHIME("Crystal Chime", "Soft ascending interval"),
    GENTLE_PULSE("Tactile Pulse", "Rhythmic cadence"),
    SILENT_ZEN("Silent Zen", "Visual & haptic only")
}

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconName: String,
    val colorHex: String,
    val sortOrder: Int = 0
)

@Entity(
    tableName = "habits",
    indices = [Index(value = ["categoryId"]), Index(value = ["isArchived"])]
)
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val iconName: String,
    val colorHex: String,
    val categoryId: Long,
    val frequencyType: String = FrequencyType.DAILY.name,
    /** Comma-separated ISO DayOfWeek integers 1 (Mon) .. 7 (Sun) */
    val customDaysCsv: String = "1,2,3,4,5,6,7",
    val targetCount: Int = 1,
    val reminderEnabled: Boolean = false,
    val reminderTime: String = "08:00",
    val startDateEpochDay: Long,
    val endDateEpochDay: Long? = null,
    val notes: String = "",
    val difficulty: String = HabitDifficulty.MEDIUM.name,
    val priority: String = HabitPriority.MEDIUM.name,
    val estimatedMinutes: Int = 15,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun parsedFrequency(): FrequencyType =
        runCatching { FrequencyType.valueOf(frequencyType) }.getOrDefault(FrequencyType.DAILY)

    fun parsedDifficulty(): HabitDifficulty =
        runCatching { HabitDifficulty.valueOf(difficulty) }.getOrDefault(HabitDifficulty.MEDIUM)

    fun parsedPriority(): HabitPriority =
        runCatching { HabitPriority.valueOf(priority) }.getOrDefault(HabitPriority.MEDIUM)

    fun activeDaysOfWeek(): Set<Int> {
        if (customDaysCsv.isBlank()) return (1..7).toSet()
        return customDaysCsv.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it in 1..7 }
            .toSet()
            .ifEmpty { (1..7).toSet() }
    }

    fun isScheduledForDate(epochDay: Long, isoDayOfWeek: Int, dayOfMonth: Int): Boolean {
        if (isArchived) return false
        if (epochDay < startDateEpochDay) return false
        if (endDateEpochDay != null && epochDay > endDateEpochDay) return false

        return when (parsedFrequency()) {
            FrequencyType.DAILY -> true
            FrequencyType.WEEKLY -> activeDaysOfWeek().contains(isoDayOfWeek)
            FrequencyType.CUSTOM -> activeDaysOfWeek().contains(isoDayOfWeek)
            FrequencyType.MONTHLY -> {
                val startDom = ((startDateEpochDay % 30).toInt().coerceIn(1, 28))
                dayOfMonth == startDom || dayOfMonth == 1
            }
        }
    }
}

@Entity(
    tableName = "completion_history",
    indices = [
        Index(value = ["habitId", "dateEpochDay"], unique = true),
        Index(value = ["dateEpochDay"])
    ]
)
data class CompletionHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val dateEpochDay: Long,
    val completedCount: Int,
    val targetCountSnapshot: Int,
    val completedAtTimestamp: Long = System.currentTimeMillis(),
    val durationMinutesSpent: Int = 0,
    val note: String = ""
) {
    val isFullyCompleted: Boolean
        get() = completedCount >= targetCountSnapshot.coerceAtLeast(1)
}

@Entity(
    tableName = "journal_entries",
    indices = [Index(value = ["dateEpochDay"])]
)
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val promptTitle: String,
    val content: String,
    val mood: String = JournalMood.CALM.name,
    val energyLevel: Int = 4, // 1..5
    val linkedHabitIdsCsv: String = "",
    val tagsCsv: String = ""
) {
    fun parsedMood(): JournalMood =
        runCatching { JournalMood.valueOf(mood) }.getOrDefault(JournalMood.CALM)

    fun linkedHabitIds(): List<Long> =
        linkedHabitIdsCsv.split(",").mapNotNull { it.trim().toLongOrNull() }

    fun tags(): List<String> =
        tagsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val userName: String = "",
    val userBio: String = "Cultivating stillness, clarity, and deliberate daily craft.",
    val themeMode: String = ThemeModePreference.SYSTEM.name,
    val notificationsEnabled: Boolean = true,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "07:00",
    val soundEffectsEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val lastBackupTimestamp: Long? = null
) {
    fun parsedThemeMode(): ThemeModePreference =
        runCatching { ThemeModePreference.valueOf(themeMode) }.getOrDefault(ThemeModePreference.SYSTEM)
}

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val code: String,
    val title: String,
    val description: String,
    val iconName: String,
    val unlockedAtTimestamp: Long? = null,
    val progressCurrent: Int = 0,
    val progressTarget: Int = 1
) {
    val isUnlocked: Boolean
        get() = unlockedAtTimestamp != null || progressCurrent >= progressTarget
}

@Entity(tableName = "statistics_cache")
data class StatisticsCacheEntity(
    @PrimaryKey val id: Int = 1,
    val overallSuccessRate: Float = 0f,
    val currentGlobalStreak: Int = 0,
    val longestGlobalStreak: Int = 0,
    val consistencyScore: Int = 0,
    val mostProductiveDayOfWeek: String = "Tuesday",
    val mostProductiveMonth: String = "October",
    val peakTimeBucket: String = "Morning (06:00 – 10:00)",
    val totalCompletions: Int = 0,
    val totalFocusMinutes: Int = 0,
    val updatedAtTimestamp: Long = System.currentTimeMillis()
)

/**
 * Rich UI Domain model combining a Habit with its Category, Today's Progress, and Streak metrics.
 */
data class HabitCardModel(
    val habit: HabitEntity,
    val category: CategoryEntity?,
    val completedCountToday: Int,
    val targetCountToday: Int,
    val isCompletedToday: Boolean,
    val currentStreak: Int,
    val longestStreak: Int,
    val completionRate30d: Float,
    val isScheduledToday: Boolean
)

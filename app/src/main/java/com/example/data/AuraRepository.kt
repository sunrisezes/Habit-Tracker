package com.example.data

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class AuraRepository(private val dao: AuraDao) {

    val categoriesFlow: Flow<List<CategoryEntity>> = dao.observeCategories()
    val activeHabitsFlow: Flow<List<HabitEntity>> = dao.observeActiveHabits()
    val allCompletionsFlow: Flow<List<CompletionHistoryEntity>> = dao.observeAllCompletions()
    val journalEntriesFlow: Flow<List<JournalEntryEntity>> = dao.observeJournalEntries()
    val userSettingsFlow: Flow<UserSettingsEntity?> = dao.observeUserSettings()
    val achievementsFlow: Flow<List<AchievementEntity>> = dao.observeAchievements()
    val statisticsCacheFlow: Flow<StatisticsCacheEntity?> = dao.observeStatisticsCache()

    /**
     * Ensures initial luxury seed data exists on first launch, and sanitizes any legacy placeholder name.
     */
    suspend fun ensureSeeded() {
        val existingCategories = dao.getCategoriesSnapshot()
        if (existingCategories.isNotEmpty()) {
            val currentSettings = dao.getUserSettingsSnapshot()
            if (currentSettings != null && currentSettings.userName.contains("Elena", ignoreCase = true)) {
                dao.upsertUserSettings(currentSettings.copy(userName = ""))
            }
            recomputeStatisticsAndAchievements()
            return
        }
        seedDefaultWorkspace()
    }

    suspend fun seedDefaultWorkspace() {
        val today = LocalDate.now().toEpochDay()

        val categories = listOf(
            CategoryEntity(id = 1L, name = "Mindfulness", iconName = "meditation", colorHex = "#3F4E8C", sortOrder = 1),
            CategoryEntity(id = 2L, name = "Vitality", iconName = "fitness", colorHex = "#1E7A5F", sortOrder = 2),
            CategoryEntity(id = 3L, name = "Deep Craft", iconName = "focus", colorHex = "#C28429", sortOrder = 3),
            CategoryEntity(id = 4L, name = "Wisdom", iconName = "book", colorHex = "#665687", sortOrder = 4),
            CategoryEntity(id = 5L, name = "Evening Wind-Down", iconName = "sleep", colorHex = "#2C6E7A", sortOrder = 5)
        )

        val habits = listOf(
            HabitEntity(
                id = 1L,
                title = "Morning Stillness & Breathwork",
                description = "15 minutes of unhurried box breathing before checking any screens.",
                iconName = "meditation",
                colorHex = "#3F4E8C",
                categoryId = 1L,
                frequencyType = FrequencyType.DAILY.name,
                customDaysCsv = "1,2,3,4,5,6,7",
                targetCount = 1,
                reminderEnabled = true,
                reminderTime = "07:15",
                startDateEpochDay = today - 45L,
                notes = "Sit by the east window; focus on 4-4-4-4 cadence.",
                difficulty = HabitDifficulty.EASY.name,
                priority = HabitPriority.HIGH.name,
                estimatedMinutes = 15
            ),
            HabitEntity(
                id = 2L,
                title = "Deep Architectural Flow",
                description = "90 minutes of uninterrupted deep craft with zero notifications.",
                iconName = "focus",
                colorHex = "#C28429",
                categoryId = 3L,
                frequencyType = FrequencyType.CUSTOM.name,
                customDaysCsv = "1,2,3,4,5",
                targetCount = 2,
                reminderEnabled = true,
                reminderTime = "09:00",
                startDateEpochDay = today - 45L,
                notes = "Two 45-minute focus blocks.",
                difficulty = HabitDifficulty.HARD.name,
                priority = HabitPriority.HIGH.name,
                estimatedMinutes = 45
            ),
            HabitEntity(
                id = 3L,
                title = "Cellular Hydration Ritual",
                description = "Four 500ml carafes of mineral water spaced throughout the day.",
                iconName = "water",
                colorHex = "#1E7A5F",
                categoryId = 2L,
                frequencyType = FrequencyType.DAILY.name,
                customDaysCsv = "1,2,3,4,5,6,7",
                targetCount = 4,
                reminderEnabled = false,
                reminderTime = "11:00",
                startDateEpochDay = today - 45L,
                notes = "Add a pinch of sea salt and lemon to the morning glass.",
                difficulty = HabitDifficulty.EASY.name,
                priority = HabitPriority.MEDIUM.name,
                estimatedMinutes = 5
            ),
            HabitEntity(
                id = 4L,
                title = "Zone 2 Kinetic Movement",
                description = "Outdoor brisk tempo run or mobility strength sculpting.",
                iconName = "run",
                colorHex = "#1E7A5F",
                categoryId = 2L,
                frequencyType = FrequencyType.DAILY.name,
                customDaysCsv = "1,2,3,4,5,6,7",
                targetCount = 1,
                reminderEnabled = true,
                reminderTime = "17:30",
                startDateEpochDay = today - 40L,
                notes = "Maintain nasal breathing throughout the session.",
                difficulty = HabitDifficulty.MEDIUM.name,
                priority = HabitPriority.HIGH.name,
                estimatedMinutes = 35
            ),
            HabitEntity(
                id = 5L,
                title = "Literature & Philosophy Reading",
                description = "Read 25 pages of non-fiction or timeless essays in physical print.",
                iconName = "book",
                colorHex = "#665687",
                categoryId = 4L,
                frequencyType = FrequencyType.DAILY.name,
                customDaysCsv = "1,2,3,4,5,6,7",
                targetCount = 1,
                reminderEnabled = true,
                reminderTime = "21:00",
                startDateEpochDay = today - 35L,
                notes = "Capture key passages in the reflection journal.",
                difficulty = HabitDifficulty.EASY.name,
                priority = HabitPriority.MEDIUM.name,
                estimatedMinutes = 25
            ),
            HabitEntity(
                id = 6L,
                title = "Digital Sunset & Light Dimming",
                description = "Dock all devices outside the bedroom 45 minutes before sleep.",
                iconName = "sleep",
                colorHex = "#2C6E7A",
                categoryId = 5L,
                frequencyType = FrequencyType.DAILY.name,
                customDaysCsv = "1,2,3,4,5,6,7",
                targetCount = 1,
                reminderEnabled = true,
                reminderTime = "22:00",
                startDateEpochDay = today - 30L,
                notes = "Warm herbal infusion and amber lighting only.",
                difficulty = HabitDifficulty.MEDIUM.name,
                priority = HabitPriority.MEDIUM.name,
                estimatedMinutes = 20
            )
        )

        // Generate 30 days of realistic completion history
        val completions = mutableListOf<CompletionHistoryEntity>()
        val nowMillis = System.currentTimeMillis()
        for (offset in 28 downTo 0) {
            val dayEpoch = today - offset
            val dateObj = LocalDate.ofEpochDay(dayEpoch)
            val dow = dateObj.dayOfWeek.value

            habits.forEach { habit ->
                val scheduled = habit.isScheduledForDate(dayEpoch, dow, dateObj.dayOfMonth)
                if (scheduled) {
                    val shouldComplete = when {
                        offset == 0 -> habit.id in listOf(1L, 3L) // Today partially completed
                        offset in 1..9 -> true // Strong active streak
                        (dayEpoch + habit.id) % 5L != 0L -> true
                        else -> false
                    }
                    if (shouldComplete) {
                        val count = if (offset == 0 && habit.id == 3L) 3 else habit.targetCount
                        val hourOfDay = when (habit.id) {
                            1L -> 7
                            2L -> 10
                            3L -> 13
                            4L -> 18
                            5L -> 21
                            else -> 22
                        }
                        val simulatedTimestamp = nowMillis - (offset * 86_400_000L) + (hourOfDay * 3_600_000L)
                        completions.add(
                            CompletionHistoryEntity(
                                habitId = habit.id,
                                dateEpochDay = dayEpoch,
                                completedCount = count,
                                targetCountSnapshot = habit.targetCount,
                                completedAtTimestamp = simulatedTimestamp,
                                durationMinutesSpent = habit.estimatedMinutes,
                                note = if (offset <= 2 && habit.id == 1L) "Deep sense of calm and presence." else ""
                            )
                        )
                    }
                }
            }
        }

        val journals = listOf(
            JournalEntryEntity(
                id = 1L,
                dateEpochDay = today,
                timestamp = nowMillis - 3_600_000L,
                promptTitle = "What ritual created the most clarity today?",
                content = "Beginning the morning with 15 minutes of breathwork before opening any messages completely transformed my attention span. My morning architectural session felt effortless and grounded.",
                mood = JournalMood.FOCUSED.name,
                energyLevel = 5,
                linkedHabitIdsCsv = "1,2",
                tagsCsv = "clarity,morning-ritual,deep-work"
            ),
            JournalEntryEntity(
                id = 2L,
                dateEpochDay = today - 1L,
                timestamp = nowMillis - 90_000_000L,
                promptTitle = "Where did your energy flow most naturally?",
                content = "Outdoor Zone 2 running as the sun set helped release cognitive tension from the afternoon. Reading philosophy afterward with warm tea made sleep deeply restorative.",
                mood = JournalMood.CALM.name,
                energyLevel = 4,
                linkedHabitIdsCsv = "4,5,6",
                tagsCsv = "vitality,recovery,reading"
            ),
            JournalEntryEntity(
                id = 3L,
                dateEpochDay = today - 3L,
                timestamp = nowMillis - 260_000_000L,
                promptTitle = "What small adjustment improved your consistency?",
                content = "Placing the water carafe and printed book directly on my desk the night before removed all friction from starting.",
                mood = JournalMood.ENERGIZED.name,
                energyLevel = 5,
                linkedHabitIdsCsv = "3,5",
                tagsCsv = "environment-design,momentum"
            )
        )

        val settings = UserSettingsEntity(
            id = 1,
            userName = "",
            userBio = "Cultivating stillness, clarity, and deliberate daily craft.",
            themeMode = ThemeModePreference.SYSTEM.name,
            notificationsEnabled = true,
            quietHoursStart = "22:00",
            quietHoursEnd = "07:00",
            soundEffectsEnabled = true,
            hapticsEnabled = true,
            lastBackupTimestamp = nowMillis
        )

        val defaultAchievements = buildDefaultAchievements()

        dao.replaceAllDataTransaction(
            categories = categories,
            habits = habits,
            completions = completions,
            journals = journals,
            settings = settings,
            achievements = defaultAchievements,
            statsCache = StatisticsCacheEntity()
        )
        recomputeStatisticsAndAchievements()
    }

    suspend fun toggleOrIncrementHabitOnDate(habit: HabitEntity, dateEpochDay: Long): Boolean {
        val existing = dao.getCompletionForHabitOnDate(habit.id, dateEpochDay)
        val target = habit.targetCount.coerceAtLeast(1)
        val newlyCompleted: Boolean

        if (existing == null) {
            dao.upsertCompletion(
                CompletionHistoryEntity(
                    habitId = habit.id,
                    dateEpochDay = dateEpochDay,
                    completedCount = 1,
                    targetCountSnapshot = target,
                    completedAtTimestamp = System.currentTimeMillis(),
                    durationMinutesSpent = habit.estimatedMinutes
                )
            )
            newlyCompleted = (1 >= target)
        } else if (existing.completedCount < target) {
            val nextCount = existing.completedCount + 1
            dao.upsertCompletion(
                existing.copy(
                    completedCount = nextCount,
                    targetCountSnapshot = target,
                    completedAtTimestamp = System.currentTimeMillis(),
                    durationMinutesSpent = existing.durationMinutesSpent + habit.estimatedMinutes
                )
            )
            newlyCompleted = (nextCount >= target)
        } else {
            // Reset if already completed
            dao.deleteCompletionOnDate(habit.id, dateEpochDay)
            newlyCompleted = false
        }

        recomputeStatisticsAndAchievements()
        return newlyCompleted
    }

    suspend fun recordFocusSessionCompletion(habit: HabitEntity, durationMinutes: Int, dateEpochDay: Long) {
        val existing = dao.getCompletionForHabitOnDate(habit.id, dateEpochDay)
        val target = habit.targetCount.coerceAtLeast(1)
        if (existing == null) {
            dao.upsertCompletion(
                CompletionHistoryEntity(
                    habitId = habit.id,
                    dateEpochDay = dateEpochDay,
                    completedCount = 1,
                    targetCountSnapshot = target,
                    completedAtTimestamp = System.currentTimeMillis(),
                    durationMinutesSpent = durationMinutes,
                    note = "Completed $durationMinutes min timed ritual"
                )
            )
        } else {
            val nextCount = (existing.completedCount + 1).coerceAtMost(target)
            dao.upsertCompletion(
                existing.copy(
                    completedCount = nextCount,
                    completedAtTimestamp = System.currentTimeMillis(),
                    durationMinutesSpent = existing.durationMinutesSpent + durationMinutes
                )
            )
        }
        recomputeStatisticsAndAchievements()
    }

    suspend fun resetHabitProgressOnDate(habitId: Long, dateEpochDay: Long) {
        dao.deleteCompletionOnDate(habitId, dateEpochDay)
        recomputeStatisticsAndAchievements()
    }

    suspend fun saveHabit(habit: HabitEntity): Long {
        val id = dao.insertHabit(habit)
        recomputeStatisticsAndAchievements()
        return id
    }

    suspend fun deleteHabit(habitId: Long) {
        dao.deleteCompletionsForHabit(habitId)
        dao.deleteHabitById(habitId)
        recomputeStatisticsAndAchievements()
    }

    suspend fun saveCategory(category: CategoryEntity): Long {
        return dao.insertCategory(category)
    }

    suspend fun saveJournalEntry(entry: JournalEntryEntity): Long {
        val id = dao.insertJournalEntry(entry)
        recomputeStatisticsAndAchievements()
        return id
    }

    suspend fun deleteJournalEntry(id: Long) {
        dao.deleteJournalEntryById(id)
        recomputeStatisticsAndAchievements()
    }

    suspend fun updateUserSettings(settings: UserSettingsEntity) {
        dao.upsertUserSettings(settings)
    }

    /**
     * Computes habit-level streaks, overall success rate, consistency score, most productive day/month,
     * and unlocks achievements in Room.
     */
    suspend fun recomputeStatisticsAndAchievements() {
        val habits = dao.getAllHabitsSnapshot().filter { !it.isArchived }
        val completions = dao.getAllCompletionsSnapshot()
        val journals = dao.getAllJournalEntriesSnapshot()
        val todayEpoch = LocalDate.now().toEpochDay()

        if (habits.isEmpty()) {
            dao.upsertStatisticsCache(StatisticsCacheEntity())
            return
        }

        val completionsByDay = completions.groupBy { it.dateEpochDay }

        // Evaluate last 30 days success rate & consistency
        var scheduledSlots30d = 0
        var completedSlots30d = 0
        var daysWithAtLeastOneCompletion30d = 0

        for (offset in 0 until 30) {
            val day = todayEpoch - offset
            val date = LocalDate.ofEpochDay(day)
            val dayCompletions = completionsByDay[day].orEmpty().associateBy { it.habitId }
            var dayHadCompletion = false

            habits.forEach { habit ->
                if (habit.isScheduledForDate(day, date.dayOfWeek.value, date.dayOfMonth)) {
                    scheduledSlots30d++
                    val comp = dayCompletions[habit.id]
                    if (comp != null && comp.isFullyCompleted) {
                        completedSlots30d++
                        dayHadCompletion = true
                    }
                }
            }
            if (dayHadCompletion) daysWithAtLeastOneCompletion30d++
        }

        val overallSuccessRate = if (scheduledSlots30d > 0) {
            ((completedSlots30d.toFloat() / scheduledSlots30d.toFloat()) * 100f).coerceIn(0f, 100f)
        } else 0f

        // Compute current and longest daily streak (days where >=1 ritual was completed)
        val completedDaysSet = completions
            .filter { it.isFullyCompleted }
            .map { it.dateEpochDay }
            .toSet()

        var currentStreak = 0
        var checkDay = if (completedDaysSet.contains(todayEpoch)) todayEpoch else todayEpoch - 1L
        while (completedDaysSet.contains(checkDay)) {
            currentStreak++
            checkDay--
        }

        val sortedDays = completedDaysSet.sorted()
        var longestStreak = 0
        var running = 0
        var prevDay: Long? = null
        for (d in sortedDays) {
            if (prevDay == null || d == prevDay + 1L) {
                running++
            } else {
                running = 1
            }
            if (running > longestStreak) longestStreak = running
            prevDay = d
        }

        // Consistency score blends 30d success rate (70%) and active days ratio (30%)
        val activeDaysRatio = (daysWithAtLeastOneCompletion30d / 30f) * 100f
        val consistencyScore = ((overallSuccessRate * 0.7f) + (activeDaysRatio * 0.3f))
            .roundToInt()
            .coerceIn(0, 100)

        // Most productive day of week
        val dowCounts = mutableMapOf<DayOfWeek, Int>()
        val monthCounts = mutableMapOf<String, Int>()
        var morningCount = 0
        var afternoonCount = 0
        var eveningCount = 0

        completions.filter { it.isFullyCompleted }.forEach { comp ->
            val date = LocalDate.ofEpochDay(comp.dateEpochDay)
            dowCounts[date.dayOfWeek] = (dowCounts[date.dayOfWeek] ?: 0) + 1
            val monthName = date.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
            monthCounts[monthName] = (monthCounts[monthName] ?: 0) + 1

            val hour = Instant.ofEpochMilli(comp.completedAtTimestamp)
                .atZone(ZoneId.systemDefault())
                .hour
            when (hour) {
                in 4..11 -> morningCount++
                in 12..17 -> afternoonCount++
                else -> eveningCount++
            }
        }

        val bestDayOfWeek = dowCounts.maxByOrNull { it.value }?.key
            ?.getDisplayName(TextStyle.FULL, Locale.ENGLISH) ?: "Tuesday"
        val bestMonth = monthCounts.maxByOrNull { it.value }?.key
            ?: LocalDate.now().month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        val peakBucket = when {
            morningCount >= afternoonCount && morningCount >= eveningCount -> "Morning (06:00 – 11:00)"
            afternoonCount >= morningCount && afternoonCount >= eveningCount -> "Afternoon (12:00 – 17:00)"
            else -> "Evening (18:00 – 22:00)"
        }

        val totalFullyCompleted = completions.count { it.isFullyCompleted }
        val totalFocusMins = completions.sumOf { it.durationMinutesSpent }

        dao.upsertStatisticsCache(
            StatisticsCacheEntity(
                id = 1,
                overallSuccessRate = overallSuccessRate,
                currentGlobalStreak = currentStreak,
                longestGlobalStreak = longestStreak.coerceAtLeast(currentStreak),
                consistencyScore = consistencyScore,
                mostProductiveDayOfWeek = bestDayOfWeek,
                mostProductiveMonth = bestMonth,
                peakTimeBucket = peakBucket,
                totalCompletions = totalFullyCompleted,
                totalFocusMinutes = totalFocusMins,
                updatedAtTimestamp = System.currentTimeMillis()
            )
        )

        // Update Achievements
        val now = System.currentTimeMillis()
        val updatedAchievements = listOf(
            AchievementEntity(
                code = "FIRST_SPARK",
                title = "First Intentional Spark",
                description = "Complete your very first daily ritual.",
                iconName = "spark",
                progressCurrent = totalFullyCompleted.coerceAtMost(1),
                progressTarget = 1,
                unlockedAtTimestamp = if (totalFullyCompleted >= 1) now else null
            ),
            AchievementEntity(
                code = "STREAK_7",
                title = "Seven-Day Rhythm",
                description = "Maintain a continuous 7-day ritual streak.",
                iconName = "energy",
                progressCurrent = longestStreak.coerceAtMost(7),
                progressTarget = 7,
                unlockedAtTimestamp = if (longestStreak >= 7) now else null
            ),
            AchievementEntity(
                code = "STREAK_21",
                title = "Architect of Habit",
                description = "Reach 21 days of unbroken ritual consistency.",
                iconName = "meditation",
                progressCurrent = longestStreak.coerceAtMost(21),
                progressTarget = 21,
                unlockedAtTimestamp = if (longestStreak >= 21) now else null
            ),
            AchievementEntity(
                code = "DEEP_FOCUS_300",
                title = "Deep Focus Master",
                description = "Accumulate 300 minutes of timed ritual focus.",
                iconName = "timer",
                progressCurrent = totalFocusMins.coerceAtMost(300),
                progressTarget = 300,
                unlockedAtTimestamp = if (totalFocusMins >= 300) now else null
            ),
            AchievementEntity(
                code = "MINDFUL_SCRIBE",
                title = "Mindful Scribe",
                description = "Author 3 guided reflections in your journal.",
                iconName = "journal",
                progressCurrent = journals.size.coerceAtMost(3),
                progressTarget = 3,
                unlockedAtTimestamp = if (journals.size >= 3) now else null
            ),
            AchievementEntity(
                code = "CENTURY_CLUB",
                title = "Centurion of Craft",
                description = "Log 100 total completed rituals across categories.",
                iconName = "heart",
                progressCurrent = totalFullyCompleted.coerceAtMost(100),
                progressTarget = 100,
                unlockedAtTimestamp = if (totalFullyCompleted >= 100) now else null
            )
        )
        dao.upsertAchievements(updatedAchievements)
    }

    private fun buildDefaultAchievements(): List<AchievementEntity> = listOf(
        AchievementEntity("FIRST_SPARK", "First Intentional Spark", "Complete your very first daily ritual.", "spark", null, 0, 1),
        AchievementEntity("STREAK_7", "Seven-Day Rhythm", "Maintain a continuous 7-day ritual streak.", "energy", null, 0, 7),
        AchievementEntity("STREAK_21", "Architect of Habit", "Reach 21 days of unbroken ritual consistency.", "meditation", null, 0, 21),
        AchievementEntity("DEEP_FOCUS_300", "Deep Focus Master", "Accumulate 300 minutes of timed ritual focus.", "timer", null, 0, 300),
        AchievementEntity("MINDFUL_SCRIBE", "Mindful Scribe", "Author 3 guided reflections in your journal.", "journal", null, 0, 3),
        AchievementEntity("CENTURY_CLUB", "Centurion of Craft", "Log 100 total completed rituals across categories.", "heart", null, 0, 100)
    )

    /**
     * Serializes the entire SQLite database into a formatted, portable JSON string for Export & Backup.
     */
    suspend fun exportBackupJson(): String {
        val root = JSONObject()
        root.put("schemaVersion", 1)
        root.put("exportedAtTimestamp", System.currentTimeMillis())

        val settings = dao.getUserSettingsSnapshot() ?: UserSettingsEntity()
        root.put("settings", JSONObject().apply {
            put("userName", settings.userName)
            put("userBio", settings.userBio)
            put("themeMode", settings.themeMode)
            put("notificationsEnabled", settings.notificationsEnabled)
            put("quietHoursStart", settings.quietHoursStart)
            put("quietHoursEnd", settings.quietHoursEnd)
            put("soundEffectsEnabled", settings.soundEffectsEnabled)
            put("hapticsEnabled", settings.hapticsEnabled)
        })

        val categoriesArray = JSONArray()
        dao.getCategoriesSnapshot().forEach { cat ->
            categoriesArray.put(JSONObject().apply {
                put("id", cat.id)
                put("name", cat.name)
                put("iconName", cat.iconName)
                put("colorHex", cat.colorHex)
                put("sortOrder", cat.sortOrder)
            })
        }
        root.put("categories", categoriesArray)

        val habitsArray = JSONArray()
        dao.getAllHabitsSnapshot().forEach { h ->
            habitsArray.put(JSONObject().apply {
                put("id", h.id)
                put("title", h.title)
                put("description", h.description)
                put("iconName", h.iconName)
                put("colorHex", h.colorHex)
                put("categoryId", h.categoryId)
                put("frequencyType", h.frequencyType)
                put("customDaysCsv", h.customDaysCsv)
                put("targetCount", h.targetCount)
                put("reminderEnabled", h.reminderEnabled)
                put("reminderTime", h.reminderTime)
                put("startDateEpochDay", h.startDateEpochDay)
                if (h.endDateEpochDay != null) put("endDateEpochDay", h.endDateEpochDay)
                put("notes", h.notes)
                put("difficulty", h.difficulty)
                put("priority", h.priority)
                put("estimatedMinutes", h.estimatedMinutes)
                put("isArchived", h.isArchived)
                put("createdAt", h.createdAt)
            })
        }
        root.put("habits", habitsArray)

        val completionsArray = JSONArray()
        dao.getAllCompletionsSnapshot().forEach { c ->
            completionsArray.put(JSONObject().apply {
                put("id", c.id)
                put("habitId", c.habitId)
                put("dateEpochDay", c.dateEpochDay)
                put("completedCount", c.completedCount)
                put("targetCountSnapshot", c.targetCountSnapshot)
                put("completedAtTimestamp", c.completedAtTimestamp)
                put("durationMinutesSpent", c.durationMinutesSpent)
                put("note", c.note)
            })
        }
        root.put("completions", completionsArray)

        val journalsArray = JSONArray()
        dao.getAllJournalEntriesSnapshot().forEach { j ->
            journalsArray.put(JSONObject().apply {
                put("id", j.id)
                put("dateEpochDay", j.dateEpochDay)
                put("timestamp", j.timestamp)
                put("promptTitle", j.promptTitle)
                put("content", j.content)
                put("mood", j.mood)
                put("energyLevel", j.energyLevel)
                put("linkedHabitIdsCsv", j.linkedHabitIdsCsv)
                put("tagsCsv", j.tagsCsv)
            })
        }
        root.put("journals", journalsArray)

        dao.upsertUserSettings(settings.copy(lastBackupTimestamp = System.currentTimeMillis()))
        return root.toString(2)
    }

    /**
     * Restores the SQLite database atomically from a portable JSON snapshot.
     */
    suspend fun importBackupJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)

            val settingsObj = root.optJSONObject("settings")
            val settings = if (settingsObj != null) {
                UserSettingsEntity(
                    id = 1,
                    userName = settingsObj.optString("userName", ""),
                    userBio = settingsObj.optString("userBio", "Cultivating stillness, clarity, and deliberate daily craft."),
                    themeMode = settingsObj.optString("themeMode", ThemeModePreference.SYSTEM.name),
                    notificationsEnabled = settingsObj.optBoolean("notificationsEnabled", true),
                    quietHoursStart = settingsObj.optString("quietHoursStart", "22:00"),
                    quietHoursEnd = settingsObj.optString("quietHoursEnd", "07:00"),
                    soundEffectsEnabled = settingsObj.optBoolean("soundEffectsEnabled", true),
                    hapticsEnabled = settingsObj.optBoolean("hapticsEnabled", true),
                    lastBackupTimestamp = System.currentTimeMillis()
                )
            } else {
                UserSettingsEntity(lastBackupTimestamp = System.currentTimeMillis())
            }

            val categories = mutableListOf<CategoryEntity>()
            val catArr = root.optJSONArray("categories") ?: JSONArray()
            for (i in 0 until catArr.length()) {
                val obj = catArr.getJSONObject(i)
                categories.add(
                    CategoryEntity(
                        id = obj.getLong("id"),
                        name = obj.getString("name"),
                        iconName = obj.optString("iconName", "spark"),
                        colorHex = obj.optString("colorHex", "#3F4E8C"),
                        sortOrder = obj.optInt("sortOrder", i)
                    )
                )
            }

            val habits = mutableListOf<HabitEntity>()
            val habArr = root.optJSONArray("habits") ?: JSONArray()
            for (i in 0 until habArr.length()) {
                val obj = habArr.getJSONObject(i)
                habits.add(
                    HabitEntity(
                        id = obj.getLong("id"),
                        title = obj.getString("title"),
                        description = obj.optString("description", ""),
                        iconName = obj.optString("iconName", "meditation"),
                        colorHex = obj.optString("colorHex", "#3F4E8C"),
                        categoryId = obj.optLong("categoryId", 1L),
                        frequencyType = obj.optString("frequencyType", FrequencyType.DAILY.name),
                        customDaysCsv = obj.optString("customDaysCsv", "1,2,3,4,5,6,7"),
                        targetCount = obj.optInt("targetCount", 1),
                        reminderEnabled = obj.optBoolean("reminderEnabled", false),
                        reminderTime = obj.optString("reminderTime", "08:00"),
                        startDateEpochDay = obj.optLong("startDateEpochDay", LocalDate.now().toEpochDay()),
                        endDateEpochDay = if (obj.has("endDateEpochDay") && !obj.isNull("endDateEpochDay")) obj.getLong("endDateEpochDay") else null,
                        notes = obj.optString("notes", ""),
                        difficulty = obj.optString("difficulty", HabitDifficulty.MEDIUM.name),
                        priority = obj.optString("priority", HabitPriority.MEDIUM.name),
                        estimatedMinutes = obj.optInt("estimatedMinutes", 15),
                        isArchived = obj.optBoolean("isArchived", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val completions = mutableListOf<CompletionHistoryEntity>()
            val compArr = root.optJSONArray("completions") ?: JSONArray()
            for (i in 0 until compArr.length()) {
                val obj = compArr.getJSONObject(i)
                completions.add(
                    CompletionHistoryEntity(
                        id = obj.optLong("id", 0L),
                        habitId = obj.getLong("habitId"),
                        dateEpochDay = obj.getLong("dateEpochDay"),
                        completedCount = obj.optInt("completedCount", 1),
                        targetCountSnapshot = obj.optInt("targetCountSnapshot", 1),
                        completedAtTimestamp = obj.optLong("completedAtTimestamp", System.currentTimeMillis()),
                        durationMinutesSpent = obj.optInt("durationMinutesSpent", 15),
                        note = obj.optString("note", "")
                    )
                )
            }

            val journals = mutableListOf<JournalEntryEntity>()
            val jArr = root.optJSONArray("journals") ?: JSONArray()
            for (i in 0 until jArr.length()) {
                val obj = jArr.getJSONObject(i)
                journals.add(
                    JournalEntryEntity(
                        id = obj.optLong("id", 0L),
                        dateEpochDay = obj.getLong("dateEpochDay"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        promptTitle = obj.optString("promptTitle", "Daily Reflection"),
                        content = obj.getString("content"),
                        mood = obj.optString("mood", JournalMood.CALM.name),
                        energyLevel = obj.optInt("energyLevel", 4),
                        linkedHabitIdsCsv = obj.optString("linkedHabitIdsCsv", ""),
                        tagsCsv = obj.optString("tagsCsv", "")
                    )
                )
            }

            dao.replaceAllDataTransaction(
                categories = categories,
                habits = habits,
                completions = completions,
                journals = journals,
                settings = settings,
                achievements = buildDefaultAchievements(),
                statsCache = StatisticsCacheEntity()
            )
            recomputeStatisticsAndAchievements()
            true
        } catch (_: Exception) {
            false
        }
    }
}

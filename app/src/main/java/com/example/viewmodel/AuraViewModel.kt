package com.example.viewmodel

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.provider.AlarmClock
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.AchievementEntity
import com.example.data.AlarmChimeStyle
import com.example.data.AuraRepository
import com.example.data.CategoryEntity
import com.example.data.CompletionHistoryEntity
import com.example.data.HabitCardModel
import com.example.data.HabitEntity
import com.example.data.JournalEntryEntity
import com.example.data.StatisticsCacheEntity
import com.example.data.ThemeModePreference
import com.example.data.UserSettingsEntity
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuraDestination(val route: String, val label: String) {
    DASHBOARD("dashboard", "Dashboard"),
    CALENDAR("calendar", "Calendar"),
    STATISTICS("statistics", "Statistics"),
    JOURNAL("journal", "Journal"),
    SETTINGS("settings", "Settings")
}

enum class TimeStudioTab(val label: String) {
    TIMER("Timer"),
    STOPWATCH("Stopwatch"),
    ALARM_REMINDER("Alarm & Reminder")
}

enum class FocusTimerMode(val label: String, val defaultMinutes: Int) {
    RITUAL_DURATION("Ritual Duration", 15),
    POMODORO_25("Pomodoro 25m", 25),
    DEEP_WORK_45("Deep Work 45m", 45),
    BREATH_5("Mindful 5m", 5),
    CUSTOM("Custom", 20)
}

data class StopwatchLap(
    val lapNumber: Int,
    val splitSeconds: Int,
    val totalSeconds: Int
) {
    val formattedSplit: String
        get() = String.format(Locale.US, "%02d:%02d", splitSeconds / 60, splitSeconds % 60)

    val formattedTotal: String
        get() = String.format(Locale.US, "%02d:%02d", totalSeconds / 60, totalSeconds % 60)
}

data class FocusSessionState(
    val habit: HabitEntity,
    val activeTab: TimeStudioTab = TimeStudioTab.TIMER,
    val mode: FocusTimerMode = FocusTimerMode.RITUAL_DURATION,
    val totalSeconds: Int,
    val remainingSeconds: Int,
    val isRunning: Boolean = true,
    val isMinimized: Boolean = false,
    val isFinished: Boolean = false,
    // Stopwatch state
    val stopwatchElapsedSeconds: Int = 0,
    val isStopwatchRunning: Boolean = false,
    val stopwatchLaps: List<StopwatchLap> = emptyList(),
    // Alarm & Reminder studio state
    val alarmEnabled: Boolean = true,
    val alarmTime: String = "08:00",
    val alarmChimeStyle: AlarmChimeStyle = AlarmChimeStyle.SINGING_BOWL,
    val snoozeMinutes: Int = 10,
    val isAlarmRingingPreview: Boolean = false
) {
    val progress: Float
        get() = if (totalSeconds > 0) {
            ((totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val formattedRemaining: String
        get() {
            val mins = remainingSeconds / 60
            val secs = remainingSeconds % 60
            return String.format(Locale.US, "%02d:%02d", mins, secs)
        }

    val formattedStopwatch: String
        get() {
            val hrs = stopwatchElapsedSeconds / 3600
            val mins = (stopwatchElapsedSeconds % 3600) / 60
            val secs = stopwatchElapsedSeconds % 60
            return if (hrs > 0) {
                String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
            } else {
                String.format(Locale.US, "%02d:%02d", mins, secs)
            }
        }
}

data class DayProgressPoint(
    val dateEpochDay: Long,
    val dayShortLabel: String,
    val dayOfMonth: Int,
    val completedCount: Int,
    val scheduledCount: Int,
    val completionRatio: Float,
    val isToday: Boolean
)

data class CategoryBreakdownItem(
    val category: CategoryEntity,
    val habitCount: Int,
    val completions30d: Int,
    val completionRate: Float
)

data class TimeOfDayBucket(
    val label: String,
    val timeRange: String,
    val completionsCount: Int,
    val shareRatio: Float
)

data class QuoteItem(
    val text: String,
    val author: String
)

data class AuraUiState(
    val isLoading: Boolean = true,
    val greeting: String = "Good Morning",
    val formattedCurrentDate: String = "",
    val quote: QuoteItem = QuoteItem(
        "We are what we repeatedly do. Excellence, then, is not an act, but a habit.",
        "Aristotle"
    ),
    val todayEpochDay: Long = LocalDate.now().toEpochDay(),
    val completionPercentageToday: Int = 0,
    val completedCountToday: Int = 0,
    val scheduledCountToday: Int = 0,
    val weeklyProgress: List<DayProgressPoint> = emptyList(),
    val monthlyStreakDays: Int = 0,
    val categories: List<CategoryEntity> = emptyList(),
    val selectedCategoryId: Long? = null,
    val showAllHabitsOnDashboard: Boolean = false,
    val todayHabitCards: List<HabitCardModel> = emptyList(),
    val allHabitCards: List<HabitCardModel> = emptyList(),
    val selectedCalendarDateEpoch: Long = LocalDate.now().toEpochDay(),
    val calendarMonthOffset: Int = 0,
    val heatmapByEpochDay: Map<Long, Float> = emptyMap(),
    val completedHabitsOnSelectedDate: List<Pair<HabitEntity, CompletionHistoryEntity>> = emptyList(),
    val missedHabitsOnSelectedDate: List<HabitEntity> = emptyList(),
    val journalsOnSelectedDate: List<JournalEntryEntity> = emptyList(),
    val journalEntries: List<JournalEntryEntity> = emptyList(),
    val statistics: StatisticsCacheEntity = StatisticsCacheEntity(),
    val trendPoints30d: List<DayProgressPoint> = emptyList(),
    val categoryBreakdowns: List<CategoryBreakdownItem> = emptyList(),
    val timeOfDayAnalysis: List<TimeOfDayBucket> = emptyList(),
    val achievements: List<AchievementEntity> = emptyList(),
    val settings: UserSettingsEntity = UserSettingsEntity(),
    val statusBannerMessage: String? = null
)

object AuraNotificationHelper {
    private const val CHANNEL_ID = "aura_ritual_reminders"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Aura Ritual Reminders & Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Gentle reminders, countdown timers, and ritual alarms."
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    fun playChimeTone(style: AlarmChimeStyle) {
        if (style == AlarmChimeStyle.SILENT_ZEN) return
        try {
            val toneType = when (style) {
                AlarmChimeStyle.SINGING_BOWL -> ToneGenerator.TONE_PROP_ACK
                AlarmChimeStyle.MORNING_CHIME -> ToneGenerator.TONE_PROP_PROMPT
                AlarmChimeStyle.GENTLE_PULSE -> ToneGenerator.TONE_PROP_BEEP2
                AlarmChimeStyle.SILENT_ZEN -> return
            }
            val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
            toneGen.startTone(toneType, 350)
        } catch (_: Exception) {
            // Ignore in headless/test environments without audio hardware
        }
    }

    fun sendNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = 1001
    ): Boolean {
        ensureChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return false
        }
        return try {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.notify(notificationId, builder.build())
            true
        } catch (_: Exception) {
            false
        }
    }

    fun scheduleSystemAlarmClock(context: Context, title: String, timeHhMm: String): Boolean {
        return try {
            val parts = timeHhMm.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 8
            val minute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_MESSAGE, "Aura Ritual: $title")
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }
}

private data class LocalViewFilters(
    val selectedCategoryId: Long? = null,
    val showAllHabitsOnDashboard: Boolean = false,
    val selectedCalendarDateEpoch: Long = LocalDate.now().toEpochDay(),
    val calendarMonthOffset: Int = 0,
    val statusBannerMessage: String? = null
)

class AuraViewModel(
    private val repository: AuraRepository,
    private val appContext: Context
) : ViewModel() {

    private val _currentDestination = MutableStateFlow(AuraDestination.DASHBOARD)
    val currentDestination: StateFlow<AuraDestination> = _currentDestination.asStateFlow()

    private val _filters = MutableStateFlow(LocalViewFilters())

    // Modal states
    private val _isAddEditSheetOpen = MutableStateFlow(false)
    val isAddEditSheetOpen: StateFlow<Boolean> = _isAddEditSheetOpen.asStateFlow()

    private val _editingHabit = MutableStateFlow<HabitEntity?>(null)
    val editingHabit: StateFlow<HabitEntity?> = _editingHabit.asStateFlow()

    private val _contextMenuHabit = MutableStateFlow<HabitCardModel?>(null)
    val contextMenuHabit: StateFlow<HabitCardModel?> = _contextMenuHabit.asStateFlow()

    private val _isJournalComposerOpen = MutableStateFlow(false)
    val isJournalComposerOpen: StateFlow<Boolean> = _isJournalComposerOpen.asStateFlow()

    private val _preselectedJournalHabitId = MutableStateFlow<Long?>(null)
    val preselectedJournalHabitId: StateFlow<Long?> = _preselectedJournalHabitId.asStateFlow()

    // Time Studio (Timer, Stopwatch, Alarm & Reminder) State
    private val _focusSession = MutableStateFlow<FocusSessionState?>(null)
    val focusSession: StateFlow<FocusSessionState?> = _focusSession.asStateFlow()
    private var timerJob: Job? = null
    private var stopwatchJob: Job? = null

    private val curatedQuotes = listOf(
        QuoteItem("How we spend our days is, of course, how we spend our lives.", "Annie Dillard"),
        QuoteItem("Simplicity is about subtracting the obvious and adding the meaningful.", "John Maeda"),
        QuoteItem("Realize deeply that the present moment is all you ever have.", "Eckhart Tolle"),
        QuoteItem("Small disciplines repeated with consistency every day lead to great achievements.", "John C. Maxwell"),
        QuoteItem("Stillness is where clarity, creativity, and deliberate action are born.", "Seneca")
    )

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    private val rawDataFlow = combine(
        repository.categoriesFlow,
        repository.activeHabitsFlow,
        repository.allCompletionsFlow,
        repository.journalEntriesFlow,
        repository.userSettingsFlow
    ) { categories, habits, completions, journals, settings ->
        RawRepositoryBundle(categories, habits, completions, journals, settings ?: UserSettingsEntity())
    }

    val uiState: StateFlow<AuraUiState> = combine(
        rawDataFlow,
        repository.achievementsFlow,
        repository.statisticsCacheFlow,
        _filters
    ) { bundle, achievements, statsCache, filters ->
        buildUiState(
            bundle = bundle,
            achievements = achievements,
            statsCache = statsCache ?: StatisticsCacheEntity(),
            filters = filters
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AuraUiState()
    )

    private fun buildUiState(
        bundle: RawRepositoryBundle,
        achievements: List<AchievementEntity>,
        statsCache: StatisticsCacheEntity,
        filters: LocalViewFilters
    ): AuraUiState {
        val today = LocalDate.now()
        val todayEpoch = today.toEpochDay()
        val hour = LocalTime.now().hour
        val greetingPrefix = when (hour) {
            in 4..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Quiet Night"
        }
        val cleanName = bundle.settings.userName.trim()
        val fullGreeting = if (cleanName.isNotEmpty()) {
            "$greetingPrefix, ${cleanName.substringBefore(" ")}"
        } else {
            greetingPrefix
        }
        val formattedDate = today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH))
        val quote = curatedQuotes[(todayEpoch.toInt().coerceAtLeast(0)) % curatedQuotes.size]

        val categoryMap = bundle.categories.associateBy { it.id }
        val completionsByDay = bundle.completions.groupBy { it.dateEpochDay }
        val completionsByHabit = bundle.completions.groupBy { it.habitId }
        val todayCompletionsMap = completionsByDay[todayEpoch].orEmpty().associateBy { it.habitId }

        val allCards = bundle.habits.map { habit ->
            val todayComp = todayCompletionsMap[habit.id]
            val completedToday = todayComp?.completedCount ?: 0
            val targetToday = habit.targetCount.coerceAtLeast(1)
            val habitHistory = completionsByHabit[habit.id].orEmpty()
                .filter { it.isFullyCompleted }
                .map { it.dateEpochDay }
                .toSet()

            var hStreak = 0
            var cDay = if (habitHistory.contains(todayEpoch)) todayEpoch else todayEpoch - 1L
            while (habitHistory.contains(cDay)) {
                hStreak++
                cDay--
            }

            var hLongest = 0
            var run = 0
            var prev: Long? = null
            habitHistory.sorted().forEach { d ->
                run = if (prev == null || d == prev!! + 1L) run + 1 else 1
                if (run > hLongest) hLongest = run
                prev = d
            }

            var sched30 = 0
            var comp30 = 0
            for (i in 0 until 30) {
                val d = todayEpoch - i
                val ld = LocalDate.ofEpochDay(d)
                if (habit.isScheduledForDate(d, ld.dayOfWeek.value, ld.dayOfMonth)) {
                    sched30++
                    if (habitHistory.contains(d)) comp30++
                }
            }
            val rate30 = if (sched30 > 0) (comp30.toFloat() / sched30.toFloat()) else 0f

            HabitCardModel(
                habit = habit,
                category = categoryMap[habit.categoryId],
                completedCountToday = completedToday,
                targetCountToday = targetToday,
                isCompletedToday = completedToday >= targetToday,
                currentStreak = hStreak,
                longestStreak = hLongest.coerceAtLeast(hStreak),
                completionRate30d = rate30,
                isScheduledToday = habit.isScheduledForDate(todayEpoch, today.dayOfWeek.value, today.dayOfMonth)
            )
        }

        val scheduledCardsToday = allCards.filter { it.isScheduledToday }
        val completedScheduledCount = scheduledCardsToday.count { it.isCompletedToday }
        val scheduledTotalCount = scheduledCardsToday.size
        val completionPctToday = if (scheduledTotalCount > 0) {
            ((completedScheduledCount.toFloat() / scheduledTotalCount.toFloat()) * 100f).roundToInt()
        } else 0

        val filteredDashboardCards = allCards.filter { card ->
            val matchesSchedule = filters.showAllHabitsOnDashboard || card.isScheduledToday
            val matchesCategory = filters.selectedCategoryId == null || card.habit.categoryId == filters.selectedCategoryId
            matchesSchedule && matchesCategory
        }

        val trend30 = (29 downTo 0).map { offset ->
            val dEpoch = todayEpoch - offset
            val dDate = LocalDate.ofEpochDay(dEpoch)
            val dayComps = completionsByDay[dEpoch].orEmpty().associateBy { it.habitId }
            var sched = 0
            var comp = 0
            bundle.habits.forEach { h ->
                if (h.isScheduledForDate(dEpoch, dDate.dayOfWeek.value, dDate.dayOfMonth)) {
                    sched++
                    if (dayComps[h.id]?.isFullyCompleted == true) comp++
                }
            }
            val ratio = if (sched > 0) (comp.toFloat() / sched.toFloat()).coerceIn(0f, 1f) else 0f
            DayProgressPoint(
                dateEpochDay = dEpoch,
                dayShortLabel = dDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).take(3),
                dayOfMonth = dDate.dayOfMonth,
                completedCount = comp,
                scheduledCount = sched,
                completionRatio = ratio,
                isToday = dEpoch == todayEpoch
            )
        }
        val weekly7 = trend30.takeLast(7)

        val heatmap = mutableMapOf<Long, Float>()
        for (offset in 0..120) {
            val dEpoch = todayEpoch - offset
            val dDate = LocalDate.ofEpochDay(dEpoch)
            val dayComps = completionsByDay[dEpoch].orEmpty().associateBy { it.habitId }
            var sched = 0
            var comp = 0
            bundle.habits.forEach { h ->
                if (h.isScheduledForDate(dEpoch, dDate.dayOfWeek.value, dDate.dayOfMonth)) {
                    sched++
                    if (dayComps[h.id]?.isFullyCompleted == true) comp++
                }
            }
            if (sched > 0) {
                heatmap[dEpoch] = (comp.toFloat() / sched.toFloat()).coerceIn(0f, 1f)
            }
        }

        val currentMonthStart = today.withDayOfMonth(1).toEpochDay()
        val monthlyActiveDays = (currentMonthStart..todayEpoch).count { d ->
            completionsByDay[d].orEmpty().any { it.isFullyCompleted }
        }

        val selEpoch = filters.selectedCalendarDateEpoch
        val selDate = LocalDate.ofEpochDay(selEpoch)
        val selDayComps = completionsByDay[selEpoch].orEmpty().associateBy { it.habitId }
        val completedOnSel = mutableListOf<Pair<HabitEntity, CompletionHistoryEntity>>()
        val missedOnSel = mutableListOf<HabitEntity>()

        bundle.habits.forEach { habit ->
            val comp = selDayComps[habit.id]
            if (comp != null && comp.isFullyCompleted) {
                completedOnSel.add(habit to comp)
            } else if (habit.isScheduledForDate(selEpoch, selDate.dayOfWeek.value, selDate.dayOfMonth) && selEpoch <= todayEpoch) {
                missedOnSel.add(habit)
            }
        }

        val journalsOnSel = bundle.journals.filter { it.dateEpochDay == selEpoch }

        val catBreakdowns = bundle.categories.map { cat ->
            val catCards = allCards.filter { it.habit.categoryId == cat.id }
            val avgRate = if (catCards.isNotEmpty()) {
                catCards.map { it.completionRate30d }.average().toFloat()
            } else 0f
            val catHabitIds = catCards.map { it.habit.id }.toSet()
            val catComps30d = bundle.completions.count {
                it.habitId in catHabitIds && it.dateEpochDay >= (todayEpoch - 30) && it.isFullyCompleted
            }
            CategoryBreakdownItem(
                category = cat,
                habitCount = catCards.size,
                completions30d = catComps30d,
                completionRate = avgRate
            )
        }

        var morningC = 0
        var afternoonC = 0
        var eveningC = 0
        var nightC = 0
        bundle.completions.filter { it.isFullyCompleted }.forEach { comp ->
            val hr = Instant.ofEpochMilli(comp.completedAtTimestamp)
                .atZone(ZoneId.systemDefault())
                .hour
            when (hr) {
                in 5..11 -> morningC++
                in 12..16 -> afternoonC++
                in 17..21 -> eveningC++
                else -> nightC++
            }
        }
        val totalBucketC = (morningC + afternoonC + eveningC + nightC).coerceAtLeast(1)
        val timeBuckets = listOf(
            TimeOfDayBucket("Morning Rituals", "05:00 – 11:59", morningC, morningC.toFloat() / totalBucketC),
            TimeOfDayBucket("Afternoon Flow", "12:00 – 16:59", afternoonC, afternoonC.toFloat() / totalBucketC),
            TimeOfDayBucket("Evening Synthesis", "17:00 – 21:59", eveningC, eveningC.toFloat() / totalBucketC),
            TimeOfDayBucket("Night Stillness", "22:00 – 04:59", nightC, nightC.toFloat() / totalBucketC)
        )

        return AuraUiState(
            isLoading = false,
            greeting = fullGreeting,
            formattedCurrentDate = formattedDate,
            quote = quote,
            todayEpochDay = todayEpoch,
            completionPercentageToday = completionPctToday,
            completedCountToday = completedScheduledCount,
            scheduledCountToday = scheduledTotalCount,
            weeklyProgress = weekly7,
            monthlyStreakDays = monthlyActiveDays,
            categories = bundle.categories,
            selectedCategoryId = filters.selectedCategoryId,
            showAllHabitsOnDashboard = filters.showAllHabitsOnDashboard,
            todayHabitCards = filteredDashboardCards,
            allHabitCards = allCards,
            selectedCalendarDateEpoch = selEpoch,
            calendarMonthOffset = filters.calendarMonthOffset,
            heatmapByEpochDay = heatmap,
            completedHabitsOnSelectedDate = completedOnSel,
            missedHabitsOnSelectedDate = missedOnSel,
            journalsOnSelectedDate = journalsOnSel,
            journalEntries = bundle.journals,
            statistics = statsCache,
            trendPoints30d = trend30,
            categoryBreakdowns = catBreakdowns,
            timeOfDayAnalysis = timeBuckets,
            achievements = achievements,
            settings = bundle.settings,
            statusBannerMessage = filters.statusBannerMessage
        )
    }

    // --- Navigation & Filters ---
    fun navigateTo(destination: AuraDestination) {
        _currentDestination.value = destination
    }

    fun selectCategoryFilter(categoryId: Long?) {
        _filters.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun toggleShowAllHabitsOnDashboard() {
        _filters.update { it.copy(showAllHabitsOnDashboard = !it.showAllHabitsOnDashboard) }
    }

    fun selectCalendarDate(epochDay: Long) {
        _filters.update { it.copy(selectedCalendarDateEpoch = epochDay) }
    }

    fun changeCalendarMonthOffset(delta: Int) {
        _filters.update { it.copy(calendarMonthOffset = (it.calendarMonthOffset + delta).coerceIn(-24, 12)) }
    }

    fun clearStatusBanner() {
        _filters.update { it.copy(statusBannerMessage = null) }
    }

    private fun postBanner(message: String) {
        _filters.update { it.copy(statusBannerMessage = message) }
    }

    // --- Habit Completion & CRUD ---
    fun toggleHabitCompletionToday(habit: HabitEntity) {
        toggleHabitCompletionOnDate(habit, LocalDate.now().toEpochDay())
    }

    fun toggleHabitCompletionOnDate(habit: HabitEntity, dateEpochDay: Long) {
        viewModelScope.launch {
            repository.toggleOrIncrementHabitOnDate(habit, dateEpochDay)
        }
    }

    fun resetHabitToday(habitId: Long) {
        viewModelScope.launch {
            repository.resetHabitProgressOnDate(habitId, LocalDate.now().toEpochDay())
            postBanner("Reset today's progress for ritual.")
        }
    }

    fun openAddHabitSheet() {
        _editingHabit.value = null
        _isAddEditSheetOpen.value = true
    }

    fun openEditHabitSheet(habit: HabitEntity) {
        _contextMenuHabit.value = null
        _editingHabit.value = habit
        _isAddEditSheetOpen.value = true
    }

    fun closeAddEditHabitSheet() {
        _isAddEditSheetOpen.value = false
        _editingHabit.value = null
    }

    fun saveHabit(habit: HabitEntity, onSaved: () -> Unit = {}) {
        _isAddEditSheetOpen.value = false
        _editingHabit.value = null
        viewModelScope.launch {
            repository.saveHabit(habit)
            if (habit.reminderEnabled && uiState.value.settings.notificationsEnabled) {
                AuraNotificationHelper.sendNotification(
                    context = appContext,
                    title = "Ritual Scheduled: ${habit.title}",
                    message = "Reminder set for ${habit.reminderTime} (${habit.estimatedMinutes} min)."
                )
            }
            postBanner("Saved ritual \"${habit.title}\".")
            onSaved()
        }
    }

    fun deleteHabit(habitId: Long) {
        _contextMenuHabit.value = null
        viewModelScope.launch {
            repository.deleteHabit(habitId)
            postBanner("Ritual removed.")
        }
    }

    fun addCustomCategory(name: String, iconName: String, colorHex: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val nextOrder = uiState.value.categories.size + 1
            val newId = repository.saveCategory(
                CategoryEntity(
                    name = name.trim(),
                    iconName = iconName,
                    colorHex = colorHex,
                    sortOrder = nextOrder
                )
            )
            _filters.update { it.copy(selectedCategoryId = newId) }
            postBanner("Category \"${name.trim()}\" created.")
        }
    }

    // --- Context Menu ---
    fun openHabitContextMenu(card: HabitCardModel) {
        _contextMenuHabit.value = card
    }

    fun closeHabitContextMenu() {
        _contextMenuHabit.value = null
    }

    // --- Journal Actions ---
    fun openJournalComposer(linkedHabitId: Long? = null) {
        _contextMenuHabit.value = null
        _preselectedJournalHabitId.value = linkedHabitId
        _isJournalComposerOpen.value = true
    }

    fun closeJournalComposer() {
        _isJournalComposerOpen.value = false
        _preselectedJournalHabitId.value = null
    }

    fun saveJournalEntry(
        promptTitle: String,
        content: String,
        moodName: String,
        energyLevel: Int,
        linkedHabitIds: List<Long>,
        tags: String
    ) {
        if (content.isBlank()) return
        _isJournalComposerOpen.value = false
        _preselectedJournalHabitId.value = null
        viewModelScope.launch {
            repository.saveJournalEntry(
                JournalEntryEntity(
                    dateEpochDay = LocalDate.now().toEpochDay(),
                    timestamp = System.currentTimeMillis(),
                    promptTitle = promptTitle.ifBlank { "Daily Reflection" },
                    content = content.trim(),
                    mood = moodName,
                    energyLevel = energyLevel.coerceIn(1, 5),
                    linkedHabitIdsCsv = linkedHabitIds.joinToString(","),
                    tagsCsv = tags.trim()
                )
            )
            postBanner("Reflection saved to your journal.")
        }
    }

    fun deleteJournalEntry(id: Long) {
        viewModelScope.launch {
            repository.deleteJournalEntry(id)
            postBanner("Reflection deleted.")
        }
    }

    // --- Built-in Time Studio: Countdown Timer, Stopwatch & Alarm/Reminder Engine ---
    fun openTimeStudio(initialTab: TimeStudioTab = TimeStudioTab.TIMER) {
        val defaultHabit = uiState.value.todayHabitCards.firstOrNull()?.habit
            ?: uiState.value.allHabitCards.firstOrNull()?.habit
            ?: HabitEntity(
                id = 1L,
                title = "Mindful Focus Session",
                description = "Dedicated deep focus or stopwatch ritual",
                iconName = "timer",
                colorHex = "#3F4E8C",
                categoryId = 1L,
                startDateEpochDay = LocalDate.now().toEpochDay(),
                estimatedMinutes = 25
            )
        startFocusSession(
            habit = defaultHabit,
            mode = FocusTimerMode.RITUAL_DURATION,
            initialTab = initialTab,
            autoStartTimer = (initialTab == TimeStudioTab.TIMER)
        )
    }

    fun startFocusSession(
        habit: HabitEntity,
        mode: FocusTimerMode = FocusTimerMode.RITUAL_DURATION,
        initialTab: TimeStudioTab = TimeStudioTab.TIMER,
        autoStartTimer: Boolean = true
    ) {
        _contextMenuHabit.value = null
        val durationMins = if (mode == FocusTimerMode.RITUAL_DURATION) {
            habit.estimatedMinutes.coerceAtLeast(1)
        } else {
            mode.defaultMinutes
        }
        val totalSecs = durationMins * 60
        _focusSession.value = FocusSessionState(
            habit = habit,
            activeTab = initialTab,
            mode = mode,
            totalSeconds = totalSecs,
            remainingSeconds = totalSecs,
            isRunning = autoStartTimer,
            isMinimized = false,
            isFinished = false,
            alarmEnabled = habit.reminderEnabled,
            alarmTime = habit.reminderTime.ifBlank { "08:00" }
        )
        if (autoStartTimer) {
            runTimerLoop()
        } else {
            timerJob?.cancel()
        }
    }

    fun selectFocusHabit(habit: HabitEntity) {
        val current = _focusSession.value ?: return
        val mins = if (current.mode == FocusTimerMode.RITUAL_DURATION) {
            habit.estimatedMinutes.coerceAtLeast(1)
        } else {
            (current.totalSeconds / 60).coerceAtLeast(1)
        }
        val secs = mins * 60
        _focusSession.value = current.copy(
            habit = habit,
            totalSeconds = secs,
            remainingSeconds = secs,
            alarmEnabled = habit.reminderEnabled,
            alarmTime = habit.reminderTime.ifBlank { "08:00" }
        )
    }

    fun selectTimeStudioTab(tab: TimeStudioTab) {
        _focusSession.update { it?.copy(activeTab = tab) }
    }

    fun selectFocusTimerMode(mode: FocusTimerMode) {
        val current = _focusSession.value ?: return
        val mins = if (mode == FocusTimerMode.RITUAL_DURATION) {
            current.habit.estimatedMinutes.coerceAtLeast(1)
        } else {
            mode.defaultMinutes
        }
        val secs = mins * 60
        _focusSession.value = current.copy(
            mode = mode,
            totalSeconds = secs,
            remainingSeconds = secs,
            isRunning = true,
            isFinished = false
        )
        runTimerLoop()
    }

    fun adjustTimerDurationMinutes(deltaMinutes: Int) {
        val current = _focusSession.value ?: return
        val newTotalSecs = (current.totalSeconds + (deltaMinutes * 60)).coerceIn(60, 10_800)
        val newRemainingSecs = (current.remainingSeconds + (deltaMinutes * 60)).coerceIn(60, newTotalSecs)
        _focusSession.value = current.copy(
            mode = FocusTimerMode.CUSTOM,
            totalSeconds = newTotalSecs,
            remainingSeconds = newRemainingSecs,
            isFinished = false
        )
    }

    fun resetFocusTimer() {
        val current = _focusSession.value ?: return
        timerJob?.cancel()
        _focusSession.value = current.copy(
            remainingSeconds = current.totalSeconds,
            isRunning = false,
            isFinished = false
        )
    }

    fun toggleFocusTimerPause() {
        val current = _focusSession.value ?: return
        val nextRunning = !current.isRunning
        _focusSession.value = current.copy(isRunning = nextRunning)
        if (nextRunning) {
            runTimerLoop()
        } else {
            timerJob?.cancel()
        }
    }

    // --- Stopwatch Controls ---
    fun toggleStopwatchRunning() {
        val current = _focusSession.value ?: return
        val nextRunning = !current.isStopwatchRunning
        _focusSession.value = current.copy(isStopwatchRunning = nextRunning)
        if (nextRunning) {
            runStopwatchLoop()
        } else {
            stopwatchJob?.cancel()
        }
    }

    fun recordStopwatchLap() {
        val current = _focusSession.value ?: return
        if (current.stopwatchElapsedSeconds <= 0) return
        val prevTotal = current.stopwatchLaps.firstOrNull()?.totalSeconds ?: 0
        val split = (current.stopwatchElapsedSeconds - prevTotal).coerceAtLeast(1)
        val nextLap = StopwatchLap(
            lapNumber = current.stopwatchLaps.size + 1,
            splitSeconds = split,
            totalSeconds = current.stopwatchElapsedSeconds
        )
        _focusSession.value = current.copy(
            stopwatchLaps = listOf(nextLap) + current.stopwatchLaps
        )
    }

    fun resetStopwatch() {
        val current = _focusSession.value ?: return
        stopwatchJob?.cancel()
        _focusSession.value = current.copy(
            stopwatchElapsedSeconds = 0,
            isStopwatchRunning = false,
            stopwatchLaps = emptyList()
        )
    }

    fun completeStopwatchSessionNow() {
        val current = _focusSession.value ?: return
        stopwatchJob?.cancel()
        val elapsedMins = (current.stopwatchElapsedSeconds / 60).coerceAtLeast(1)
        viewModelScope.launch {
            repository.recordFocusSessionCompletion(
                habit = current.habit,
                durationMinutes = elapsedMins,
                dateEpochDay = LocalDate.now().toEpochDay()
            )
            postBanner("Logged $elapsedMins min stopwatch session for \"${current.habit.title}\".")
            _focusSession.value = current.copy(
                isStopwatchRunning = false,
                stopwatchElapsedSeconds = 0,
                stopwatchLaps = emptyList()
            )
        }
    }

    // --- Alarm & Reminder Controls ---
    fun updateStudioAlarmConfig(
        enabled: Boolean? = null,
        timeHhMm: String? = null,
        chimeStyle: AlarmChimeStyle? = null,
        snoozeMins: Int? = null
    ) {
        val current = _focusSession.value ?: return
        _focusSession.value = current.copy(
            alarmEnabled = enabled ?: current.alarmEnabled,
            alarmTime = timeHhMm ?: current.alarmTime,
            alarmChimeStyle = chimeStyle ?: current.alarmChimeStyle,
            snoozeMinutes = snoozeMins ?: current.snoozeMinutes
        )
    }

    fun stepAlarmTimeMinutes(deltaMinutes: Int) {
        val current = _focusSession.value ?: return
        val parts = current.alarmTime.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 8
        val m = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
        val totalMins = ((h * 60 + m + deltaMinutes) % 1440 + 1440) % 1440
        val newTime = String.format(Locale.US, "%02d:%02d", totalMins / 60, totalMins % 60)
        _focusSession.value = current.copy(alarmTime = newTime)
    }

    fun saveStudioAlarmToHabit(setSystemAlarmClock: Boolean = false) {
        val current = _focusSession.value ?: return
        val updatedHabit = current.habit.copy(
            reminderEnabled = current.alarmEnabled,
            reminderTime = current.alarmTime
        )
        viewModelScope.launch {
            repository.saveHabit(updatedHabit)
            _focusSession.update { it?.copy(habit = updatedHabit) }
            if (current.alarmEnabled) {
                AuraNotificationHelper.playChimeTone(current.alarmChimeStyle)
                AuraNotificationHelper.sendNotification(
                    context = appContext,
                    title = "Alarm & Reminder Active: ${updatedHabit.title}",
                    message = "Scheduled daily at ${current.alarmTime} (${current.alarmChimeStyle.label} · ${current.snoozeMinutes}m snooze)."
                )
                if (setSystemAlarmClock) {
                    AuraNotificationHelper.scheduleSystemAlarmClock(
                        context = appContext,
                        title = updatedHabit.title,
                        timeHhMm = current.alarmTime
                    )
                }
                postBanner("Alarm set for ${current.alarmTime} (${current.alarmChimeStyle.label}).")
            } else {
                postBanner("Reminder disabled for \"${updatedHabit.title}\".")
            }
        }
    }

    fun triggerLiveAlarmPreview() {
        val current = _focusSession.value ?: return
        AuraNotificationHelper.playChimeTone(current.alarmChimeStyle)
        AuraNotificationHelper.sendNotification(
            context = appContext,
            title = "Ritual Alarm — ${current.habit.title}",
            message = "Scheduled ${current.alarmTime} · Chime: ${current.alarmChimeStyle.label}"
        )
        _focusSession.value = current.copy(isAlarmRingingPreview = true)
        postBanner("Ringing ${current.alarmChimeStyle.label} alarm for \"${current.habit.title}\".")
    }

    fun snoozeActiveAlarm() {
        val current = _focusSession.value ?: return
        stepAlarmTimeMinutes(current.snoozeMinutes)
        val snoozedTime = _focusSession.value?.alarmTime ?: current.alarmTime
        _focusSession.update { it?.copy(isAlarmRingingPreview = false) }
        postBanner("Alarm snoozed for ${current.snoozeMinutes}m (until $snoozedTime).")
    }

    fun dismissActiveAlarmPreview() {
        _focusSession.update { it?.copy(isAlarmRingingPreview = false) }
    }

    fun setFocusSheetMinimized(minimized: Boolean) {
        _focusSession.update { it?.copy(isMinimized = minimized) }
    }

    fun completeFocusSessionNow() {
        val current = _focusSession.value ?: return
        timerJob?.cancel()
        stopwatchJob?.cancel()
        val elapsedMins = ((current.totalSeconds - current.remainingSeconds) / 60)
            .coerceAtLeast(current.habit.estimatedMinutes.coerceAtLeast(1))
        viewModelScope.launch {
            repository.recordFocusSessionCompletion(
                habit = current.habit,
                durationMinutes = elapsedMins,
                dateEpochDay = LocalDate.now().toEpochDay()
            )
            if (uiState.value.settings.notificationsEnabled) {
                AuraNotificationHelper.playChimeTone(current.alarmChimeStyle)
                AuraNotificationHelper.sendNotification(
                    context = appContext,
                    title = "Focus Ritual Complete",
                    message = "${current.habit.title} logged ($elapsedMins min)."
                )
            }
            _focusSession.value = null
            postBanner("Completed focus session for \"${current.habit.title}\".")
        }
    }

    fun dismissFocusSession() {
        timerJob?.cancel()
        stopwatchJob?.cancel()
        _focusSession.value = null
    }

    private fun runTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000L)
                val s = _focusSession.value ?: break
                if (!s.isRunning) break
                if (s.remainingSeconds > 1) {
                    _focusSession.value = s.copy(remainingSeconds = s.remainingSeconds - 1)
                } else {
                    val mins = (s.totalSeconds / 60).coerceAtLeast(1)
                    repository.recordFocusSessionCompletion(
                        habit = s.habit,
                        durationMinutes = mins,
                        dateEpochDay = LocalDate.now().toEpochDay()
                    )
                    if (uiState.value.settings.notificationsEnabled) {
                        AuraNotificationHelper.playChimeTone(s.alarmChimeStyle)
                        AuraNotificationHelper.sendNotification(
                            context = appContext,
                            title = "Ritual Complete — ${s.habit.title}",
                            message = "Your $mins-minute focus ritual has been recorded."
                        )
                    }
                    _focusSession.value = s.copy(
                        remainingSeconds = 0,
                        isRunning = false,
                        isMinimized = false,
                        isFinished = true
                    )
                    break
                }
            }
        }
    }

    private fun runStopwatchLoop() {
        stopwatchJob?.cancel()
        stopwatchJob = viewModelScope.launch {
            while (true) {
                delay(1_000L)
                val s = _focusSession.value ?: break
                if (!s.isStopwatchRunning) break
                _focusSession.value = s.copy(stopwatchElapsedSeconds = s.stopwatchElapsedSeconds + 1)
            }
        }
    }

    // --- Settings, Theme, Notifications & Backup/Restore ---
    fun updateThemeMode(mode: ThemeModePreference) {
        val current = uiState.value.settings
        viewModelScope.launch {
            repository.updateUserSettings(current.copy(themeMode = mode.name))
        }
    }

    fun updateProfile(name: String, bio: String) {
        val current = uiState.value.settings
        viewModelScope.launch {
            repository.updateUserSettings(
                current.copy(
                    userName = name.trim(),
                    userBio = bio.trim()
                )
            )
            postBanner("Profile saved.")
        }
    }

    fun updateNotificationSettings(
        enabled: Boolean,
        quietStart: String,
        quietEnd: String,
        haptics: Boolean
    ) {
        val current = uiState.value.settings
        viewModelScope.launch {
            repository.updateUserSettings(
                current.copy(
                    notificationsEnabled = enabled,
                    quietHoursStart = quietStart,
                    quietHoursEnd = quietEnd,
                    hapticsEnabled = haptics
                )
            )
        }
    }

    fun triggerTestReminderNotification() {
        AuraNotificationHelper.playChimeTone(AlarmChimeStyle.SINGING_BOWL)
        val sent = AuraNotificationHelper.sendNotification(
            context = appContext,
            title = "Aura Daily Ritual Reminder",
            message = "Time for Morning Stillness & Breathwork (15 min)."
        )
        if (sent) {
            postBanner("Reminder notification & chime dispatched.")
        } else {
            postBanner("Reminder chime played (grant notification permission for system alerts).")
        }
    }

    fun exportBackupToJson(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportBackupJson()
            onReady(json)
            postBanner("JSON backup snapshot generated.")
        }
    }

    fun importBackupFromJson(jsonString: String) {
        viewModelScope.launch {
            val success = repository.importBackupJson(jsonString)
            if (success) {
                postBanner("Database restored from JSON backup.")
            } else {
                postBanner("Could not parse backup file. Ensure valid Aura JSON format.")
            }
        }
    }

    fun saveInstantLocalBackup() {
        viewModelScope.launch {
            try {
                val json = repository.exportBackupJson()
                val file = File(appContext.filesDir, "aura_local_backup_snapshot.json")
                file.writeText(json)
                postBanner("Local snapshot backed up to device vault.")
            } catch (_: Exception) {
                postBanner("Failed to write local backup snapshot.")
            }
        }
    }

    fun restoreInstantLocalBackup() {
        viewModelScope.launch {
            try {
                val file = File(appContext.filesDir, "aura_local_backup_snapshot.json")
                if (!file.exists()) {
                    postBanner("No local snapshot found yet. Tap 'Backup Now' first.")
                    return@launch
                }
                val json = file.readText()
                val ok = repository.importBackupJson(json)
                if (ok) {
                    postBanner("Restored rituals and history from local vault snapshot.")
                } else {
                    postBanner("Snapshot file was corrupted.")
                }
            } catch (_: Exception) {
                postBanner("Could not restore local snapshot.")
            }
        }
    }

    fun resetToDefaultCuratedData() {
        viewModelScope.launch {
            repository.seedDefaultWorkspace()
            postBanner("Workspace reset to curated luxury rituals.")
        }
    }
}

private data class RawRepositoryBundle(
    val categories: List<CategoryEntity>,
    val habits: List<HabitEntity>,
    val completions: List<CompletionHistoryEntity>,
    val journals: List<JournalEntryEntity>,
    val settings: UserSettingsEntity
)

class AuraViewModelFactory(
    private val repository: AuraRepository,
    private val appContext: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuraViewModel::class.java)) {
            return AuraViewModel(repository, appContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

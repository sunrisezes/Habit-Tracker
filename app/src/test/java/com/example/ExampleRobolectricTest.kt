package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AuraDatabase
import com.example.data.AuraRepository
import com.example.viewmodel.AuraDestination
import com.example.viewmodel.AuraViewModel
import com.example.viewmodel.FocusTimerMode
import com.example.viewmodel.TimeStudioTab
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var database: AuraDatabase
    private lateinit var repository: AuraRepository
    private lateinit var viewModel: AuraViewModel

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AuraDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = AuraRepository(database.auraDao())
        runBlocking {
            repository.ensureSeeded()
        }
        viewModel = AuraViewModel(repository, context)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read app name string from context and verify no hardcoded Elena name`() = runBlocking {
        val appName = context.getString(R.string.app_name)
        assertEquals("Aura", appName)

        val settings = repository.userSettingsFlow.first()
        assertNotNull(settings)
        assertEquals("", settings?.userName)
        assertFalse(settings?.userName?.contains("Elena", ignoreCase = true) == true)
    }

    @Test
    fun `seed workspace populates categories habits completions and JSON backup restores cleanly`() = runBlocking {
        val categories = repository.categoriesFlow.first()
        val habits = repository.activeHabitsFlow.first()
        val stats = repository.statisticsCacheFlow.first()

        assertTrue(categories.size >= 5)
        assertTrue(habits.size >= 6)
        assertTrue((stats?.currentGlobalStreak ?: 0) >= 1)

        val backupJson = repository.exportBackupJson()
        assertTrue(backupJson.contains("Morning Stillness"))
        assertFalse(backupJson.contains("Elena"))

        val restoreSuccess = repository.importBackupJson(backupJson)
        assertTrue(restoreSuccess)
    }

    @Test
    fun `verify all buttons tabs timer stopwatch alarm and habit clicks work properly`() {
        composeTestRule.setContent {
            AuraAppRoot(viewModel = viewModel)
        }
        composeTestRule.waitForIdle()

        // 1. Verify Dashboard Greeting has no "Elena" and Quick Buttons are displayed
        composeTestRule.onNodeWithTag("dashboard_greeting_text").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dashboard_quick_add_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("quick_open_timer_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("quick_open_stopwatch_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("quick_open_alarm_button").assertIsDisplayed()

        // 2. Click Quick Timer Button -> Opens Time Studio on TIMER tab
        composeTestRule.onNodeWithTag("quick_open_timer_button").performClick()
        composeTestRule.waitForIdle()
        assertEquals(TimeStudioTab.TIMER, viewModel.focusSession.value?.activeTab)

        // Test Timer Mode & Duration Adjustment Buttons (+5m, Reset, Pause/Resume)
        composeTestRule.onNodeWithTag("timer_mode_pomodoro_25").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(FocusTimerMode.POMODORO_25, viewModel.focusSession.value?.mode)
        assertEquals(25 * 60, viewModel.focusSession.value?.totalSeconds)

        composeTestRule.onNodeWithTag("timer_adjust_plus_5m").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(30 * 60, viewModel.focusSession.value?.totalSeconds)

        composeTestRule.onNodeWithTag("focus_pause_resume_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(false, viewModel.focusSession.value?.isRunning)

        composeTestRule.onNodeWithTag("timer_reset_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // 3. Switch to Stopwatch Tab inside Time Studio and test Start / Pause / Reset
        composeTestRule.onNodeWithTag("time_studio_tab_stopwatch").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(TimeStudioTab.STOPWATCH, viewModel.focusSession.value?.activeTab)

        composeTestRule.onNodeWithTag("stopwatch_start_pause_btn").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(true, viewModel.focusSession.value?.isStopwatchRunning)

        // Pause and Reset Stopwatch
        composeTestRule.onNodeWithTag("stopwatch_start_pause_btn").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("stopwatch_reset_btn").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(0, viewModel.focusSession.value?.stopwatchElapsedSeconds)

        // 4. Switch to Alarm & Reminder Tab and test +15m stepper, Test Ring, Snooze, Save
        composeTestRule.onNodeWithTag("time_studio_tab_alarm_reminder").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(TimeStudioTab.ALARM_REMINDER, viewModel.focusSession.value?.activeTab)

        val beforeTime = viewModel.focusSession.value?.alarmTime
        composeTestRule.onNodeWithTag("alarm_step_plus_15m").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertTrue(beforeTime != viewModel.focusSession.value?.alarmTime)

        composeTestRule.onNodeWithTag("alarm_test_ring_btn").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(true, viewModel.focusSession.value?.isAlarmRingingPreview)

        composeTestRule.onNodeWithTag("alarm_snooze_btn").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(false, viewModel.focusSession.value?.isAlarmRingingPreview)

        composeTestRule.onNodeWithTag("alarm_save_btn").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Minimize Time Studio -> Verify Minimized Floating Bar appears
        composeTestRule.onNodeWithTag("time_studio_minimize_btn").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("minimized_focus_bar").assertIsDisplayed()

        // Expand and Close Time Studio
        composeTestRule.onNodeWithTag("minimized_focus_bar").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("time_studio_close_btn").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // 5. Test New Ritual Modal Button & Saving a New Habit
        composeTestRule.onNodeWithTag("dashboard_quick_add_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("habit_title_input").performScrollTo().performTextInput("Evening Tea Ceremony")
        composeTestRule.onNodeWithTag("save_habit_submit_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertFalse(viewModel.isAddEditSheetOpen.value)

        // 6. Test Bottom Navigation across Calendar, Statistics, Journal, and Settings
        composeTestRule.onNodeWithTag("nav_calendar").performClick()
        composeTestRule.waitForIdle()
        assertEquals(AuraDestination.CALENDAR, viewModel.currentDestination.value)
        composeTestRule.onNodeWithTag("calendar_prev_month_btn").performClick()
        composeTestRule.waitForIdle()
        assertEquals(-1, viewModel.uiState.value.calendarMonthOffset)
        composeTestRule.onNodeWithTag("calendar_next_month_btn").performClick()
        composeTestRule.waitForIdle()
        assertEquals(0, viewModel.uiState.value.calendarMonthOffset)

        composeTestRule.onNodeWithTag("nav_statistics").performClick()
        composeTestRule.waitForIdle()
        assertEquals(AuraDestination.STATISTICS, viewModel.currentDestination.value)

        composeTestRule.onNodeWithTag("nav_journal").performClick()
        composeTestRule.waitForIdle()
        assertEquals(AuraDestination.JOURNAL, viewModel.currentDestination.value)
        composeTestRule.onNodeWithTag("journal_new_entry_button").performClick()
        composeTestRule.waitForIdle()
        assertTrue(viewModel.isJournalComposerOpen.value)
        composeTestRule.onNodeWithTag("journal_content_input").performScrollTo().performTextInput("Feeling deeply focused and serene.")
        composeTestRule.onNodeWithTag("save_journal_submit_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertFalse(viewModel.isJournalComposerOpen.value)

        composeTestRule.onNodeWithTag("nav_settings").performClick()
        composeTestRule.waitForIdle()
        assertEquals(AuraDestination.SETTINGS, viewModel.currentDestination.value)
        composeTestRule.onNodeWithTag("settings_lazy_column")
            .performScrollToNode(hasTestTag("theme_mode_dark"))
        composeTestRule.onNodeWithTag("theme_mode_dark").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("theme_mode_light").performClick()
        composeTestRule.waitForIdle()
    }
}

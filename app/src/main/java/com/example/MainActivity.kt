package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AuraDatabase
import com.example.data.AuraRepository
import com.example.data.ThemeModePreference
import com.example.ui.screens.AddEditHabitSheet
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.JournalScreen
import com.example.ui.screens.MinimizedFocusTimerBar
import com.example.ui.screens.PomodoroFocusSheet
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.theme.AuraTheme
import com.example.ui.theme.AuraTokens
import com.example.ui.theme.LocalAuraColors
import com.example.viewmodel.AuraDestination
import com.example.viewmodel.AuraViewModel
import com.example.viewmodel.AuraViewModelFactory
import com.example.viewmodel.TimeStudioTab
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = AuraDatabase.getInstance(applicationContext)
        val repository = AuraRepository(database.auraDao())
        val factory = AuraViewModelFactory(repository, applicationContext)

        setContent {
            val auraViewModel: AuraViewModel = viewModel(factory = factory)
            AuraAppRoot(viewModel = auraViewModel)
        }
    }
}

@Composable
fun AuraAppRoot(viewModel: AuraViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentDestination by viewModel.currentDestination.collectAsStateWithLifecycle()
    val isAddEditSheetOpen by viewModel.isAddEditSheetOpen.collectAsStateWithLifecycle()
    val editingHabit by viewModel.editingHabit.collectAsStateWithLifecycle()
    val contextMenuHabit by viewModel.contextMenuHabit.collectAsStateWithLifecycle()
    val isJournalComposerOpen by viewModel.isJournalComposerOpen.collectAsStateWithLifecycle()
    val preselectedJournalHabitId by viewModel.preselectedJournalHabitId.collectAsStateWithLifecycle()
    val focusSession by viewModel.focusSession.collectAsStateWithLifecycle()

    val systemDark = isSystemInDarkTheme()
    val useDarkTheme = when (uiState.settings.parsedThemeMode()) {
        ThemeModePreference.LIGHT -> false
        ThemeModePreference.DARK -> true
        ThemeModePreference.SYSTEM -> systemDark
    }

    LaunchedEffect(uiState.statusBannerMessage) {
        if (uiState.statusBannerMessage != null) {
            delay(3_200L)
            viewModel.clearStatusBanner()
        }
    }

    BackHandler(enabled = currentDestination != AuraDestination.DASHBOARD) {
        viewModel.navigateTo(AuraDestination.DASHBOARD)
    }

    AuraTheme(darkTheme = useDarkTheme) {
        val colors = LocalAuraColors.current

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.backgroundPrimary)
        ) {
            val isExpandedScreen = maxWidth >= 700.dp

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = colors.backgroundPrimary,
                contentWindowInsets = WindowInsets.safeDrawing,
                bottomBar = {
                    if (!isExpandedScreen) {
                        AuraBottomNavigationBar(
                            currentDestination = currentDestination,
                            onNavigate = viewModel::navigateTo
                        )
                    }
                },
                floatingActionButton = {
                    if (currentDestination == AuraDestination.DASHBOARD || currentDestination == AuraDestination.CALENDAR) {
                        FloatingActionButton(
                            onClick = viewModel::openAddHabitSheet,
                            shape = CircleShape,
                            containerColor = colors.primaryIndigo,
                            contentColor = Color.White,
                            modifier = Modifier.testTag("aura_fab_add_habit")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add Ritual"
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isExpandedScreen) {
                        AuraSideNavigationRail(
                            currentDestination = currentDestination,
                            onNavigate = viewModel::navigateTo,
                            onQuickAdd = viewModel::openAddHabitSheet
                        )
                    }

                    Box(
                        contentAlignment = Alignment.TopCenter,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = 920.dp)
                        ) {
                            AnimatedContent(
                                targetState = currentDestination,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(AuraTokens.Motion.FastMs)) togetherWith
                                        fadeOut(animationSpec = tween(AuraTokens.Motion.FastMs))
                                },
                                label = "screenTransition"
                            ) { destination ->
                                when (destination) {
                                    AuraDestination.DASHBOARD -> {
                                        DashboardScreen(
                                            uiState = uiState,
                                            contextMenuHabit = contextMenuHabit,
                                            onToggleHabit = viewModel::toggleHabitCompletionToday,
                                            onStartFocus = { habit, tab ->
                                                viewModel.startFocusSession(
                                                    habit = habit,
                                                    initialTab = tab,
                                                    autoStartTimer = (tab == TimeStudioTab.TIMER)
                                                )
                                            },
                                            onOpenTimeStudio = { tab ->
                                                viewModel.openTimeStudio(tab)
                                            },
                                            onOpenAddHabit = viewModel::openAddHabitSheet,
                                            onOpenContextMenu = viewModel::openHabitContextMenu,
                                            onCloseContextMenu = viewModel::closeHabitContextMenu,
                                            onEditHabit = viewModel::openEditHabitSheet,
                                            onResetHabitToday = viewModel::resetHabitToday,
                                            onDeleteHabit = viewModel::deleteHabit,
                                            onOpenJournalForHabit = { habitId ->
                                                viewModel.navigateTo(AuraDestination.JOURNAL)
                                                viewModel.openJournalComposer(habitId)
                                            },
                                            onSelectCategory = viewModel::selectCategoryFilter,
                                            onToggleShowAllHabits = viewModel::toggleShowAllHabitsOnDashboard,
                                            onSelectCalendarDateAndNavigate = { epochDay ->
                                                viewModel.selectCalendarDate(epochDay)
                                                viewModel.navigateTo(AuraDestination.CALENDAR)
                                            },
                                            isExpandedScreen = isExpandedScreen
                                        )
                                    }

                                    AuraDestination.CALENDAR -> {
                                        CalendarScreen(
                                            uiState = uiState,
                                            onSelectDate = viewModel::selectCalendarDate,
                                            onChangeMonthOffset = viewModel::changeCalendarMonthOffset,
                                            onToggleHabitOnSelectedDate = viewModel::toggleHabitCompletionOnDate,
                                            isExpandedScreen = isExpandedScreen
                                        )
                                    }

                                    AuraDestination.STATISTICS -> {
                                        StatisticsScreen(
                                            uiState = uiState,
                                            isExpandedScreen = isExpandedScreen
                                        )
                                    }

                                    AuraDestination.JOURNAL -> {
                                        JournalScreen(
                                            uiState = uiState,
                                            isComposerOpen = isJournalComposerOpen,
                                            preselectedHabitId = preselectedJournalHabitId,
                                            onOpenComposer = viewModel::openJournalComposer,
                                            onCloseComposer = viewModel::closeJournalComposer,
                                            onSaveJournal = viewModel::saveJournalEntry,
                                            onDeleteJournal = viewModel::deleteJournalEntry,
                                            isExpandedScreen = isExpandedScreen
                                        )
                                    }

                                    AuraDestination.SETTINGS -> {
                                        SettingsScreen(
                                            uiState = uiState,
                                            onUpdateTheme = viewModel::updateThemeMode,
                                            onUpdateProfile = viewModel::updateProfile,
                                            onUpdateNotifications = viewModel::updateNotificationSettings,
                                            onTriggerTestNotification = viewModel::triggerTestReminderNotification,
                                            onOpenTimeStudio = viewModel::openTimeStudio,
                                            onRequestExportJson = viewModel::exportBackupToJson,
                                            onImportJson = viewModel::importBackupFromJson,
                                            onInstantBackup = viewModel::saveInstantLocalBackup,
                                            onInstantRestore = viewModel::restoreInstantLocalBackup,
                                            onResetDemoData = viewModel::resetToDefaultCuratedData,
                                            isExpandedScreen = isExpandedScreen
                                        )
                                    }
                                }
                            }

                            // Minimized Focus Timer / Stopwatch Floating Pill
                            val activeFocus = focusSession
                            if (activeFocus != null && activeFocus.isMinimized) {
                                val isStopwatch = activeFocus.activeTab == TimeStudioTab.STOPWATCH
                                MinimizedFocusTimerBar(
                                    session = activeFocus,
                                    onExpand = { viewModel.setFocusSheetMinimized(false) },
                                    onTogglePause = {
                                        if (isStopwatch) viewModel.toggleStopwatchRunning()
                                        else viewModel.toggleFocusTimerPause()
                                    },
                                    onCompleteNow = {
                                        if (isStopwatch) viewModel.completeStopwatchSessionNow()
                                        else viewModel.completeFocusSessionNow()
                                    },
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(horizontal = 20.dp, vertical = 14.dp)
                                )
                            }

                            // Subtle Status Toast Banner
                            androidx.compose.animation.AnimatedVisibility(
                                visible = uiState.statusBannerMessage != null,
                                enter = slideInVertically { -it } + fadeIn(),
                                exit = slideOutVertically { -it } + fadeOut(),
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 12.dp, start = 20.dp, end = 20.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(AuraTokens.Radius.pill),
                                    color = colors.inkPrimary,
                                    contentColor = colors.backgroundPrimary,
                                    shadowElevation = 6.dp,
                                    modifier = Modifier
                                        .testTag("status_toast_banner")
                                        .clickable { viewModel.clearStatusBanner() }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = colors.accentEmerald,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = uiState.statusBannerMessage.orEmpty(),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Full Add / Edit Habit Bottom Sheet Studio
            if (isAddEditSheetOpen) {
                AddEditHabitSheet(
                    initialHabit = editingHabit,
                    categories = uiState.categories,
                    onDismiss = viewModel::closeAddEditHabitSheet,
                    onSaveHabit = { habit -> viewModel.saveHabit(habit) },
                    onCreateCategory = viewModel::addCustomCategory
                )
            }

            // Full Time Studio Sheet (Timer, Stopwatch & Alarm/Reminder)
            val currentFocus = focusSession
            if (currentFocus != null && !currentFocus.isMinimized) {
                PomodoroFocusSheet(
                    session = currentFocus,
                    allHabits = uiState.allHabitCards.map { it.habit },
                    onSelectHabit = viewModel::selectFocusHabit,
                    onSelectTab = viewModel::selectTimeStudioTab,
                    onSelectMode = viewModel::selectFocusTimerMode,
                    onAdjustTimerMinutes = viewModel::adjustTimerDurationMinutes,
                    onResetTimer = viewModel::resetFocusTimer,
                    onTogglePause = viewModel::toggleFocusTimerPause,
                    onCompleteNow = viewModel::completeFocusSessionNow,
                    onToggleStopwatch = viewModel::toggleStopwatchRunning,
                    onLapStopwatch = viewModel::recordStopwatchLap,
                    onResetStopwatch = viewModel::resetStopwatch,
                    onCompleteStopwatch = viewModel::completeStopwatchSessionNow,
                    onUpdateAlarmConfig = viewModel::updateStudioAlarmConfig,
                    onStepAlarmTime = viewModel::stepAlarmTimeMinutes,
                    onSaveAlarm = viewModel::saveStudioAlarmToHabit,
                    onRingAlarmPreview = viewModel::triggerLiveAlarmPreview,
                    onSnoozeAlarm = viewModel::snoozeActiveAlarm,
                    onDismissAlarmPreview = viewModel::dismissActiveAlarmPreview,
                    onMinimize = { viewModel.setFocusSheetMinimized(true) },
                    onDismiss = viewModel::dismissFocusSession
                )
            }
        }
    }
}

private data class NavItemSpec(
    val destination: AuraDestination,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String,
    val testTag: String
)

private val NavigationSpecs = listOf(
    NavItemSpec(
        destination = AuraDestination.DASHBOARD,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        label = "Dashboard",
        testTag = "nav_dashboard"
    ),
    NavItemSpec(
        destination = AuraDestination.CALENDAR,
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth,
        label = "Calendar",
        testTag = "nav_calendar"
    ),
    NavItemSpec(
        destination = AuraDestination.STATISTICS,
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart,
        label = "Statistics",
        testTag = "nav_statistics"
    ),
    NavItemSpec(
        destination = AuraDestination.JOURNAL,
        selectedIcon = Icons.Filled.Book,
        unselectedIcon = Icons.Outlined.Book,
        label = "Journal",
        testTag = "nav_journal"
    ),
    NavItemSpec(
        destination = AuraDestination.SETTINGS,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        label = "Settings",
        testTag = "nav_settings"
    )
)

@Composable
private fun AuraBottomNavigationBar(
    currentDestination: AuraDestination,
    onNavigate: (AuraDestination) -> Unit
) {
    val colors = LocalAuraColors.current

    Surface(
        color = colors.glassBackground,
        border = BorderStroke(0.75.dp, colors.borderHairline),
        tonalElevation = AuraTokens.Elevation.floating
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            contentColor = colors.inkPrimary,
            tonalElevation = 0.dp,
            modifier = Modifier.navigationBarsPadding()
        ) {
            NavigationSpecs.forEach { spec ->
                val selected = currentDestination == spec.destination
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(spec.destination) },
                    icon = {
                        Icon(
                            imageVector = if (selected) spec.selectedIcon else spec.unselectedIcon,
                            contentDescription = spec.label,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = spec.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.primaryIndigo,
                        selectedTextColor = colors.primaryIndigo,
                        indicatorColor = colors.primaryIndigo.copy(alpha = 0.14f),
                        unselectedIconColor = colors.inkSecondary,
                        unselectedTextColor = colors.inkSecondary
                    ),
                    modifier = Modifier.testTag(spec.testTag)
                )
            }
        }
    }
}

@Composable
private fun AuraSideNavigationRail(
    currentDestination: AuraDestination,
    onNavigate: (AuraDestination) -> Unit,
    onQuickAdd: () -> Unit
) {
    val colors = LocalAuraColors.current

    Surface(
        color = colors.surfaceElevated,
        border = BorderStroke(0.75.dp, colors.borderHairline),
        modifier = Modifier.fillMaxHeight()
    ) {
        NavigationRail(
            containerColor = Color.Transparent,
            contentColor = colors.inkPrimary,
            header = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
                ) {
                    Text(
                        text = "AURA",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.primaryIndigo,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FloatingActionButton(
                        onClick = onQuickAdd,
                        shape = CircleShape,
                        containerColor = colors.primaryIndigo,
                        contentColor = Color.White,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "New Ritual"
                        )
                    }
                }
            },
            modifier = Modifier
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 6.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            NavigationSpecs.forEach { spec ->
                val selected = currentDestination == spec.destination
                NavigationRailItem(
                    selected = selected,
                    onClick = { onNavigate(spec.destination) },
                    icon = {
                        Icon(
                            imageVector = if (selected) spec.selectedIcon else spec.unselectedIcon,
                            contentDescription = spec.label
                        )
                    },
                    label = {
                        Text(
                            text = spec.label,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = colors.primaryIndigo,
                        selectedTextColor = colors.primaryIndigo,
                        indicatorColor = colors.primaryIndigo.copy(alpha = 0.14f),
                        unselectedIconColor = colors.inkSecondary,
                        unselectedTextColor = colors.inkSecondary
                    ),
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .testTag(spec.testTag)
                )
            }
        }
    }
}

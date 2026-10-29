package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.AlarmChimeStyle
import com.example.data.HabitEntity
import com.example.ui.components.AuraBadge
import com.example.ui.components.AuraCard
import com.example.ui.components.AuraHeroProgressRing
import com.example.ui.components.AuraIconCatalog
import com.example.ui.components.AuraPrimaryButton
import com.example.ui.components.AuraSecondaryButton
import com.example.ui.theme.AuraNumericTypography
import com.example.ui.theme.AuraPalette
import com.example.ui.theme.AuraTokens
import com.example.ui.theme.LocalAuraColors
import com.example.viewmodel.FocusSessionState
import com.example.viewmodel.FocusTimerMode
import com.example.viewmodel.TimeStudioTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroFocusSheet(
    session: FocusSessionState,
    allHabits: List<HabitEntity>,
    onSelectHabit: (HabitEntity) -> Unit,
    onSelectTab: (TimeStudioTab) -> Unit,
    onSelectMode: (FocusTimerMode) -> Unit,
    onAdjustTimerMinutes: (Int) -> Unit,
    onResetTimer: () -> Unit,
    onTogglePause: () -> Unit,
    onCompleteNow: () -> Unit,
    // Stopwatch callbacks
    onToggleStopwatch: () -> Unit,
    onLapStopwatch: () -> Unit,
    onResetStopwatch: () -> Unit,
    onCompleteStopwatch: () -> Unit,
    // Alarm & Reminder callbacks
    onUpdateAlarmConfig: (Boolean?, String?, AlarmChimeStyle?, Int?) -> Unit,
    onStepAlarmTime: (Int) -> Unit,
    onSaveAlarm: (Boolean) -> Unit,
    onRingAlarmPreview: () -> Unit,
    onSnoozeAlarm: () -> Unit,
    onDismissAlarmPreview: () -> Unit,
    onMinimize: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAuraColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val accent = AuraPalette.parseHex(session.habit.colorHex, colors.primaryIndigo)

    ModalBottomSheet(
        onDismissRequest = onMinimize,
        sheetState = sheetState,
        containerColor = colors.backgroundPrimary,
        contentColor = colors.inkPrimary
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AuraTokens.Spacing.xl, vertical = AuraTokens.Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(AuraTokens.Spacing.md)
        ) {
            // Top Header with Minimize & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onMinimize,
                    modifier = Modifier.testTag("time_studio_minimize_btn")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = "Minimize time studio",
                        tint = colors.inkSecondary
                    )
                }
                Text(
                    text = "TIME, STOPWATCH & ALARM STUDIO",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.inkSecondary
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("time_studio_close_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close time studio",
                        tint = colors.inkSecondary
                    )
                }
            }

            // 3-Mode Segmented Tab Selector: Timer | Stopwatch | Alarm & Reminder
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AuraTokens.Radius.md))
                    .background(colors.surfaceSubtle)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TimeStudioTab.entries.forEach { tab ->
                    val isSelected = session.activeTab == tab
                    Surface(
                        onClick = { onSelectTab(tab) },
                        shape = RoundedCornerShape(AuraTokens.Radius.sm),
                        color = if (isSelected) colors.surfaceElevated else Color.Transparent,
                        contentColor = if (isSelected) colors.primaryIndigo else colors.inkSecondary,
                        shadowElevation = if (isSelected && !colors.isDark) 2.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("time_studio_tab_${tab.name.lowercase()}")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
                        ) {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Ritual Switcher Strip
            if (allHabits.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allHabits.forEach { habit ->
                        val isChosen = habit.id == session.habit.id
                        val hAccent = AuraPalette.parseHex(habit.colorHex, colors.primaryIndigo)
                        Surface(
                            onClick = { onSelectHabit(habit) },
                            shape = RoundedCornerShape(AuraTokens.Radius.pill),
                            color = if (isChosen) hAccent else colors.surfaceElevated,
                            contentColor = if (isChosen) Color.White else colors.inkPrimary,
                            border = BorderStroke(1.dp, if (isChosen) hAccent else colors.borderHairline)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Icon(
                                    imageVector = AuraIconCatalog.resolve(habit.iconName),
                                    contentDescription = null,
                                    tint = if (isChosen) Color.White else hAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = habit.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            when (session.activeTab) {
                TimeStudioTab.TIMER -> {
                    CountdownTimerTabContent(
                        session = session,
                        accent = accent,
                        onSelectMode = onSelectMode,
                        onAdjustMinutes = onAdjustTimerMinutes,
                        onReset = onResetTimer,
                        onTogglePause = onTogglePause,
                        onCompleteNow = onCompleteNow
                    )
                }

                TimeStudioTab.STOPWATCH -> {
                    StopwatchTabContent(
                        session = session,
                        accent = accent,
                        onToggleStopwatch = onToggleStopwatch,
                        onLap = onLapStopwatch,
                        onReset = onResetStopwatch,
                        onCompleteNow = onCompleteStopwatch
                    )
                }

                TimeStudioTab.ALARM_REMINDER -> {
                    AlarmReminderTabContent(
                        session = session,
                        accent = accent,
                        onUpdateConfig = onUpdateAlarmConfig,
                        onStepTime = onStepAlarmTime,
                        onSaveAlarm = onSaveAlarm,
                        onRingPreview = onRingAlarmPreview,
                        onSnooze = onSnoozeAlarm,
                        onDismissPreview = onDismissAlarmPreview
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn( androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun CountdownTimerTabContent(
    session: FocusSessionState,
    accent: Color,
    onSelectMode: (FocusTimerMode) -> Unit,
    onAdjustMinutes: (Int) -> Unit,
    onReset: () -> Unit,
    onTogglePause: () -> Unit,
    onCompleteNow: () -> Unit
) {
    val colors = LocalAuraColors.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Preset Mode Chips
        androidx.compose.foundation.layout.FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FocusTimerMode.entries.forEach { mode ->
                val selected = session.mode == mode
                Surface(
                    onClick = { onSelectMode(mode) },
                    shape = RoundedCornerShape(AuraTokens.Radius.pill),
                    color = if (selected) accent else colors.surfaceElevated,
                    contentColor = if (selected) Color.White else colors.inkPrimary,
                    border = BorderStroke(1.dp, if (selected) accent else colors.borderHairline),
                    modifier = Modifier.testTag("timer_mode_${mode.name.lowercase()}")
                ) {
                    Text(
                        text = mode.label,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Hero Countdown Ring
        AuraHeroProgressRing(
            progress = session.progress,
            size = 148.dp,
            strokeWidth = 9.dp,
            primaryColor = accent,
            secondaryColor = colors.accentEmerald
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = session.formattedRemaining,
                    style = AuraNumericTypography.HeroMetric,
                    color = colors.inkPrimary,
                    modifier = Modifier.testTag("timer_remaining_text")
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when {
                        session.isFinished -> "RITUAL COMPLETE"
                        session.isRunning -> "COUNTDOWN ACTIVE"
                        else -> "TIMER PAUSED"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (session.isFinished) colors.accentEmerald else colors.inkSecondary
                )
            }
        }

        // Quick Duration Adjustment Steppers (-5m, -1m, +1m, +5m, +15m, Reset)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(-5 to "-5m", -1 to "-1m", 1 to "+1m", 5 to "+5m", 15 to "+15m").forEach { (delta, label) ->
                Surface(
                    onClick = { onAdjustMinutes(delta) },
                    shape = RoundedCornerShape(AuraTokens.Radius.sm),
                    color = colors.surfaceElevated,
                    border = BorderStroke(1.dp, colors.borderHairline),
                    modifier = Modifier.testTag("timer_adjust_${label.replace("+", "plus_").replace("-", "minus_")}")
                ) {
                    Text(
                        text = label,
                        style = AuraNumericTypography.SmallBadgeMetric,
                        color = colors.inkPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    )
                }
            }
            Surface(
                onClick = onReset,
                shape = RoundedCornerShape(AuraTokens.Radius.sm),
                color = colors.surfaceSubtle,
                border = BorderStroke(1.dp, colors.borderHairline),
                modifier = Modifier.testTag("timer_reset_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Reset Timer",
                    tint = colors.inkSecondary,
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                        .size(16.dp)
                )
            }
        }

        // Primary Timer Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AuraSecondaryButton(
                text = if (session.isRunning) "Pause" else "Start / Resume",
                icon = if (session.isRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                onClick = onTogglePause,
                modifier = Modifier.weight(1f),
                testTag = "focus_pause_resume_button"
            )
            AuraPrimaryButton(
                text = "Complete & Log",
                icon = Icons.Filled.Check,
                containerColor = colors.accentEmerald,
                onClick = onCompleteNow,
                modifier = Modifier.weight(1.3f),
                testTag = "focus_complete_now_button"
            )
        }
    }
}

@Composable
private fun StopwatchTabContent(
    session: FocusSessionState,
    accent: Color,
    onToggleStopwatch: () -> Unit,
    onLap: () -> Unit,
    onReset: () -> Unit,
    onCompleteNow: () -> Unit
) {
    val colors = LocalAuraColors.current
    val cycleProgress = (session.stopwatchElapsedSeconds % 60) / 60f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        AuraHeroProgressRing(
            progress = cycleProgress,
            size = 160.dp,
            strokeWidth = 10.dp,
            primaryColor = colors.accentEmerald,
            secondaryColor = accent
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = session.formattedStopwatch,
                    style = AuraNumericTypography.HeroMetric,
                    color = colors.inkPrimary,
                    modifier = Modifier.testTag("stopwatch_elapsed_text")
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (session.isStopwatchRunning) "CHRONOGRAPH RUNNING" else "CHRONOGRAPH READY",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (session.isStopwatchRunning) colors.accentEmerald else colors.inkSecondary
                )
            }
        }

        // Stopwatch Action Controls: Start/Pause | Lap | Reset
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AuraPrimaryButton(
                text = if (session.isStopwatchRunning) "Pause" else "Start",
                icon = if (session.isStopwatchRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                containerColor = if (session.isStopwatchRunning) colors.warningAmber else colors.primaryIndigo,
                onClick = onToggleStopwatch,
                modifier = Modifier.weight(1.2f),
                testTag = "stopwatch_start_pause_btn"
            )
            AuraSecondaryButton(
                text = "Lap",
                icon = Icons.Outlined.Flag,
                onClick = onLap,
                modifier = Modifier.weight(1f),
                testTag = "stopwatch_lap_btn"
            )
            AuraSecondaryButton(
                text = "Reset",
                icon = Icons.Outlined.Refresh,
                onClick = onReset,
                modifier = Modifier.weight(1f),
                testTag = "stopwatch_reset_btn"
            )
        }

        // Log Stopwatch Elapsed Time to Ritual Button
        AuraPrimaryButton(
            text = "Log Elapsed Time to Ritual",
            icon = Icons.Filled.Check,
            containerColor = colors.accentEmerald,
            onClick = onCompleteNow,
            modifier = Modifier.fillMaxWidth(),
            testTag = "stopwatch_complete_log_btn"
        )

        // Recorded Laps Table
        if (session.stopwatchLaps.isNotEmpty()) {
            AuraCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("LAP", style = MaterialTheme.typography.labelSmall, color = colors.inkSecondary)
                        Text("SPLIT", style = MaterialTheme.typography.labelSmall, color = colors.inkSecondary)
                        Text("TOTAL", style = MaterialTheme.typography.labelSmall, color = colors.inkSecondary)
                    }
                    HorizontalDivider(color = colors.borderHairline)
                    session.stopwatchLaps.take(6).forEach { lap ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Lap #${lap.lapNumber}",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.inkPrimary
                            )
                            Text(
                                text = "+${lap.formattedSplit}",
                                style = AuraNumericTypography.SmallBadgeMetric,
                                color = colors.accentEmerald
                            )
                            Text(
                                text = lap.formattedTotal,
                                style = AuraNumericTypography.SmallBadgeMetric,
                                color = colors.inkSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlarmReminderTabContent(
    session: FocusSessionState,
    accent: Color,
    onUpdateConfig: (Boolean?, String?, AlarmChimeStyle?, Int?) -> Unit,
    onStepTime: (Int) -> Unit,
    onSaveAlarm: (Boolean) -> Unit,
    onRingPreview: () -> Unit,
    onSnooze: () -> Unit,
    onDismissPreview: () -> Unit
) {
    val colors = LocalAuraColors.current

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Active Ringing Alarm Banner (when testing or triggered)
        if (session.isAlarmRingingPreview) {
            AuraCard(
                containerColor = colors.warningAmber.copy(alpha = 0.16f),
                borderColor = colors.warningAmber,
                contentPadding = PaddingValues(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Alarm,
                                contentDescription = null,
                                tint = colors.warningAmber,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "ALARM RINGING · ${session.alarmTime}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.warningAmber,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = session.habit.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.inkPrimary
                                )
                            }
                        }
                        AuraBadge(
                            text = session.alarmChimeStyle.label,
                            accentColor = colors.warningAmber,
                            filled = true
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AuraSecondaryButton(
                            text = "Snooze ${session.snoozeMinutes}m",
                            icon = Icons.Outlined.Snooze,
                            onClick = onSnooze,
                            modifier = Modifier.weight(1f),
                            testTag = "alarm_snooze_btn"
                        )
                        AuraPrimaryButton(
                            text = "Dismiss",
                            icon = Icons.Filled.Check,
                            containerColor = colors.accentEmerald,
                            onClick = onDismissPreview,
                            modifier = Modifier.weight(1f),
                            testTag = "alarm_dismiss_btn"
                        )
                    }
                }
            }
        }

        // Precision Alarm Time Display & Steppers
        AuraCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(18.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DAILY RITUAL ALARM & REMINDER",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.inkSecondary
                        )
                        Text(
                            text = if (session.alarmEnabled) "Enabled for ${session.habit.title}" else "Paused",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkSecondary
                        )
                    }
                    Switch(
                        checked = session.alarmEnabled,
                        onCheckedChange = { onUpdateConfig(it, null, null, null) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = accent
                        ),
                        modifier = Modifier.testTag("alarm_enable_switch")
                    )
                }

                // Big Tabular Time Display
                Text(
                    text = session.alarmTime,
                    style = AuraNumericTypography.TimerDisplay,
                    color = if (session.alarmEnabled) colors.inkPrimary else colors.inkTertiary,
                    modifier = Modifier.testTag("alarm_time_display")
                )

                // Stepper Buttons (-1h, -15m, +15m, +1h)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    listOf(-60 to "-1h", -15 to "-15m", 15 to "+15m", 60 to "+1h").forEach { (delta, label) ->
                        Surface(
                            onClick = { onStepTime(delta) },
                            shape = RoundedCornerShape(AuraTokens.Radius.sm),
                            color = colors.surfaceSubtle,
                            border = BorderStroke(1.dp, colors.borderHairline),
                            modifier = Modifier.testTag("alarm_step_${label.replace("+", "plus_").replace("-", "minus_")}")
                        ) {
                            Text(
                                text = label,
                                style = AuraNumericTypography.SmallBadgeMetric,
                                color = colors.inkPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                // Quick Preset Times
                val presetTimes = listOf("06:30", "07:30", "09:00", "12:30", "17:30", "21:00", "22:00")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetTimes.forEach { preset ->
                        val isTimeChosen = session.alarmTime == preset
                        Surface(
                            onClick = { onUpdateConfig(true, preset, null, null) },
                            shape = RoundedCornerShape(AuraTokens.Radius.pill),
                            color = if (isTimeChosen) accent else colors.surfaceSubtle,
                            contentColor = if (isTimeChosen) Color.White else colors.inkPrimary
                        ) {
                            Text(
                                text = preset,
                                style = AuraNumericTypography.SmallBadgeMetric,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Chime Tone & Snooze Duration Card
        AuraCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "ACOUSTIC CHIME & SNOOZE",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.inkSecondary
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AlarmChimeStyle.entries.forEach { chime ->
                        val selected = session.alarmChimeStyle == chime
                        Surface(
                            onClick = { onUpdateConfig(null, null, chime, null) },
                            shape = RoundedCornerShape(AuraTokens.Radius.md),
                            color = if (selected) accent else colors.surfaceSubtle,
                            contentColor = if (selected) Color.White else colors.inkPrimary,
                            border = BorderStroke(1.dp, if (selected) accent else colors.borderHairline)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(text = chime.label, style = MaterialTheme.typography.labelMedium)
                                Text(
                                    text = chime.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) Color.White.copy(alpha = 0.85f) else colors.inkTertiary
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Snooze Interval",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkSecondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(5, 10, 15).forEach { mins ->
                            val chosen = session.snoozeMinutes == mins
                            Surface(
                                onClick = { onUpdateConfig(null, null, null, mins) },
                                shape = RoundedCornerShape(AuraTokens.Radius.pill),
                                color = if (chosen) colors.primaryIndigo else colors.surfaceSubtle,
                                contentColor = if (chosen) Color.White else colors.inkPrimary
                            ) {
                                Text(
                                    text = "${mins}m",
                                    style = AuraNumericTypography.SmallBadgeMetric,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons: Ring Test Alarm Now | Save Alarm | System Alarm Clock
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AuraSecondaryButton(
                text = "Test Ring",
                icon = Icons.Outlined.NotificationsActive,
                onClick = onRingPreview,
                modifier = Modifier.weight(1f),
                testTag = "alarm_test_ring_btn"
            )
            AuraPrimaryButton(
                text = "Save Reminder",
                icon = Icons.Filled.Check,
                containerColor = accent,
                onClick = { onSaveAlarm(false) },
                modifier = Modifier.weight(1.3f),
                testTag = "alarm_save_btn"
            )
        }

        AuraSecondaryButton(
            text = "Sync with Android System Alarm Clock",
            icon = Icons.Outlined.Alarm,
            onClick = { onSaveAlarm(true) },
            modifier = Modifier.fillMaxWidth(),
            testTag = "alarm_system_clock_btn"
        )
    }
}

/**
 * Floating Glassmorphic Mini-Bar when the Time Studio (Timer or Stopwatch) is minimized.
 */
@Composable
fun MinimizedFocusTimerBar(
    session: FocusSessionState,
    onExpand: () -> Unit,
    onTogglePause: () -> Unit,
    onCompleteNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAuraColors.current
    val accent = AuraPalette.parseHex(session.habit.colorHex, colors.primaryIndigo)
    val isStopwatchMode = session.activeTab == TimeStudioTab.STOPWATCH

    Surface(
        shape = RoundedCornerShape(AuraTokens.Radius.pill),
        color = colors.glassBackground,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.4f)),
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onExpand)
            .testTag("minimized_focus_bar")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isStopwatchMode) Icons.Outlined.Timer else AuraIconCatalog.resolve(session.habit.iconName),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.habit.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.inkPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isStopwatchMode) {
                            "Stopwatch: ${session.formattedStopwatch}"
                        } else {
                            "${session.formattedRemaining} remaining"
                        },
                        style = AuraNumericTypography.SmallBadgeMetric,
                        color = accent
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onTogglePause, modifier = Modifier.size(34.dp)) {
                    val running = if (isStopwatchMode) session.isStopwatchRunning else session.isRunning
                    Icon(
                        imageVector = if (running) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        contentDescription = "Pause or resume",
                        tint = colors.inkPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onCompleteNow, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Complete session",
                        tint = colors.accentEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onExpand, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowUp,
                        contentDescription = "Expand timer",
                        tint = colors.inkSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

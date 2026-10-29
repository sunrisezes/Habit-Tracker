package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.AvTimer
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.data.CategoryEntity
import com.example.data.HabitCardModel
import com.example.data.HabitEntity
import com.example.ui.components.AuraBadge
import com.example.ui.components.AuraCard
import com.example.ui.components.AuraCompletionRingButton
import com.example.ui.components.AuraHeroProgressRing
import com.example.ui.components.AuraIconCatalog
import com.example.ui.components.AuraPrimaryButton
import com.example.ui.components.AuraSectionHeader
import com.example.ui.components.AuraZenIllustration
import com.example.ui.theme.AuraNumericTypography
import com.example.ui.theme.AuraPalette
import com.example.ui.theme.AuraTokens
import com.example.ui.theme.LocalAuraColors
import com.example.viewmodel.AuraUiState
import com.example.viewmodel.DayProgressPoint
import com.example.viewmodel.TimeStudioTab
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    uiState: AuraUiState,
    contextMenuHabit: HabitCardModel?,
    onToggleHabit: (HabitEntity) -> Unit,
    onStartFocus: (HabitEntity, TimeStudioTab) -> Unit,
    onOpenTimeStudio: (TimeStudioTab) -> Unit,
    onOpenAddHabit: () -> Unit,
    onOpenContextMenu: (HabitCardModel) -> Unit,
    onCloseContextMenu: () -> Unit,
    onEditHabit: (HabitEntity) -> Unit,
    onResetHabitToday: (Long) -> Unit,
    onDeleteHabit: (Long) -> Unit,
    onOpenJournalForHabit: (Long) -> Unit,
    onSelectCategory: (Long?) -> Unit,
    onToggleShowAllHabits: () -> Unit,
    onSelectCalendarDateAndNavigate: (Long) -> Unit,
    isExpandedScreen: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalAuraColors.current
    val haptics = LocalHapticFeedback.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_lazy_column"),
        contentPadding = PaddingValues(
            start = if (isExpandedScreen) AuraTokens.Spacing.xxl else AuraTokens.Spacing.lg,
            end = if (isExpandedScreen) AuraTokens.Spacing.xxl else AuraTokens.Spacing.lg,
            top = AuraTokens.Spacing.md,
            bottom = 128.dp
        ),
        verticalArrangement = Arrangement.spacedBy(AuraTokens.Spacing.lg)
    ) {
        // 1. Editorial Header: Current Date, Greeting & Quick Add Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.formattedCurrentDate.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.inkSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.greeting,
                        style = MaterialTheme.typography.displayMedium,
                        color = colors.inkPrimary,
                        modifier = Modifier.testTag("dashboard_greeting_text")
                    )
                }

                Surface(
                    onClick = onOpenAddHabit,
                    shape = RoundedCornerShape(AuraTokens.Radius.pill),
                    color = colors.primaryIndigo,
                    contentColor = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.testTag("dashboard_quick_add_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "New Ritual",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "New Ritual",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // 2. Quick Time Studio Bar: Timer | Stopwatch | Alarm & Reminder
        item {
            TimeStudioQuickAccessRow(
                onOpenTimer = { onOpenTimeStudio(TimeStudioTab.TIMER) },
                onOpenStopwatch = { onOpenTimeStudio(TimeStudioTab.STOPWATCH) },
                onOpenAlarm = { onOpenTimeStudio(TimeStudioTab.ALARM_REMINDER) }
            )
        }

        // 3. Motivational Quote Card
        item {
            AuraCard(
                containerColor = colors.surfaceSubtle.copy(alpha = 0.72f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Column {
                    Text(
                        text = "“${uiState.quote.text}”",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = MaterialTheme.typography.titleMedium.fontSize,
                            fontStyle = FontStyle.Italic
                        ),
                        color = colors.inkPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "— ${uiState.quote.author}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.inkSecondary
                    )
                }
            }
        }

        // 4. Hero Completion Ring + Weekly Progress + Monthly Streak
        item {
            HeroCompletionAndMomentumCard(
                completionPercentage = uiState.completionPercentageToday,
                completedCount = uiState.completedCountToday,
                scheduledCount = uiState.scheduledCountToday,
                currentStreak = uiState.statistics.currentGlobalStreak,
                monthlyStreakDays = uiState.monthlyStreakDays,
                totalFocusMinutes = uiState.statistics.totalFocusMinutes,
                weeklyProgress = uiState.weeklyProgress,
                onTapDay = onSelectCalendarDateAndNavigate
            )
        }

        // 5. Category Filter Strip & Schedule Toggle
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AuraSectionHeader(
                    title = "Today's Rituals",
                    subtitle = "Tap ring or swipe right to complete · Swipe left for timer · Long-press for menu",
                    trailing = {
                        Surface(
                            onClick = onToggleShowAllHabits,
                            shape = RoundedCornerShape(AuraTokens.Radius.pill),
                            color = if (uiState.showAllHabitsOnDashboard) {
                                colors.primaryIndigo.copy(alpha = 0.14f)
                            } else {
                                colors.surfaceSubtle
                            },
                            border = BorderStroke(1.dp, colors.borderHairline),
                            modifier = Modifier.testTag("toggle_all_habits_chip")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.FilterList,
                                    contentDescription = null,
                                    tint = colors.inkSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (uiState.showAllHabitsOnDashboard) "All Rituals" else "Scheduled Today",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.inkPrimary
                                )
                            }
                        }
                    }
                )

                CategoryFilterRow(
                    categories = uiState.categories,
                    selectedCategoryId = uiState.selectedCategoryId,
                    onSelectCategory = onSelectCategory
                )
            }
        }

        // 6. Habit Cards List or Serene Empty State
        if (uiState.todayHabitCards.isEmpty()) {
            item {
                AuraCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(AuraTokens.Spacing.xxl)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AuraZenIllustration(size = 88.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Stillness & Space",
                            style = MaterialTheme.typography.headlineSmall,
                            color = colors.inkPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No rituals match the current filter. Begin a new intentional habit or view all categories.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.inkSecondary
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        AuraPrimaryButton(
                            text = "Design New Ritual",
                            icon = Icons.Filled.Add,
                            onClick = onOpenAddHabit,
                            testTag = "empty_state_add_habit_button"
                        )
                    }
                }
            }
        } else {
            items(
                items = uiState.todayHabitCards,
                key = { it.habit.id }
            ) { cardModel ->
                LuxuryHabitCard(
                    cardModel = cardModel,
                    onToggleCompletion = {
                        if (uiState.settings.hapticsEnabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        onToggleHabit(cardModel.habit)
                    },
                    onStartFocus = { tab ->
                        if (uiState.settings.hapticsEnabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onStartFocus(cardModel.habit, tab)
                    },
                    onLongPressMenu = {
                        if (uiState.settings.hapticsEnabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        onOpenContextMenu(cardModel)
                    }
                )
            }
        }
    }

    // Long-press contextual menu bottom sheet
    if (contextMenuHabit != null) {
        HabitContextMenuSheet(
            cardModel = contextMenuHabit,
            onDismiss = onCloseContextMenu,
            onToggleComplete = {
                onToggleHabit(contextMenuHabit.habit)
                onCloseContextMenu()
            },
            onStartFocusTimer = {
                onStartFocus(contextMenuHabit.habit, TimeStudioTab.TIMER)
            },
            onStartStopwatch = {
                onStartFocus(contextMenuHabit.habit, TimeStudioTab.STOPWATCH)
            },
            onConfigureAlarm = {
                onStartFocus(contextMenuHabit.habit, TimeStudioTab.ALARM_REMINDER)
            },
            onLogReflection = {
                onOpenJournalForHabit(contextMenuHabit.habit.id)
            },
            onEdit = {
                onEditHabit(contextMenuHabit.habit)
            },
            onResetToday = {
                onResetHabitToday(contextMenuHabit.habit.id)
                onCloseContextMenu()
            },
            onDelete = {
                onDeleteHabit(contextMenuHabit.habit.id)
            }
        )
    }
}

@Composable
private fun TimeStudioQuickAccessRow(
    onOpenTimer: () -> Unit,
    onOpenStopwatch: () -> Unit,
    onOpenAlarm: () -> Unit
) {
    val colors = LocalAuraColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TimeQuickButton(
            icon = Icons.Outlined.Timer,
            title = "Timer",
            subtitle = "Pomodoro & Focus",
            accent = colors.primaryIndigo,
            onClick = onOpenTimer,
            testTag = "quick_open_timer_button",
            modifier = Modifier.weight(1f)
        )
        TimeQuickButton(
            icon = Icons.Outlined.AvTimer,
            title = "Stopwatch",
            subtitle = "Chronograph & Laps",
            accent = colors.accentEmerald,
            onClick = onOpenStopwatch,
            testTag = "quick_open_stopwatch_button",
            modifier = Modifier.weight(1f)
        )
        TimeQuickButton(
            icon = Icons.Outlined.Alarm,
            title = "Alarm",
            subtitle = "Reminders & Chime",
            accent = colors.warningAmber,
            onClick = onOpenAlarm,
            testTag = "quick_open_alarm_button",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TimeQuickButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalAuraColors.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(AuraTokens.Radius.md),
        color = colors.surfaceElevated,
        border = BorderStroke(1.dp, colors.borderHairline),
        shadowElevation = if (colors.isDark) 0.dp else 1.dp,
        modifier = modifier.testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.14f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.inkPrimary,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.inkTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun HeroCompletionAndMomentumCard(
    completionPercentage: Int,
    completedCount: Int,
    scheduledCount: Int,
    currentStreak: Int,
    monthlyStreakDays: Int,
    totalFocusMinutes: Int,
    weeklyProgress: List<DayProgressPoint>,
    onTapDay: (Long) -> Unit
) {
    val colors = LocalAuraColors.current
    val progressFloat = (completionPercentage / 100f).coerceIn(0f, 1f)

    AuraCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(AuraTokens.Spacing.xl)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AuraTokens.Spacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DAILY ALIGNMENT",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.inkSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = when {
                            completionPercentage >= 100 -> "All rituals complete"
                            completionPercentage >= 60 -> "Strong daily rhythm"
                            completionPercentage > 0 -> "Momentum underway"
                            else -> "Ready to begin"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.inkPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$completedCount of $scheduledCount scheduled rituals completed today",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AuraBadge(
                            text = "$currentStreak day streak",
                            icon = Icons.Outlined.LocalFireDepartment,
                            accentColor = colors.warningAmber,
                            filled = true
                        )
                        AuraBadge(
                            text = "$monthlyStreakDays d this month",
                            accentColor = colors.accentEmerald,
                            filled = true
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                AuraHeroProgressRing(
                    progress = progressFloat,
                    size = 114.dp,
                    strokeWidth = 9.dp
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$completionPercentage%",
                            style = AuraNumericTypography.CardMetric.copy(
                                fontSize = MaterialTheme.typography.headlineMedium.fontSize
                            ),
                            color = colors.inkPrimary
                        )
                        Text(
                            text = "${totalFocusMinutes}m focus",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.inkTertiary
                        )
                    }
                }
            }

            HorizontalDivider(color = colors.borderHairline)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WEEKLY PROGRESS",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.inkSecondary
                    )
                    Text(
                        text = "Tap any day to inspect",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkTertiary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weeklyProgress.forEach { pt ->
                        WeeklyDayRingCell(
                            point = pt,
                            onClick = { onTapDay(pt.dateEpochDay) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyDayRingCell(
    point: DayProgressPoint,
    onClick: () -> Unit
) {
    val colors = LocalAuraColors.current
    val isFull = point.completionRatio >= 0.99f
    val ringColor = when {
        isFull -> colors.accentEmerald
        point.completionRatio > 0f -> colors.primaryIndigo
        else -> colors.borderHairline
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(AuraTokens.Radius.sm))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Text(
            text = point.dayShortLabel,
            style = MaterialTheme.typography.labelSmall,
            color = if (point.isToday) colors.primaryIndigo else colors.inkSecondary,
            fontWeight = if (point.isToday) FontWeight.Bold else FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isFull -> colors.accentEmerald.copy(alpha = 0.16f)
                        point.isToday -> colors.primaryIndigo.copy(alpha = 0.10f)
                        else -> Color.Transparent
                    }
                )
                .border(
                    width = if (point.isToday) 1.8.dp else 1.2.dp,
                    color = ringColor,
                    shape = CircleShape
                )
        ) {
            if (isFull) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Completed",
                    tint = colors.accentEmerald,
                    modifier = Modifier.size(15.dp)
                )
            } else {
                Text(
                    text = point.dayOfMonth.toString(),
                    style = AuraNumericTypography.SmallBadgeMetric,
                    color = if (point.isToday) colors.primaryIndigo else colors.inkPrimary
                )
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    categories: List<CategoryEntity>,
    selectedCategoryId: Long?,
    onSelectCategory: (Long?) -> Unit
) {
    val colors = LocalAuraColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChipPill(
            label = "All",
            isSelected = selectedCategoryId == null,
            accentColor = colors.primaryIndigo,
            onClick = { onSelectCategory(null) }
        )
        categories.forEach { category ->
            val catColor = AuraPalette.parseHex(category.colorHex, colors.primaryIndigo)
            FilterChipPill(
                label = category.name,
                isSelected = selectedCategoryId == category.id,
                accentColor = catColor,
                onClick = {
                    onSelectCategory(if (selectedCategoryId == category.id) null else category.id)
                }
            )
        }
    }
}

@Composable
private fun FilterChipPill(
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val colors = LocalAuraColors.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(AuraTokens.Radius.pill),
        color = if (isSelected) accentColor else colors.surfaceElevated,
        contentColor = if (isSelected) Color.White else colors.inkPrimary,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) accentColor else colors.borderHairline
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LuxuryHabitCard(
    cardModel: HabitCardModel,
    onToggleCompletion: () -> Unit,
    onStartFocus: (TimeStudioTab) -> Unit,
    onLongPressMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAuraColors.current
    val habit = cardModel.habit
    val habitAccent = AuraPalette.parseHex(habit.colorHex, colors.primaryIndigo)

    var dragOffsetPx by remember { mutableFloatStateOf(0f) }
    val animatedOffsetPx by animateFloatAsState(
        targetValue = dragOffsetPx,
        animationSpec = AuraTokens.Motion.springSnappy(),
        label = "swipeOffset"
    )
    val swipeThresholdPx = 135f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("habit_card_${habit.id}")
    ) {
        if (animatedOffsetPx != 0f) {
            val isRightSwipe = animatedOffsetPx > 0f
            val bgAccent = if (isRightSwipe) colors.accentEmerald else colors.primaryIndigo
            Row(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(AuraTokens.Radius.lg))
                    .background(bgAccent.copy(alpha = 0.16f))
                    .padding(horizontal = 22.dp),
                horizontalArrangement = if (isRightSwipe) Arrangement.Start else Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isRightSwipe) Icons.Filled.Check else Icons.Outlined.Timer,
                    contentDescription = null,
                    tint = bgAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRightSwipe) "Complete Ritual" else "Start Focus Timer",
                    style = MaterialTheme.typography.labelLarge,
                    color = bgAccent
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(AuraTokens.Radius.lg),
            color = if (cardModel.isCompletedToday) {
                colors.surfaceElevated.copy(alpha = 0.92f)
            } else {
                colors.surfaceElevated
            },
            border = BorderStroke(
                width = 1.dp,
                color = if (cardModel.isCompletedToday) {
                    colors.accentEmerald.copy(alpha = 0.38f)
                } else {
                    colors.borderHairline
                }
            ),
            shadowElevation = if (colors.isDark) 0.dp else 1.5.dp,
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetPx.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        dragOffsetPx = (dragOffsetPx + delta).coerceIn(-220f, 220f)
                    },
                    onDragStopped = {
                        when {
                            dragOffsetPx > swipeThresholdPx -> onToggleCompletion()
                            dragOffsetPx < -swipeThresholdPx -> onStartFocus(TimeStudioTab.TIMER)
                        }
                        dragOffsetPx = 0f
                    }
                )
                .clip(RoundedCornerShape(AuraTokens.Radius.lg))
                .combinedClickable(
                    onClick = onToggleCompletion,
                    onLongClick = onLongPressMenu
                )
        ) {
            Column(
                modifier = Modifier.padding(AuraTokens.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(AuraTokens.Radius.md))
                            .background(habitAccent.copy(alpha = 0.12f))
                            .border(
                                width = 1.dp,
                                color = habitAccent.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(AuraTokens.Radius.md)
                            )
                    ) {
                        Icon(
                            imageVector = AuraIconCatalog.resolve(habit.iconName),
                            contentDescription = habit.title,
                            tint = habitAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = habit.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (cardModel.isCompletedToday) {
                                    colors.inkSecondary
                                } else {
                                    colors.inkPrimary
                                },
                                textDecoration = if (cardModel.isCompletedToday) {
                                    TextDecoration.LineThrough
                                } else {
                                    TextDecoration.None
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            if (cardModel.currentStreak > 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(AuraTokens.Radius.pill))
                                        .background(colors.warningAmber.copy(alpha = 0.14f))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.LocalFireDepartment,
                                        contentDescription = "Current streak",
                                        tint = colors.warningAmber,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${cardModel.currentStreak}d",
                                        style = AuraNumericTypography.SmallBadgeMetric,
                                        color = colors.warningAmber
                                    )
                                }
                            }
                        }

                        if (habit.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = habit.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.inkSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    AuraCompletionRingButton(
                        completedCount = cardModel.completedCountToday,
                        targetCount = cardModel.targetCountToday,
                        accentColor = habitAccent,
                        onToggle = onToggleCompletion,
                        testTag = "complete_ring_${habit.id}"
                    )
                }

                // Metadata & Quick Action Row: Category, Duration, Reminder/Alarm (clickable), Timer, Stopwatch, Menu
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    cardModel.category?.let { cat ->
                        AuraBadge(
                            text = cat.name,
                            accentColor = habitAccent,
                            filled = true
                        )
                    }

                    AuraBadge(
                        text = "${habit.estimatedMinutes}m",
                        icon = Icons.Outlined.AccessTime,
                        accentColor = colors.inkSecondary
                    )

                    // Tappable Reminder / Alarm Badge
                    Surface(
                        onClick = { onStartFocus(TimeStudioTab.ALARM_REMINDER) },
                        shape = RoundedCornerShape(AuraTokens.Radius.pill),
                        color = if (habit.reminderEnabled) {
                            colors.primaryIndigo.copy(alpha = 0.12f)
                        } else {
                            colors.surfaceSubtle
                        },
                        modifier = Modifier.testTag("habit_alarm_chip_${habit.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (habit.reminderEnabled) {
                                    Icons.Outlined.NotificationsActive
                                } else {
                                    Icons.Outlined.Alarm
                                },
                                contentDescription = "Configure reminder alarm",
                                tint = if (habit.reminderEnabled) colors.primaryIndigo else colors.inkTertiary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (habit.reminderEnabled) habit.reminderTime else "Set Alarm",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (habit.reminderEnabled) colors.primaryIndigo else colors.inkSecondary
                            )
                        }
                    }

                    AuraBadge(
                        text = habit.parsedDifficulty().label,
                        accentColor = colors.inkTertiary
                    )

                    val priorityColor = when (habit.parsedPriority().sortRank) {
                        3 -> colors.warningAmber
                        2 -> colors.primaryIndigo
                        else -> colors.inkTertiary
                    }
                    AuraBadge(
                        text = habit.parsedPriority().label,
                        accentColor = priorityColor
                    )

                    Spacer(modifier = Modifier.width(2.dp))

                    // Quick Timer Pill
                    Surface(
                        onClick = { onStartFocus(TimeStudioTab.TIMER) },
                        shape = RoundedCornerShape(AuraTokens.Radius.pill),
                        color = colors.primaryIndigo.copy(alpha = 0.10f),
                        modifier = Modifier.testTag("start_focus_${habit.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PlayArrow,
                                contentDescription = "Start Focus Timer",
                                tint = colors.primaryIndigo,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Timer",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.primaryIndigo
                            )
                        }
                    }

                    // Quick Stopwatch Pill
                    Surface(
                        onClick = { onStartFocus(TimeStudioTab.STOPWATCH) },
                        shape = RoundedCornerShape(AuraTokens.Radius.pill),
                        color = colors.accentEmerald.copy(alpha = 0.10f),
                        modifier = Modifier.testTag("start_stopwatch_${habit.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AvTimer,
                                contentDescription = "Start Stopwatch",
                                tint = colors.accentEmerald,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Stopwatch",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.accentEmerald
                            )
                        }
                    }

                    IconButton(
                        onClick = onLongPressMenu,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("habit_more_menu_${habit.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreHoriz,
                            contentDescription = "Ritual options",
                            tint = colors.inkTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitContextMenuSheet(
    cardModel: HabitCardModel,
    onDismiss: () -> Unit,
    onToggleComplete: () -> Unit,
    onStartFocusTimer: () -> Unit,
    onStartStopwatch: () -> Unit,
    onConfigureAlarm: () -> Unit,
    onLogReflection: () -> Unit,
    onEdit: () -> Unit,
    onResetToday: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalAuraColors.current
    val habit = cardModel.habit
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surfaceElevated,
        contentColor = colors.inkPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AuraTokens.Spacing.xl, vertical = AuraTokens.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = habit.title,
                style = MaterialTheme.typography.headlineSmall,
                color = colors.inkPrimary
            )
            Text(
                text = "Current Streak: ${cardModel.currentStreak} days · Longest: ${cardModel.longestStreak} days",
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            ContextActionRow(
                icon = Icons.Filled.Check,
                title = if (cardModel.isCompletedToday) "Unmark Completion" else "Complete / Increment Today",
                subtitle = "Progress: ${cardModel.completedCountToday} / ${cardModel.targetCountToday}",
                accent = colors.accentEmerald,
                onClick = onToggleComplete
            )

            ContextActionRow(
                icon = Icons.Outlined.Timer,
                title = "Launch Countdown / Pomodoro Timer",
                subtitle = "Start ${habit.estimatedMinutes} min deep focus timer",
                accent = colors.primaryIndigo,
                onClick = onStartFocusTimer
            )

            ContextActionRow(
                icon = Icons.Outlined.AvTimer,
                title = "Launch Stopwatch & Lap Tracker",
                subtitle = "Track open-ended session with split times",
                accent = colors.accentEmerald,
                onClick = onStartStopwatch
            )

            ContextActionRow(
                icon = Icons.Outlined.Alarm,
                title = "Configure Alarm & Reminder Chime",
                subtitle = if (habit.reminderEnabled) "Scheduled at ${habit.reminderTime}" else "Set daily reminder & alarm clock",
                accent = colors.warningAmber,
                onClick = onConfigureAlarm
            )

            ContextActionRow(
                icon = Icons.Outlined.EditNote,
                title = "Write Guided Reflection",
                subtitle = "Link a journal note & mood to this ritual",
                accent = colors.primaryIndigo,
                onClick = onLogReflection
            )

            ContextActionRow(
                icon = Icons.Outlined.Edit,
                title = "Edit Ritual Architecture",
                subtitle = "Modify frequency, reminders, target, or notes",
                accent = colors.inkPrimary,
                onClick = onEdit
            )

            if (cardModel.completedCountToday > 0) {
                ContextActionRow(
                    icon = Icons.Outlined.Refresh,
                    title = "Reset Today's Progress",
                    subtitle = "Clear today's completion count back to 0",
                    accent = colors.warningAmber,
                    onClick = onResetToday
                )
            }

            HorizontalDivider(
                color = colors.borderHairline,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            ContextActionRow(
                icon = Icons.Outlined.DeleteOutline,
                title = "Delete Ritual",
                subtitle = "Permanently remove this habit and its history",
                accent = colors.dangerRed,
                onClick = onDelete
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ContextActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit
) {
    val colors = LocalAuraColors.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(AuraTokens.Radius.md),
        color = colors.surfaceSubtle.copy(alpha = 0.55f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = accent
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkSecondary
                )
            }
        }
    }
}

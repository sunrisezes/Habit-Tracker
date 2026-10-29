package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.HabitEntity
import com.example.ui.components.AuraBadge
import com.example.ui.components.AuraCard
import com.example.ui.components.AuraIconCatalog
import com.example.ui.components.AuraSectionHeader
import com.example.ui.theme.AuraNumericTypography
import com.example.ui.theme.AuraPalette
import com.example.ui.theme.AuraTokens
import com.example.ui.theme.LocalAuraColors
import com.example.viewmodel.AuraUiState
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarScreen(
    uiState: AuraUiState,
    onSelectDate: (Long) -> Unit,
    onChangeMonthOffset: (Int) -> Unit,
    onToggleHabitOnSelectedDate: (HabitEntity, Long) -> Unit,
    isExpandedScreen: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalAuraColors.current
    val today = remember { LocalDate.now() }
    val displayedYearMonth = remember(uiState.calendarMonthOffset) {
        YearMonth.from(today).plusMonths(uiState.calendarMonthOffset.toLong())
    }
    val selectedDate = remember(uiState.selectedCalendarDateEpoch) {
        LocalDate.ofEpochDay(uiState.selectedCalendarDateEpoch)
    }
    val selectedDateFormatted = remember(selectedDate) {
        selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_lazy_column"),
        contentPadding = PaddingValues(
            start = if (isExpandedScreen) AuraTokens.Spacing.xxl else AuraTokens.Spacing.lg,
            end = if (isExpandedScreen) AuraTokens.Spacing.xxl else AuraTokens.Spacing.lg,
            top = AuraTokens.Spacing.md,
            bottom = 128.dp
        ),
        verticalArrangement = Arrangement.spacedBy(AuraTokens.Spacing.lg)
    ) {
        // 1. Header
        item {
            Column {
                Text(
                    text = "TEMPORAL CONTINUITY",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.inkSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Calendar & Heatmap",
                    style = MaterialTheme.typography.displayMedium,
                    color = colors.inkPrimary
                )
            }
        }

        // 2. Interactive Month Calendar with Completion Circles (Top priority on Calendar screen)
        item {
            MonthlyCompletionCalendarCard(
                yearMonth = displayedYearMonth,
                todayEpoch = uiState.todayEpochDay,
                selectedEpoch = uiState.selectedCalendarDateEpoch,
                heatmap = uiState.heatmapByEpochDay,
                onPreviousMonth = { onChangeMonthOffset(-1) },
                onNextMonth = { onChangeMonthOffset(1) },
                onSelectDate = onSelectDate
            )
        }

        // 3. 12-Week Consistency Heatmap Card
        item {
            ConsistencyHeatmapCard(
                todayEpoch = uiState.todayEpochDay,
                selectedEpoch = uiState.selectedCalendarDateEpoch,
                heatmap = uiState.heatmapByEpochDay,
                onSelectDate = onSelectDate
            )
        }

        // 4. Selected Date Inspection Header
        item {
            AuraSectionHeader(
                title = selectedDateFormatted,
                subtitle = "${uiState.completedHabitsOnSelectedDate.size} completed · ${uiState.missedHabitsOnSelectedDate.size} missed or pending"
            )
        }

        // 5. Completed Habits on Selected Date
        item {
            AuraCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "COMPLETED RITUALS",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.accentEmerald
                        )
                        AuraBadge(
                            text = "${uiState.completedHabitsOnSelectedDate.size}",
                            accentColor = colors.accentEmerald,
                            filled = true
                        )
                    }

                    if (uiState.completedHabitsOnSelectedDate.isEmpty()) {
                        Text(
                            text = "No completed rituals recorded on this date.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkSecondary
                        )
                    } else {
                        uiState.completedHabitsOnSelectedDate.forEach { (habit, completion) ->
                            val accent = AuraPalette.parseHex(habit.colorHex, colors.primaryIndigo)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(AuraTokens.Radius.md))
                                    .background(colors.surfaceSubtle.copy(alpha = 0.55f))
                                    .clickable {
                                        onToggleHabitOnSelectedDate(habit, uiState.selectedCalendarDateEpoch)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(colors.accentEmerald.copy(alpha = 0.16f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Completed",
                                            tint = colors.accentEmerald,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = habit.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = colors.inkPrimary
                                        )
                                        Text(
                                            text = "${completion.completedCount}/${completion.targetCountSnapshot} target · ${completion.durationMinutesSpent}m spent",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.inkSecondary
                                        )
                                    }
                                }
                                AuraBadge(
                                    text = "Completed",
                                    accentColor = accent
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Missed / Pending Habits on Selected Date (with 1-tap retroactive logging)
        item {
            AuraCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.selectedCalendarDateEpoch == uiState.todayEpochDay) {
                                "REMAINING TODAY (TAP TO COMPLETE)"
                            } else {
                                "MISSED RITUALS (TAP TO LOG RETROACTIVELY)"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.warningAmber
                        )
                        AuraBadge(
                            text = "${uiState.missedHabitsOnSelectedDate.size}",
                            accentColor = colors.warningAmber,
                            filled = true
                        )
                    }

                    if (uiState.missedHabitsOnSelectedDate.isEmpty()) {
                        Text(
                            text = "Zero missed rituals on this date.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkSecondary
                        )
                    } else {
                        uiState.missedHabitsOnSelectedDate.forEach { habit ->
                            val accent = AuraPalette.parseHex(habit.colorHex, colors.primaryIndigo)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(AuraTokens.Radius.md))
                                    .background(colors.surfaceSubtle.copy(alpha = 0.45f))
                                    .clickable {
                                        onToggleHabitOnSelectedDate(habit, uiState.selectedCalendarDateEpoch)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.RadioButtonUnchecked,
                                        contentDescription = "Log completion",
                                        tint = colors.inkTertiary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = habit.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = colors.inkPrimary
                                        )
                                        Text(
                                            text = "${habit.estimatedMinutes}m · ${habit.parsedDifficulty().label}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.inkSecondary
                                        )
                                    }
                                }
                                AuraBadge(
                                    text = "Log Now",
                                    accentColor = accent
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. Reflections on Selected Date
        if (uiState.journalsOnSelectedDate.isNotEmpty()) {
            item {
                AuraSectionHeader(
                    title = "Journal Reflections on This Day",
                    subtitle = "${uiState.journalsOnSelectedDate.size} entry recorded"
                )
            }
            items(
                items = uiState.journalsOnSelectedDate,
                key = { it.id }
            ) { journal ->
                val mood = journal.parsedMood()
                val moodColor = AuraPalette.parseHex(mood.colorHex, colors.primaryIndigo)
                AuraCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = journal.promptTitle,
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.inkPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            AuraBadge(
                                text = "${mood.label} · Energy ${journal.energyLevel}/5",
                                accentColor = moodColor,
                                filled = true
                            )
                        }
                        Text(
                            text = journal.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.inkSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsistencyHeatmapCard(
    todayEpoch: Long,
    selectedEpoch: Long,
    heatmap: Map<Long, Float>,
    onSelectDate: (Long) -> Unit
) {
    val colors = LocalAuraColors.current
    // 12 weeks x 7 days matrix (84 days ending today)
    val weeksCount = 12

    AuraCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "12-WEEK CONSISTENCY MATRIX",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.inkSecondary
                    )
                    Text(
                        text = "Daily Ritual Density",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.inkPrimary
                    )
                }

                // Legend
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Less", style = MaterialTheme.typography.labelSmall, color = colors.inkTertiary)
                    listOf(0f, 0.35f, 0.7f, 1f).forEach { level ->
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (level == 0f) colors.surfaceSubtle
                                    else colors.accentEmerald.copy(alpha = (0.25f + level * 0.75f).coerceIn(0f, 1f))
                                )
                        )
                    }
                    Text("Full", style = MaterialTheme.typography.labelSmall, color = colors.inkTertiary)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (weekIdx in (weeksCount - 1) downTo 0) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        for (dayIdx in 6 downTo 0) {
                            val offset = (weekIdx * 7) + dayIdx
                            val epoch = todayEpoch - offset
                            val ratio = (heatmap[epoch] ?: 0f).coerceIn(0f, 1f)
                            val isSelected = epoch == selectedEpoch

                            val cellColor = when {
                                ratio >= 0.95f -> colors.accentEmerald
                                ratio >= 0.60f -> colors.accentEmerald.copy(alpha = 0.68f)
                                ratio > 0f -> colors.primaryIndigo.copy(alpha = 0.42f)
                                else -> colors.surfaceSubtle
                            }

                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(cellColor)
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(
                                                width = 1.6.dp,
                                                color = colors.inkPrimary,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                        } else Modifier
                                    )
                                    .clickable { onSelectDate(epoch) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyCompletionCalendarCard(
    yearMonth: YearMonth,
    todayEpoch: Long,
    selectedEpoch: Long,
    heatmap: Map<Long, Float>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (Long) -> Unit
) {
    val colors = LocalAuraColors.current
    val monthTitle = remember(yearMonth) {
        yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH))
    }

    val firstDayOfMonth = yearMonth.atDay(1)
    val leadingEmptyCells = (firstDayOfMonth.dayOfWeek.value - 1).coerceIn(0, 6) // Monday = 0
    val daysInMonth = yearMonth.lengthOfMonth()
    val totalCells = ((leadingEmptyCells + daysInMonth + 6) / 7) * 7

    AuraCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Month Switcher Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPreviousMonth,
                    modifier = Modifier.testTag("calendar_prev_month_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                        contentDescription = "Previous month",
                        tint = colors.inkPrimary
                    )
                }

                Text(
                    text = monthTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.inkPrimary
                )

                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier.testTag("calendar_next_month_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        contentDescription = "Next month",
                        tint = colors.inkPrimary
                    )
                }
            }

            // Day of week headers
            val dowHeaders = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
            Row(modifier = Modifier.fillMaxWidth()) {
                dowHeaders.forEach { d ->
                    Text(
                        text = d,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.inkTertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            HorizontalDivider(color = colors.borderHairline)

            // Calendar Grid Rows
            val rows = totalCells / 7
            for (r in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (c in 0 until 7) {
                        val cellIndex = r * 7 + c
                        val dayNum = cellIndex - leadingEmptyCells + 1
                        if (dayNum in 1..daysInMonth) {
                            val date = yearMonth.atDay(dayNum)
                            val epoch = date.toEpochDay()
                            val ratio = (heatmap[epoch] ?: 0f).coerceIn(0f, 1f)
                            val isSelected = epoch == selectedEpoch
                            val isToday = epoch == todayEpoch

                            CalendarDayRingCell(
                                dayNumber = dayNum,
                                ratio = ratio,
                                isSelected = isSelected,
                                isToday = isToday,
                                onClick = { onSelectDate(epoch) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("calendar_day_$dayNum")
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayRingCell(
    dayNumber: Int,
    ratio: Float,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAuraColors.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> colors.primaryIndigo.copy(alpha = 0.16f)
                    isToday -> colors.surfaceSubtle
                    else -> Color.Transparent
                }
            )
            .clickable(onClick = onClick)
    ) {
        Canvas(modifier = Modifier.size(34.dp)) {
            val strokePx = 2.6.dp.toPx()
            val inset = strokePx
            val diameter = size.minDimension - inset * 2
            val topLeft = Offset(inset, inset)

            // Subtle base circle
            drawArc(
                color = if (isSelected) colors.primaryIndigo.copy(alpha = 0.45f) else colors.borderHairline,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokePx)
            )

            // Completion circle arc
            if (ratio > 0.01f) {
                drawArc(
                    color = if (ratio >= 0.99f) colors.accentEmerald else colors.primaryIndigo,
                    startAngle = -90f,
                    sweepAngle = ratio * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        Text(
            text = dayNumber.toString(),
            style = AuraNumericTypography.SmallBadgeMetric,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
            color = when {
                isSelected -> colors.primaryIndigo
                ratio >= 0.99f -> colors.accentEmerald
                else -> colors.inkPrimary
            }
        )
    }
}

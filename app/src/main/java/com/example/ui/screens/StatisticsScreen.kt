package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.AchievementEntity
import com.example.ui.components.AuraBadge
import com.example.ui.components.AuraCard
import com.example.ui.components.AuraHeroProgressRing
import com.example.ui.components.AuraIconCatalog
import com.example.ui.components.AuraSectionHeader
import com.example.ui.theme.AuraNumericTypography
import com.example.ui.theme.AuraPalette
import com.example.ui.theme.AuraTokens
import com.example.ui.theme.LocalAuraColors
import com.example.viewmodel.AuraUiState
import com.example.viewmodel.CategoryBreakdownItem
import com.example.viewmodel.DayProgressPoint
import com.example.viewmodel.TimeOfDayBucket
import kotlin.math.roundToInt

@Composable
fun StatisticsScreen(
    uiState: AuraUiState,
    isExpandedScreen: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalAuraColors.current
    val stats = uiState.statistics
    var selectedRangeDays by remember { mutableIntStateOf(30) }

    val displayedTrendPoints = remember(uiState.trendPoints30d, selectedRangeDays) {
        if (selectedRangeDays == 7) uiState.trendPoints30d.takeLast(7)
        else uiState.trendPoints30d
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("statistics_lazy_column"),
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
                    text = "BEHAVIORAL INTELLIGENCE",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.inkSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Longitudinal Insights",
                    style = MaterialTheme.typography.displayMedium,
                    color = colors.inkPrimary
                )
            }
        }

        // 2. Consistency Score Hero Card + 4 Key KPIs (Success Rate, Current Streak, Longest Streak, Focus Mins)
        item {
            AuraCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CONSISTENCY INDEX",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.inkSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when {
                                    stats.consistencyScore >= 80 -> "Exceptional Mastery"
                                    stats.consistencyScore >= 60 -> "Steady Intentionality"
                                    else -> "Building Foundation"
                                },
                                style = MaterialTheme.typography.headlineSmall,
                                color = colors.inkPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Composite index combining 30-day completion ratio and unbroken active days.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.inkSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        AuraHeroProgressRing(
                            progress = stats.consistencyScore / 100f,
                            size = 104.dp,
                            strokeWidth = 9.dp
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${stats.consistencyScore}",
                                    style = AuraNumericTypography.CardMetric.copy(
                                        fontSize = MaterialTheme.typography.headlineMedium.fontSize
                                    ),
                                    color = colors.inkPrimary
                                )
                                Text(
                                    text = "/ 100",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.inkTertiary
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = colors.borderHairline)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatMetricTile(
                            label = "SUCCESS RATE",
                            value = "${stats.overallSuccessRate.roundToInt()}%",
                            subtitle = "30d average",
                            accent = colors.accentEmerald,
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricTile(
                            label = "CURRENT STREAK",
                            value = "${stats.currentGlobalStreak}d",
                            subtitle = "Active rhythm",
                            accent = colors.warningAmber,
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricTile(
                            label = "LONGEST STREAK",
                            value = "${stats.longestGlobalStreak}d",
                            subtitle = "Personal record",
                            accent = colors.primaryIndigo,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 3. Completion Trends Canvas Area & Line Chart
        item {
            CompletionTrendChartCard(
                points = displayedTrendPoints,
                selectedRangeDays = selectedRangeDays,
                onSelectRange = { selectedRangeDays = it }
            )
        }

        // 4. Peak Performance Highlights (Most Productive Day, Most Productive Month, Peak Time)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InsightCalloutCard(
                    icon = Icons.AutoMirrored.Outlined.TrendingUp,
                    label = "MOST PRODUCTIVE DAY",
                    value = stats.mostProductiveDayOfWeek,
                    caption = "Highest completion velocity",
                    accent = colors.primaryIndigo,
                    modifier = Modifier.weight(1f)
                )
                InsightCalloutCard(
                    icon = Icons.Outlined.CalendarMonth,
                    label = "PEAK MONTH",
                    value = stats.mostProductiveMonth,
                    caption = "${stats.totalCompletions} total rituals",
                    accent = colors.accentEmerald,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 5. Category Analysis Breakdown
        item {
            CategoryAnalysisCard(breakdowns = uiState.categoryBreakdowns)
        }

        // 6. Time-of-Day Rhythm Analysis
        item {
            TimeOfDayAnalysisCard(
                peakBucketLabel = stats.peakTimeBucket,
                buckets = uiState.timeOfDayAnalysis
            )
        }

        // 7. Achievements & Milestones Showcase
        item {
            AuraSectionHeader(
                title = "Milestones & Honors",
                subtitle = "${uiState.achievements.count { it.isUnlocked }} of ${uiState.achievements.size} unlocked"
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                uiState.achievements.forEach { achievement ->
                    AchievementRowCard(achievement = achievement)
                }
            }
        }
    }
}

@Composable
private fun StatMetricTile(
    label: String,
    value: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val colors = LocalAuraColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(AuraTokens.Radius.md))
            .background(colors.surfaceSubtle.copy(alpha = 0.65f))
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.inkSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = AuraNumericTypography.CardMetric,
            color = accent
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = colors.inkTertiary
        )
    }
}

@Composable
private fun CompletionTrendChartCard(
    points: List<DayProgressPoint>,
    selectedRangeDays: Int,
    onSelectRange: (Int) -> Unit
) {
    val colors = LocalAuraColors.current

    AuraCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "COMPLETION TRAJECTORY",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.inkSecondary
                    )
                    Text(
                        text = "Daily Fulfillment Curve",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.inkPrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(7 to "7D", 30 to "30D").forEach { (days, label) ->
                        val active = selectedRangeDays == days
                        Surface(
                            onClick = { onSelectRange(days) },
                            shape = RoundedCornerShape(AuraTokens.Radius.pill),
                            color = if (active) colors.primaryIndigo else colors.surfaceSubtle,
                            contentColor = if (active) Color.White else colors.inkPrimary
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Custom Smooth Area & Line Chart on Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .padding(top = 8.dp, bottom = 4.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    if (points.isEmpty()) return@Canvas

                    // Horizontal guide lines (25%, 50%, 75%, 100%)
                    listOf(0f, 0.25f, 0.5f, 0.75f, 1f).forEach { level ->
                        val y = h - (level * h)
                        drawLine(
                            color = colors.borderHairline.copy(alpha = 0.65f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    val stepX = if (points.size > 1) w / (points.size - 1).toFloat() else w
                    val coords = points.mapIndexed { idx, pt ->
                        val x = idx * stepX
                        val y = h - (pt.completionRatio.coerceIn(0f, 1f) * (h * 0.88f)) - (h * 0.06f)
                        Offset(x, y)
                    }

                    val linePath = Path().apply {
                        coords.forEachIndexed { idx, offset ->
                            if (idx == 0) moveTo(offset.x, offset.y)
                            else {
                                val prev = coords[idx - 1]
                                val ctrlX = (prev.x + offset.x) / 2f
                                cubicTo(ctrlX, prev.y, ctrlX, offset.y, offset.x, offset.y)
                            }
                        }
                    }

                    val fillPath = Path().apply {
                        addPath(linePath)
                        lineTo(coords.last().x, h)
                        lineTo(coords.first().x, h)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                colors.primaryIndigo.copy(alpha = 0.28f),
                                colors.accentEmerald.copy(alpha = 0.04f),
                                Color.Transparent
                            )
                        )
                    )

                    drawPath(
                        path = linePath,
                        brush = Brush.horizontalGradient(
                            listOf(colors.primaryIndigo, colors.accentEmerald)
                        ),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Data nodes
                    coords.forEachIndexed { idx, pt ->
                        if (points.size <= 10 || idx % 4 == 0 || idx == coords.lastIndex) {
                            drawCircle(
                                color = colors.surfaceElevated,
                                radius = 4.5.dp.toPx(),
                                center = pt
                            )
                            drawCircle(
                                color = if (idx == coords.lastIndex) colors.accentEmerald else colors.primaryIndigo,
                                radius = 3.dp.toPx(),
                                center = pt
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (selectedRangeDays == 7) "7 days ago" else "30 days ago",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.inkTertiary
                )
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.accentEmerald
                )
            }
        }
    }
}

@Composable
private fun InsightCalloutCard(
    icon: ImageVector,
    label: String,
    value: String,
    caption: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val colors = LocalAuraColors.current
    AuraCard(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.inkSecondary
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = colors.inkPrimary
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkTertiary
            )
        }
    }
}

@Composable
private fun CategoryAnalysisCard(
    breakdowns: List<CategoryBreakdownItem>
) {
    val colors = LocalAuraColors.current
    AuraCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column {
                Text(
                    text = "DOMAIN BALANCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.inkSecondary
                )
                Text(
                    text = "Category Consistency Analysis",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.inkPrimary
                )
            }

            breakdowns.forEach { item ->
                val catAccent = AuraPalette.parseHex(item.category.colorHex, colors.primaryIndigo)
                val animatedWidth by animateFloatAsState(
                    targetValue = item.completionRate.coerceIn(0.04f, 1f),
                    animationSpec = AuraTokens.Motion.springSnappy(),
                    label = "catBarWidth"
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(catAccent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.category.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.inkPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${item.habitCount} rituals · ${item.completions30d} logs",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.inkTertiary
                            )
                        }

                        Text(
                            text = "${(item.completionRate * 100).roundToInt()}%",
                            style = AuraNumericTypography.SmallBadgeMetric,
                            color = catAccent
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(AuraTokens.Radius.pill))
                            .background(colors.surfaceSubtle)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedWidth)
                                .height(7.dp)
                                .clip(RoundedCornerShape(AuraTokens.Radius.pill))
                                .background(catAccent)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeOfDayAnalysisCard(
    peakBucketLabel: String,
    buckets: List<TimeOfDayBucket>
) {
    val colors = LocalAuraColors.current
    AuraCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CIRCADIAN RHYTHM",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.inkSecondary
                    )
                    Text(
                        text = "Time-of-Day Distribution",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.inkPrimary
                    )
                }
                AuraBadge(
                    text = "Peak: ${peakBucketLabel.substringBefore(" ")}",
                    icon = Icons.Outlined.WbSunny,
                    accentColor = colors.warningAmber,
                    filled = true
                )
            }

            buckets.forEach { bucket ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.width(136.dp)) {
                        Text(
                            text = bucket.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.inkPrimary
                        )
                        Text(
                            text = bucket.timeRange,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkTertiary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(AuraTokens.Radius.pill))
                            .background(colors.surfaceSubtle)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(bucket.shareRatio.coerceIn(0.05f, 1f))
                                .height(8.dp)
                                .clip(RoundedCornerShape(AuraTokens.Radius.pill))
                                .background(colors.primaryIndigo)
                        )
                    }

                    Text(
                        text = "${(bucket.shareRatio * 100).roundToInt()}%",
                        style = AuraNumericTypography.SmallBadgeMetric,
                        color = colors.inkSecondary,
                        modifier = Modifier.width(38.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementRowCard(achievement: AchievementEntity) {
    val colors = LocalAuraColors.current
    val unlocked = achievement.isUnlocked
    val accent = if (unlocked) colors.accentEmerald else colors.inkTertiary

    AuraCard(
        containerColor = if (unlocked) colors.surfaceElevated else colors.surfaceSubtle.copy(alpha = 0.55f),
        borderColor = if (unlocked) colors.accentEmerald.copy(alpha = 0.35f) else colors.borderHairline,
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.14f))
            ) {
                Icon(
                    imageVector = AuraIconCatalog.resolve(achievement.iconName),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.inkPrimary
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkSecondary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            AuraBadge(
                text = if (unlocked) "Unlocked" else "${achievement.progressCurrent}/${achievement.progressTarget}",
                icon = if (unlocked) Icons.Filled.Check else null,
                accentColor = accent,
                filled = unlocked
            )
        }
    }
}

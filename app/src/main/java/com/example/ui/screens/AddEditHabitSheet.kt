package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.NotificationsActive
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CategoryEntity
import com.example.data.FrequencyType
import com.example.data.HabitDifficulty
import com.example.data.HabitEntity
import com.example.data.HabitPriority
import com.example.ui.components.AuraBadge
import com.example.ui.components.AuraCard
import com.example.ui.components.AuraIconCatalog
import com.example.ui.components.AuraPrimaryButton
import com.example.ui.components.AuraSecondaryButton
import com.example.ui.components.AuraTextField
import com.example.ui.theme.AuraNumericTypography
import com.example.ui.theme.AuraPalette
import com.example.ui.theme.AuraTokens
import com.example.ui.theme.LocalAuraColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditHabitSheet(
    initialHabit: HabitEntity?,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSaveHabit: (HabitEntity) -> Unit,
    onCreateCategory: (String, String, String) -> Unit
) {
    val colors = LocalAuraColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val todayEpoch = remember { LocalDate.now().toEpochDay() }

    var title by remember { mutableStateOf(initialHabit?.title ?: "") }
    var description by remember { mutableStateOf(initialHabit?.description ?: "") }
    var selectedIcon by remember { mutableStateOf(initialHabit?.iconName ?: "meditation") }
    var selectedColorHex by remember { mutableStateOf(initialHabit?.colorHex ?: "#3F4E8C") }
    var selectedCategoryId by remember {
        mutableLongStateOf(initialHabit?.categoryId ?: categories.firstOrNull()?.id ?: 1L)
    }
    var selectedFrequency by remember {
        mutableStateOf(initialHabit?.parsedFrequency() ?: FrequencyType.DAILY)
    }
    val selectedDaysOfWeek = remember {
        val initialDays = initialHabit?.activeDaysOfWeek() ?: setOf(1, 2, 3, 4, 5, 6, 7)
        mutableStateListOf<Int>().apply { addAll(initialDays) }
    }
    var targetCount by remember { mutableIntStateOf(initialHabit?.targetCount ?: 1) }
    var reminderEnabled by remember { mutableStateOf(initialHabit?.reminderEnabled ?: false) }
    var reminderTime by remember { mutableStateOf(initialHabit?.reminderTime ?: "08:00") }
    var startDateEpoch by remember { mutableLongStateOf(initialHabit?.startDateEpochDay ?: todayEpoch) }
    var hasEndDate by remember { mutableStateOf(initialHabit?.endDateEpochDay != null) }
    var endDateEpoch by remember {
        mutableLongStateOf(initialHabit?.endDateEpochDay ?: (todayEpoch + 30L))
    }
    var notes by remember { mutableStateOf(initialHabit?.notes ?: "") }
    var selectedDifficulty by remember {
        mutableStateOf(initialHabit?.parsedDifficulty() ?: HabitDifficulty.MEDIUM)
    }
    var selectedPriority by remember {
        mutableStateOf(initialHabit?.parsedPriority() ?: HabitPriority.MEDIUM)
    }
    var estimatedMinutes by remember { mutableIntStateOf(initialHabit?.estimatedMinutes ?: 15) }

    // Inline New Category Creator
    var showNewCategoryField by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    var validationError by remember { mutableStateOf<String?>(null) }

    val activeAccent = AuraPalette.parseHex(selectedColorHex, colors.primaryIndigo)
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.backgroundPrimary,
        contentColor = colors.inkPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AuraTokens.Spacing.xl, vertical = AuraTokens.Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(AuraTokens.Spacing.lg)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (initialHabit == null) "NEW RITUAL" else "EDIT RITUAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.inkSecondary
                    )
                    Text(
                        text = if (initialHabit == null) "Design Intentional Habit" else "Refine Ritual",
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.inkPrimary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = colors.inkSecondary
                    )
                }
            }

            // Live Preview Card
            AuraCard(
                containerColor = colors.surfaceElevated,
                borderColor = activeAccent.copy(alpha = 0.45f),
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
                            .clip(RoundedCornerShape(AuraTokens.Radius.md))
                            .background(activeAccent.copy(alpha = 0.14f))
                    ) {
                        Icon(
                            imageVector = AuraIconCatalog.resolve(selectedIcon),
                            contentDescription = null,
                            tint = activeAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title.ifBlank { "Untitled Ritual" },
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.inkPrimary
                        )
                        Text(
                            text = description.ifBlank { "Deliberate daily practice" },
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkSecondary
                        )
                    }
                    AuraBadge(
                        text = "${estimatedMinutes}m · ${selectedFrequency.label}",
                        accentColor = activeAccent,
                        filled = true
                    )
                }
            }

            // 1. Title & Description
            AuraTextField(
                value = title,
                onValueChange = {
                    title = it
                    validationError = null
                },
                label = "Ritual Title",
                placeholder = "e.g., Morning Stillness & Breathwork",
                testTag = "habit_title_input"
            )

            AuraTextField(
                value = description,
                onValueChange = { description = it },
                label = "Concise Description",
                placeholder = "Why and how you practice this ritual...",
                singleLine = false,
                minLines = 2,
                testTag = "habit_description_input"
            )

            // 2. Icon Picker
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ICONOGRAPHY",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.inkSecondary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AuraIconCatalog.IconsByName.forEach { (iconKey, vector) ->
                        val isChosen = selectedIcon == iconKey
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(AuraTokens.Radius.md))
                                .background(
                                    if (isChosen) activeAccent else colors.surfaceElevated
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isChosen) activeAccent else colors.borderHairline,
                                    shape = RoundedCornerShape(AuraTokens.Radius.md)
                                )
                                .clickable { selectedIcon = iconKey }
                        ) {
                            Icon(
                                imageVector = vector,
                                contentDescription = iconKey,
                                tint = if (isChosen) Color.White else colors.inkPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 3. Luxury Color Picker
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "AESTHETIC TONE",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.inkSecondary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AuraPalette.Swatches.forEach { (swatchName, hexLong) ->
                        val hexString = String.format("#%06X", (0xFFFFFF and hexLong.toInt()))
                        val swatchColor = Color(hexLong or 0xFF000000)
                        val isSelected = selectedColorHex.equals(hexString, ignoreCase = true)

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(swatchColor)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) colors.inkPrimary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hexString }
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = swatchName,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Category Selector + Inline Custom Category
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CATEGORY",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.inkSecondary
                    )
                    Text(
                        text = if (showNewCategoryField) "Cancel" else "+ New Category",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.primaryIndigo,
                        modifier = Modifier.clickable {
                            showNewCategoryField = !showNewCategoryField
                        }
                    )
                }

                if (showNewCategoryField) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AuraTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            label = "Category Name",
                            placeholder = "e.g., Creative Studio",
                            modifier = Modifier.weight(1f)
                        )
                        AuraSecondaryButton(
                            text = "Add",
                            icon = Icons.Filled.Add,
                            onClick = {
                                if (newCategoryName.isNotBlank()) {
                                    onCreateCategory(newCategoryName, selectedIcon, selectedColorHex)
                                    newCategoryName = ""
                                    showNewCategoryField = false
                                }
                            }
                        )
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isChosen = selectedCategoryId == cat.id
                        Surface(
                            onClick = { selectedCategoryId = cat.id },
                            shape = RoundedCornerShape(AuraTokens.Radius.pill),
                            color = if (isChosen) activeAccent else colors.surfaceElevated,
                            contentColor = if (isChosen) Color.White else colors.inkPrimary,
                            border = BorderStroke(1.dp, if (isChosen) activeAccent else colors.borderHairline)
                        ) {
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // 5. Frequency Engine (Daily, Weekly, Monthly, Custom)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "CADENCE & FREQUENCY",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.inkSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FrequencyType.entries.forEach { freq ->
                        val isChosen = selectedFrequency == freq
                        Surface(
                            onClick = { selectedFrequency = freq },
                            shape = RoundedCornerShape(AuraTokens.Radius.sm),
                            color = if (isChosen) colors.primaryIndigo else colors.surfaceElevated,
                            contentColor = if (isChosen) Color.White else colors.inkPrimary,
                            border = BorderStroke(1.dp, if (isChosen) colors.primaryIndigo else colors.borderHairline),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text = freq.label.substringBefore(" "),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }

                if (selectedFrequency == FrequencyType.CUSTOM || selectedFrequency == FrequencyType.WEEKLY) {
                    val dayLabels = listOf(
                        1 to "M", 2 to "T", 3 to "W", 4 to "T", 5 to "F", 6 to "S", 7 to "S"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        dayLabels.forEach { (isoDay, shortLabel) ->
                            val isDayActive = selectedDaysOfWeek.contains(isoDay)
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isDayActive) activeAccent else colors.surfaceElevated
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isDayActive) activeAccent else colors.borderHairline,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        if (isDayActive && selectedDaysOfWeek.size > 1) {
                                            selectedDaysOfWeek.remove(isoDay)
                                        } else if (!isDayActive) {
                                            selectedDaysOfWeek.add(isoDay)
                                        }
                                    }
                            ) {
                                Text(
                                    text = shortLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isDayActive) Color.White else colors.inkPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // 6. Target Count & Estimated Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AuraCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "DAILY TARGET COUNT",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.inkSecondary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                onClick = { if (targetCount > 1) targetCount-- },
                                shape = CircleShape,
                                color = colors.surfaceSubtle,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("-", style = MaterialTheme.typography.titleMedium)
                                }
                            }
                            Text(
                                text = "$targetCount×",
                                style = AuraNumericTypography.CardMetric,
                                color = colors.inkPrimary
                            )
                            Surface(
                                onClick = { if (targetCount < 20) targetCount++ },
                                shape = CircleShape,
                                color = colors.surfaceSubtle,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("+", style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }

                AuraCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "ESTIMATED DURATION",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.inkSecondary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                onClick = { if (estimatedMinutes > 5) estimatedMinutes -= 5 },
                                shape = CircleShape,
                                color = colors.surfaceSubtle,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("-", style = MaterialTheme.typography.titleMedium)
                                }
                            }
                            Text(
                                text = "${estimatedMinutes}m",
                                style = AuraNumericTypography.CardMetric,
                                color = colors.inkPrimary
                            )
                            Surface(
                                onClick = { if (estimatedMinutes < 180) estimatedMinutes += 5 },
                                shape = CircleShape,
                                color = colors.surfaceSubtle,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("+", style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }
            }

            // 7. Difficulty & Priority Selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "DIFFICULTY",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.inkSecondary
                    )
                    HabitDifficulty.entries.forEach { diff ->
                        val chosen = selectedDifficulty == diff
                        Surface(
                            onClick = { selectedDifficulty = diff },
                            shape = RoundedCornerShape(AuraTokens.Radius.sm),
                            color = if (chosen) colors.primaryIndigo.copy(alpha = 0.14f) else colors.surfaceElevated,
                            border = BorderStroke(1.dp, if (chosen) colors.primaryIndigo else colors.borderHairline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = diff.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (chosen) colors.primaryIndigo else colors.inkPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "PRIORITY",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.inkSecondary
                    )
                    HabitPriority.entries.forEach { prio ->
                        val chosen = selectedPriority == prio
                        Surface(
                            onClick = { selectedPriority = prio },
                            shape = RoundedCornerShape(AuraTokens.Radius.sm),
                            color = if (chosen) colors.accentEmerald.copy(alpha = 0.14f) else colors.surfaceElevated,
                            border = BorderStroke(1.dp, if (chosen) colors.accentEmerald else colors.borderHairline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = prio.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (chosen) colors.accentEmerald else colors.inkPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                            )
                        }
                    }
                }
            }

            // 8. Reminder Time & Schedule Window (Start Date / End Date)
            AuraCard(contentPadding = PaddingValues(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.NotificationsActive,
                                contentDescription = null,
                                tint = colors.primaryIndigo,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Scheduled Reminder",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.inkPrimary
                                )
                                Text(
                                    text = "Receive gentle local prompt",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.inkSecondary
                                )
                            }
                        }
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { reminderEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = colors.primaryIndigo
                            )
                        )
                    }

                    if (reminderEnabled) {
                        val presetTimes = listOf("06:30", "07:30", "09:00", "12:30", "17:30", "21:00")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            presetTimes.forEach { preset ->
                                val isTimeSelected = reminderTime == preset
                                Surface(
                                    onClick = { reminderTime = preset },
                                    shape = RoundedCornerShape(AuraTokens.Radius.pill),
                                    color = if (isTimeSelected) colors.primaryIndigo else colors.surfaceSubtle,
                                    contentColor = if (isTimeSelected) Color.White else colors.inkPrimary
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

                    HorizontalDivider(color = colors.borderHairline)

                    // Start Date & Optional End Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "START DATE",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.inkSecondary
                            )
                            Text(
                                text = LocalDate.ofEpochDay(startDateEpoch).format(dateFormatter),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.inkPrimary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AuraSecondaryButton(
                                text = "-1d",
                                onClick = { startDateEpoch-- }
                            )
                            AuraSecondaryButton(
                                text = "Today",
                                onClick = { startDateEpoch = todayEpoch }
                            )
                            AuraSecondaryButton(
                                text = "+1d",
                                onClick = { startDateEpoch++ }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TARGET END DATE (OPTIONAL)",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.inkSecondary
                            )
                            Text(
                                text = if (hasEndDate) {
                                    LocalDate.ofEpochDay(endDateEpoch).format(dateFormatter)
                                } else {
                                    "Continuous / Ongoing"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.inkPrimary
                            )
                        }
                        Switch(
                            checked = hasEndDate,
                            onCheckedChange = {
                                hasEndDate = it
                                if (it && endDateEpoch < startDateEpoch) {
                                    endDateEpoch = startDateEpoch + 21L
                                }
                            }
                        )
                    }
                }
            }

            // 9. Personal Notes
            AuraTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Implementation Intention & Notes",
                placeholder = "Environment cues, preparation steps, or reading page goals...",
                singleLine = false,
                minLines = 2
            )

            if (validationError != null) {
                Text(
                    text = validationError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.dangerRed
                )
            }

            // Submit Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AuraSecondaryButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
                AuraPrimaryButton(
                    text = if (initialHabit == null) "Create Ritual" else "Save Changes",
                    icon = Icons.Filled.Check,
                    onClick = {
                        if (title.isBlank()) {
                            validationError = "Please enter a ritual title."
                            return@AuraPrimaryButton
                        }
                        val sortedDays = selectedDaysOfWeek.sorted().joinToString(",")
                        val entity = HabitEntity(
                            id = initialHabit?.id ?: 0L,
                            title = title.trim(),
                            description = description.trim(),
                            iconName = selectedIcon,
                            colorHex = selectedColorHex,
                            categoryId = selectedCategoryId,
                            frequencyType = selectedFrequency.name,
                            customDaysCsv = sortedDays.ifEmpty { "1,2,3,4,5,6,7" },
                            targetCount = targetCount.coerceIn(1, 50),
                            reminderEnabled = reminderEnabled,
                            reminderTime = reminderTime,
                            startDateEpochDay = startDateEpoch,
                            endDateEpochDay = if (hasEndDate) endDateEpoch.coerceAtLeast(startDateEpoch) else null,
                            notes = notes.trim(),
                            difficulty = selectedDifficulty.name,
                            priority = selectedPriority.name,
                            estimatedMinutes = estimatedMinutes.coerceIn(1, 300),
                            isArchived = false,
                            createdAt = initialHabit?.createdAt ?: System.currentTimeMillis()
                        )
                        onSaveHabit(entity)
                    },
                    modifier = Modifier.weight(1.4f),
                    testTag = "save_habit_submit_button"
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

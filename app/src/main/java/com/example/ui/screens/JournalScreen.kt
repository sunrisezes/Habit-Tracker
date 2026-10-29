package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.EditNote
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.example.data.HabitEntity
import com.example.data.JournalEntryEntity
import com.example.data.JournalMood
import com.example.ui.components.AuraBadge
import com.example.ui.components.AuraCard
import com.example.ui.components.AuraPrimaryButton
import com.example.ui.components.AuraSecondaryButton
import com.example.ui.components.AuraSectionHeader
import com.example.ui.components.AuraTextField
import com.example.ui.components.AuraZenIllustration
import com.example.ui.theme.AuraPalette
import com.example.ui.theme.AuraTokens
import com.example.ui.theme.LocalAuraColors
import com.example.viewmodel.AuraUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val GuidedPrompts = listOf(
    "What ritual created the most clarity today?",
    "Where did your energy flow most naturally?",
    "Which habit felt effortless, and what made it so?",
    "What distraction can you subtract tomorrow?",
    "What moment of stillness are you most grateful for today?"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JournalScreen(
    uiState: AuraUiState,
    isComposerOpen: Boolean,
    preselectedHabitId: Long?,
    onOpenComposer: (Long?) -> Unit,
    onCloseComposer: () -> Unit,
    onSaveJournal: (String, String, String, Int, List<Long>, String) -> Unit,
    onDeleteJournal: (Long) -> Unit,
    isExpandedScreen: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalAuraColors.current
    var selectedMoodFilter by remember { mutableStateOf<JournalMood?>(null) }
    var activePrompt by remember { mutableStateOf(GuidedPrompts.first()) }

    val habitMap = remember(uiState.allHabitCards) {
        uiState.allHabitCards.associate { it.habit.id to it.habit }
    }

    val filteredEntries = remember(uiState.journalEntries, selectedMoodFilter) {
        if (selectedMoodFilter == null) uiState.journalEntries
        else uiState.journalEntries.filter { it.parsedMood() == selectedMoodFilter }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("journal_lazy_column"),
        contentPadding = PaddingValues(
            start = if (isExpandedScreen) AuraTokens.Spacing.xxl else AuraTokens.Spacing.lg,
            end = if (isExpandedScreen) AuraTokens.Spacing.xxl else AuraTokens.Spacing.lg,
            top = AuraTokens.Spacing.md,
            bottom = 128.dp
        ),
        verticalArrangement = Arrangement.spacedBy(AuraTokens.Spacing.lg)
    ) {
        // 1. Header + Write Reflection CTA
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MINDFUL CONTEMPLATION",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.inkSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Guided Journal",
                        style = MaterialTheme.typography.displayMedium,
                        color = colors.inkPrimary
                    )
                }

                AuraPrimaryButton(
                    text = "Reflect",
                    icon = Icons.Filled.Add,
                    onClick = { onOpenComposer(null) },
                    testTag = "journal_new_entry_button"
                )
            }
        }

        // 2. Guided Reflection Prompts Carousel
        item {
            AuraCard(
                containerColor = colors.surfaceSubtle.copy(alpha = 0.72f),
                contentPadding = PaddingValues(AuraTokens.Spacing.lg)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = colors.primaryIndigo,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "DAILY GUIDED PROMPTS",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.primaryIndigo
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GuidedPrompts.forEach { prompt ->
                            Surface(
                                onClick = {
                                    activePrompt = prompt
                                    onOpenComposer(null)
                                },
                                shape = RoundedCornerShape(AuraTokens.Radius.md),
                                color = colors.surfaceElevated,
                                border = BorderStroke(1.dp, colors.borderHairline),
                                modifier = Modifier.width(240.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "“$prompt”",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontStyle = FontStyle.Italic
                                        ),
                                        color = colors.inkPrimary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Tap to respond →",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.primaryIndigo
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Mood Filter Strip
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = { selectedMoodFilter = null },
                    shape = RoundedCornerShape(AuraTokens.Radius.pill),
                    color = if (selectedMoodFilter == null) colors.primaryIndigo else colors.surfaceElevated,
                    contentColor = if (selectedMoodFilter == null) Color.White else colors.inkPrimary,
                    border = BorderStroke(1.dp, colors.borderHairline)
                ) {
                    Text(
                        text = "All States (${uiState.journalEntries.size})",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                JournalMood.entries.forEach { mood ->
                    val moodColor = AuraPalette.parseHex(mood.colorHex, colors.primaryIndigo)
                    val selected = selectedMoodFilter == mood
                    Surface(
                        onClick = {
                            selectedMoodFilter = if (selected) null else mood
                        },
                        shape = RoundedCornerShape(AuraTokens.Radius.pill),
                        color = if (selected) moodColor else colors.surfaceElevated,
                        contentColor = if (selected) Color.White else colors.inkPrimary,
                        border = BorderStroke(1.dp, if (selected) moodColor else colors.borderHairline)
                    ) {
                        Text(
                            text = mood.label,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // 4. Journal Timeline Entries
        if (filteredEntries.isEmpty()) {
            item {
                AuraCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(AuraTokens.Spacing.xxl)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AuraZenIllustration(size = 80.dp)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Unwritten Pages",
                            style = MaterialTheme.typography.headlineSmall,
                            color = colors.inkPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Capture how your daily rituals shape your mood, focus, and inner clarity.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.inkSecondary
                        )
                    }
                }
            }
        } else {
            items(
                items = filteredEntries,
                key = { it.id }
            ) { entry ->
                JournalEntryCard(
                    entry = entry,
                    habitMap = habitMap,
                    onDelete = { onDeleteJournal(entry.id) }
                )
            }
        }
    }

    if (isComposerOpen) {
        GuidedJournalComposerSheet(
            initialPrompt = activePrompt,
            preselectedHabitId = preselectedHabitId,
            habits = uiState.allHabitCards.map { it.habit },
            onDismiss = onCloseComposer,
            onSave = onSaveJournal
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JournalEntryCard(
    entry: JournalEntryEntity,
    habitMap: Map<Long, HabitEntity>,
    onDelete: () -> Unit
) {
    val colors = LocalAuraColors.current
    val mood = entry.parsedMood()
    val moodAccent = AuraPalette.parseHex(mood.colorHex, colors.primaryIndigo)
    val dateString = remember(entry.dateEpochDay) {
        LocalDate.ofEpochDay(entry.dateEpochDay)
            .format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.ENGLISH))
    }

    AuraCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = dateString.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.inkSecondary
                    )
                    AuraBadge(
                        text = mood.label,
                        accentColor = moodAccent,
                        filled = true
                    )
                    AuraBadge(
                        text = "Energy ${entry.energyLevel}/5",
                        icon = Icons.Outlined.Bolt,
                        accentColor = colors.warningAmber
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Delete entry",
                        tint = colors.inkTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = entry.promptTitle,
                style = MaterialTheme.typography.titleMedium,
                color = colors.inkPrimary
            )

            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.inkSecondary
            )

            val linkedHabits = entry.linkedHabitIds().mapNotNull { habitMap[it] }
            val tags = entry.tags()

            if (linkedHabits.isNotEmpty() || tags.isNotEmpty()) {
                HorizontalDivider(color = colors.borderHairline)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    linkedHabits.forEach { habit ->
                        val hColor = AuraPalette.parseHex(habit.colorHex, colors.primaryIndigo)
                        AuraBadge(
                            text = habit.title,
                            accentColor = hColor,
                            filled = true
                        )
                    }
                    tags.forEach { tag ->
                        AuraBadge(
                            text = "#$tag",
                            accentColor = colors.inkTertiary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun GuidedJournalComposerSheet(
    initialPrompt: String,
    preselectedHabitId: Long?,
    habits: List<HabitEntity>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int, List<Long>, String) -> Unit
) {
    val colors = LocalAuraColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var promptTitle by remember { mutableStateOf(initialPrompt) }
    var content by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf(JournalMood.FOCUSED) }
    var energyLevel by remember { mutableIntStateOf(4) }
    val selectedHabitIds = remember {
        mutableStateListOf<Long>().apply {
            if (preselectedHabitId != null) add(preselectedHabitId)
        }
    }
    var tagsText by remember { mutableStateOf("clarity,ritual") }

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
                .padding(horizontal = AuraTokens.Spacing.xl, vertical = AuraTokens.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(AuraTokens.Spacing.lg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GUIDED REFLECTION",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.inkSecondary
                    )
                    Text(
                        text = "Capture Inner State",
                        style = MaterialTheme.typography.headlineSmall,
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

            // Prompt Picker Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GuidedPrompts.forEach { p ->
                    val chosen = promptTitle == p
                    Surface(
                        onClick = { promptTitle = p },
                        shape = RoundedCornerShape(AuraTokens.Radius.pill),
                        color = if (chosen) colors.primaryIndigo else colors.surfaceElevated,
                        contentColor = if (chosen) Color.White else colors.inkPrimary,
                        border = BorderStroke(1.dp, colors.borderHairline)
                    ) {
                        Text(
                            text = p,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // Mood Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "EMOTIONAL VALENCE & MOOD",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.inkSecondary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    JournalMood.entries.forEach { mood ->
                        val mColor = AuraPalette.parseHex(mood.colorHex, colors.primaryIndigo)
                        val isChosen = selectedMood == mood
                        Surface(
                            onClick = { selectedMood = mood },
                            shape = RoundedCornerShape(AuraTokens.Radius.md),
                            color = if (isChosen) mColor else colors.surfaceElevated,
                            contentColor = if (isChosen) Color.White else colors.inkPrimary,
                            border = BorderStroke(1.dp, if (isChosen) mColor else colors.borderHairline)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = mood.label,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = mood.subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isChosen) Color.White.copy(alpha = 0.85f) else colors.inkTertiary
                                )
                            }
                        }
                    }
                }
            }

            // 5-Point Energy Matrix
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "VITALITY & ENERGY LEVEL ($energyLevel / 5)",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.inkSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..5).forEach { lvl ->
                        val active = energyLevel >= lvl
                        Surface(
                            onClick = { energyLevel = lvl },
                            shape = RoundedCornerShape(AuraTokens.Radius.sm),
                            color = if (active) colors.warningAmber else colors.surfaceElevated,
                            contentColor = if (active) Color.White else colors.inkPrimary,
                            border = BorderStroke(1.dp, if (active) colors.warningAmber else colors.borderHairline),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text = "$lvl",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }

            // Reflection Text
            AuraTextField(
                value = content,
                onValueChange = { content = it },
                label = "Reflection",
                placeholder = "Write freely with honesty and stillness...",
                singleLine = false,
                minLines = 4,
                testTag = "journal_content_input"
            )

            // Link to Rituals
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "LINK TO RITUALS",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.inkSecondary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    habits.forEach { habit ->
                        val isLinked = selectedHabitIds.contains(habit.id)
                        val hAccent = AuraPalette.parseHex(habit.colorHex, colors.primaryIndigo)
                        Surface(
                            onClick = {
                                if (isLinked) selectedHabitIds.remove(habit.id)
                                else selectedHabitIds.add(habit.id)
                            },
                            shape = RoundedCornerShape(AuraTokens.Radius.pill),
                            color = if (isLinked) hAccent else colors.surfaceElevated,
                            contentColor = if (isLinked) Color.White else colors.inkPrimary,
                            border = BorderStroke(1.dp, if (isLinked) hAccent else colors.borderHairline)
                        ) {
                            Text(
                                text = habit.title,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            AuraTextField(
                value = tagsText,
                onValueChange = { tagsText = it },
                label = "Tags (comma-separated)",
                placeholder = "clarity,morning,focus"
            )

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
                    text = "Save Reflection",
                    icon = Icons.Filled.Check,
                    onClick = {
                        if (content.isNotBlank()) {
                            onSave(
                                promptTitle,
                                content,
                                selectedMood.name,
                                energyLevel,
                                selectedHabitIds.toList(),
                                tagsText
                            )
                        }
                    },
                    modifier = Modifier.weight(1.4f),
                    testTag = "save_journal_submit_button"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

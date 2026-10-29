package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ThemeModePreference
import com.example.ui.components.AuraBadge
import com.example.ui.components.AuraCard
import com.example.ui.components.AuraPrimaryButton
import com.example.ui.components.AuraSecondaryButton
import com.example.ui.components.AuraTextField
import com.example.ui.theme.AuraTokens
import com.example.ui.theme.LocalAuraColors
import com.example.viewmodel.AuraUiState
import com.example.viewmodel.TimeStudioTab
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SettingsScreen(
    uiState: AuraUiState,
    onUpdateTheme: (ThemeModePreference) -> Unit,
    onUpdateProfile: (String, String) -> Unit,
    onUpdateNotifications: (Boolean, String, String, Boolean) -> Unit,
    onTriggerTestNotification: () -> Unit,
    onOpenTimeStudio: (TimeStudioTab) -> Unit,
    onRequestExportJson: ((String) -> Unit) -> Unit,
    onImportJson: (String) -> Unit,
    onInstantBackup: () -> Unit,
    onInstantRestore: () -> Unit,
    onResetDemoData: () -> Unit,
    isExpandedScreen: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalAuraColors.current
    val context = LocalContext.current
    val settings = uiState.settings

    var editedName by remember(settings.userName) { mutableStateOf(settings.userName) }
    var editedBio by remember(settings.userBio) { mutableStateOf(settings.userBio) }
    var pendingExportContent by remember { mutableStateOf<String?>(null) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val json = pendingExportContent
        if (uri != null && json != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(json.toByteArray(Charsets.UTF_8))
                }
            }
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (!text.isNullOrBlank()) {
                    onImportJson(text)
                }
            }
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        onTriggerTestNotification()
    }

    val initials = remember(settings.userName) {
        settings.userName.split(" ")
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .take(2)
            .joinToString("")
            .ifEmpty { "AU" }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_lazy_column"),
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
                    text = "STUDIO & PREFERENCES",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.inkSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Architecture & Vault",
                    style = MaterialTheme.typography.displayMedium,
                    color = colors.inkPrimary
                )
            }
        }

        // 2. Profile Identity Card (No hardcoded personal name)
        item {
            AuraCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(colors.primaryIndigo.copy(alpha = 0.16f))
                        ) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.headlineSmall,
                                color = colors.primaryIndigo,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PERSONAL IDENTITY",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.inkSecondary
                            )
                            Text(
                                text = settings.userName.ifBlank { "Personal Ritual Studio" },
                                style = MaterialTheme.typography.titleLarge,
                                color = colors.inkPrimary
                            )
                        }
                    }

                    AuraTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = "Display Name (Optional)",
                        placeholder = "Enter your preferred name...",
                        testTag = "profile_name_input"
                    )

                    AuraTextField(
                        value = editedBio,
                        onValueChange = { editedBio = it },
                        label = "Personal Intention Statement",
                        singleLine = false,
                        minLines = 2,
                        testTag = "profile_bio_input"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        AuraPrimaryButton(
                            text = "Save Identity",
                            icon = Icons.Filled.Check,
                            onClick = { onUpdateProfile(editedName, editedBio) },
                            testTag = "save_profile_button"
                        )
                    }
                }
            }
        }

        // 3. Theme Appearance Card (Light, Dark, System with smooth animation)
        item {
            AuraCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.DarkMode,
                            contentDescription = null,
                            tint = colors.primaryIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ATMOSPHERE & THEME",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.inkSecondary
                            )
                            Text(
                                text = "Smoothly animated surface appearance",
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.inkPrimary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val activeMode = settings.parsedThemeMode()
                        ThemeModePreference.entries.forEach { mode ->
                            val selected = activeMode == mode
                            Surface(
                                onClick = { onUpdateTheme(mode) },
                                shape = RoundedCornerShape(AuraTokens.Radius.md),
                                color = if (selected) colors.primaryIndigo else colors.surfaceSubtle,
                                contentColor = if (selected) Color.White else colors.inkPrimary,
                                border = BorderStroke(
                                    1.dp,
                                    if (selected) colors.primaryIndigo else colors.borderHairline
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("theme_mode_${mode.name.lowercase()}")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                ) {
                                    Text(
                                        text = mode.label,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Notifications, Alarms & Tactile Haptics
        item {
            AuraCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsActive,
                            contentDescription = null,
                            tint = colors.accentEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "REMINDERS, ALARMS & SENSORY FEEDBACK",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.inkSecondary
                            )
                            Text(
                                text = "Quiet hours: ${settings.quietHoursStart} – ${settings.quietHoursEnd}",
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.inkPrimary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Scheduled Ritual Notifications",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.inkPrimary
                            )
                            Text(
                                text = "Prompt when daily rituals or focus timers conclude",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.inkSecondary
                            )
                        }
                        Switch(
                            checked = settings.notificationsEnabled,
                            onCheckedChange = {
                                onUpdateNotifications(
                                    it,
                                    settings.quietHoursStart,
                                    settings.quietHoursEnd,
                                    settings.hapticsEnabled
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = colors.accentEmerald
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Tactile Haptic Micro-Feedback",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.inkPrimary
                            )
                            Text(
                                text = "Subtle physical resonance on ring completion and swipe",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.inkSecondary
                            )
                        }
                        Switch(
                            checked = settings.hapticsEnabled,
                            onCheckedChange = {
                                onUpdateNotifications(
                                    settings.notificationsEnabled,
                                    settings.quietHoursStart,
                                    settings.quietHoursEnd,
                                    it
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = colors.primaryIndigo
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AuraSecondaryButton(
                            text = "Test Notification",
                            icon = Icons.Outlined.NotificationsActive,
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    onTriggerTestNotification()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "test_notification_button"
                        )
                        AuraSecondaryButton(
                            text = "Alarm Studio",
                            icon = Icons.Outlined.Alarm,
                            onClick = { onOpenTimeStudio(TimeStudioTab.ALARM_REMINDER) },
                            modifier = Modifier.weight(1f),
                            testTag = "settings_open_alarm_studio_button"
                        )
                    }
                }
            }
        }

        // 5. Data Portability: Export JSON, Import JSON, Local Snapshot Backup & Restore
        item {
            AuraCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Backup,
                                contentDescription = null,
                                tint = colors.primaryIndigo,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "DATA PORTABILITY & VAULT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.inkSecondary
                                )
                                Text(
                                    text = "Export, Import, Backup & Restore",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.inkPrimary
                                )
                            }
                        }

                        settings.lastBackupTimestamp?.let { ts ->
                            val formattedTs = remember(ts) {
                                Instant.ofEpochMilli(ts)
                                    .atZone(ZoneId.systemDefault())
                                    .format(DateTimeFormatter.ofPattern("MMM d, HH:mm", Locale.ENGLISH))
                            }
                            AuraBadge(
                                text = "Synced $formattedTs",
                                accentColor = colors.accentEmerald,
                                filled = true
                            )
                        }
                    }

                    Text(
                        text = "Your entire relational SQLite schema (Habits, Categories, Completion History, Journal Reflections, Settings, and Achievements) can be exported as portable JSON or backed up to your local device vault.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AuraSecondaryButton(
                            text = "Export JSON",
                            icon = Icons.Outlined.FileDownload,
                            onClick = {
                                onRequestExportJson { json ->
                                    pendingExportContent = json
                                    createDocumentLauncher.launch("aura_rituals_backup.json")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "export_json_button"
                        )
                        AuraSecondaryButton(
                            text = "Import JSON",
                            icon = Icons.Outlined.FileUpload,
                            onClick = {
                                openDocumentLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "import_json_button"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AuraPrimaryButton(
                            text = "Backup Now",
                            icon = Icons.Outlined.Backup,
                            onClick = onInstantBackup,
                            modifier = Modifier.weight(1f),
                            testTag = "instant_backup_button"
                        )
                        AuraSecondaryButton(
                            text = "Restore Vault",
                            icon = Icons.Outlined.Restore,
                            onClick = onInstantRestore,
                            modifier = Modifier.weight(1f),
                            testTag = "instant_restore_button"
                        )
                    }

                    HorizontalDivider(color = colors.borderHairline)

                    AuraSecondaryButton(
                        text = "Reset to Curated Default Rituals",
                        icon = Icons.Outlined.Refresh,
                        onClick = onResetDemoData,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "reset_curated_data_button"
                    )
                }
            }
        }

        // 6. Privacy & About Card
        item {
            AuraCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = colors.accentEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PRIVACY & LOCAL SOVEREIGNTY",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.accentEmerald
                        )
                    }
                    Text(
                        text = "Aura stores 100% of your habits, streaks, and personal reflections locally on your device inside an indexed Room SQLite database. Zero third-party trackers, zero telemetry.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkSecondary
                    )
                    HorizontalDivider(color = colors.borderHairline)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = colors.inkTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Aura Rituals Studio · v1.0",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.inkSecondary
                            )
                        }
                        Text(
                            text = "SQLite + Jetpack Compose",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.inkTertiary
                        )
                    }
                }
            }
        }
    }
}

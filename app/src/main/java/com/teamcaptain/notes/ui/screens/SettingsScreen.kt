package com.teamcaptain.notes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.teamcaptain.notes.BuildConfig
import com.teamcaptain.notes.data.model.TeamMood
import com.teamcaptain.notes.ui.AppText
import com.teamcaptain.notes.ui.AppViewModel
import com.teamcaptain.notes.ui.components.BoardCard
import com.teamcaptain.notes.ui.components.CaptainScaffold
import com.teamcaptain.notes.ui.components.Gap
import com.teamcaptain.notes.ui.components.SectionLabel
import com.teamcaptain.notes.ui.components.StatusChip
import com.teamcaptain.notes.ui.components.moodColor
import com.teamcaptain.notes.ui.navigation.Routes
import com.teamcaptain.notes.ui.theme.CaptainBlue
import com.teamcaptain.notes.ui.theme.ErrorRed
import com.teamcaptain.notes.ui.theme.MutedGray
import com.teamcaptain.notes.ui.theme.SuccessGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(vm: AppViewModel, nav: NavController) {
    val data by vm.appData.collectAsState()
    val settings = data.settings

    var confirm by remember { mutableStateOf<ConfirmAction?>(null) }

    val tokenConfigured = BuildConfig.FOOTBALL_DATA_API_TOKEN.isNotBlank() &&
        BuildConfig.FOOTBALL_DATA_API_TOKEN != "your_api_token_here"

    CaptainScaffold(title = "Settings", onBack = { nav.popBackStack() }) { inner ->
        Column(
            Modifier.fillMaxSize().padding(inner).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            // Captain preferences
            SectionLabel("Captain preferences")
            BoardCard {
                Text("Default team mood", style = MaterialTheme.typography.titleMedium)
                Gap(4)
                Text("Pre-selected when creating a new match.", style = MaterialTheme.typography.labelMedium, color = MutedGray)
                Gap(8)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip(
                        text = "None", container = CaptainBlue,
                        selected = settings.defaultMood == null,
                        onClick = { vm.setDefaultMood(null) },
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                    TeamMood.entries.forEach { m ->
                        StatusChip(
                            text = m.label, container = moodColor(m),
                            selected = settings.defaultMood == m,
                            onClick = { vm.setDefaultMood(m) },
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
                Gap(12)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Compact mode", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = settings.compactMode, onCheckedChange = { vm.setCompactMode(it) })
                }
            }

            Gap(14)
            SectionLabel("Match Schedule")
            BoardCard {
                NavRow("Match Schedule settings") { nav.navigate(Routes.SCHEDULE_SETTINGS) }
                Gap(8)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("API status", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    StatusChip(
                        text = if (tokenConfigured) "Token configured" else "No token · demo",
                        container = if (tokenConfigured) SuccessGreen else MutedGray
                    )
                }
                Gap(10)
                OutlinedButton(onClick = { vm.clearMatchCache() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Clear match cache")
                }
            }

            Gap(14)
            SectionLabel("General")
            BoardCard {
                OutlinedButton(
                    onClick = {
                        vm.showOnboardingAgain()
                        nav.navigate(Routes.ONBOARDING)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Show onboarding again") }
            }

            Gap(14)
            SectionLabel("Data management")
            BoardCard {
                DangerButton("Delete all players") { confirm = ConfirmAction.DELETE_PLAYERS }
                Gap(8)
                DangerButton("Delete all matches") { confirm = ConfirmAction.DELETE_MATCHES }
                Gap(8)
                DangerButton("Delete all tasks and attendance") { confirm = ConfirmAction.DELETE_TASKS_ATT }
                Gap(8)
                DangerButton("Reset all local data") { confirm = ConfirmAction.RESET_ALL }
            }

            Gap(14)
            SectionLabel("About & privacy")
            BoardCard {
                Text("App information", style = MaterialTheme.typography.titleMedium)
                Gap(4)
                Text(AppText.APP_INFO, style = MaterialTheme.typography.bodyMedium, color = MutedGray)
                Gap(12)
                Text("Captain notes", style = MaterialTheme.typography.titleMedium)
                Gap(4)
                Text(AppText.CAPTAIN_DISCLAIMER, style = MaterialTheme.typography.bodyMedium, color = MutedGray)
                Gap(12)
                Text("Match schedule data", style = MaterialTheme.typography.titleMedium)
                Gap(4)
                Text(AppText.SCHEDULE_DISCLAIMER, style = MaterialTheme.typography.bodyMedium, color = MutedGray)
                Gap(12)
                Text("Privacy", style = MaterialTheme.typography.titleMedium)
                Gap(4)
                Text(AppText.PRIVACY_NOTE, style = MaterialTheme.typography.bodyMedium, color = MutedGray)
            }
            Gap(24)
        }
    }

    confirm?.let { action ->
        ConfirmDialog(
            action = action,
            onConfirm = {
                when (action) {
                    ConfirmAction.DELETE_PLAYERS -> vm.deleteAllPlayers()
                    ConfirmAction.DELETE_MATCHES -> vm.deleteAllMatches()
                    ConfirmAction.DELETE_TASKS_ATT -> vm.deleteAllTasksAndAttendance()
                    ConfirmAction.RESET_ALL -> vm.resetAll()
                }
                confirm = null
            },
            onDismiss = { confirm = null }
        )
    }
}

private enum class ConfirmAction(val title: String, val message: String) {
    DELETE_PLAYERS("Delete all players?", "This removes every player and their attendance records. This cannot be undone."),
    DELETE_MATCHES("Delete all matches?", "This removes every match record with its tasks and attendance. This cannot be undone."),
    DELETE_TASKS_ATT("Delete tasks and attendance?", "This removes all match tasks and attendance records. Players and matches are kept."),
    RESET_ALL("Reset all local data?", "This erases players, matches, tasks, attendance, settings, and the match cache. This cannot be undone.")
}

@Composable
private fun ConfirmDialog(action: ConfirmAction, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(action.title) },
        text = { Text(action.message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Delete", color = ErrorRed) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun NavRow(label: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        androidx.compose.material3.IconButton(onClick = onClick) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = label)
        }
    }
}

@Composable
private fun DangerButton(label: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(label, color = ErrorRed)
    }
}

package com.teamcaptain.notes.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import com.teamcaptain.notes.ui.AppText
import com.teamcaptain.notes.ui.AppViewModel
import com.teamcaptain.notes.ui.components.BoardCard
import com.teamcaptain.notes.ui.components.CaptainScaffold
import com.teamcaptain.notes.ui.components.Gap
import com.teamcaptain.notes.ui.theme.ErrorRed
import com.teamcaptain.notes.ui.theme.MutedGray
import com.teamcaptain.notes.util.DateUtils

@Composable
fun ScheduleSettingsScreen(vm: AppViewModel, nav: NavController) {
    val data by vm.appData.collectAsState()
    val current = data.settings.matchSchedule

    var apiEnabled by remember { mutableStateOf(current.apiEnabled) }
    var useDemo by remember { mutableStateOf(current.useDemoData) }
    var dateFrom by remember { mutableStateOf(current.dateFrom) }
    var dateTo by remember { mutableStateOf(current.dateTo) }
    var competition by remember { mutableStateOf(current.competitionCode) }
    var saved by remember { mutableStateOf(false) }

    val fromError = if (dateFrom.isNotBlank() && !DateUtils.isValidDate(dateFrom)) "Use YYYY-MM-DD or leave empty." else null
    val toError = when {
        dateTo.isNotBlank() && !DateUtils.isValidDate(dateTo) -> "Use YYYY-MM-DD or leave empty."
        !DateUtils.dateNotAfter(dateFrom, dateTo) -> "End date can't be before start date."
        else -> null
    }
    val canSave = fromError == null && toError == null

    CaptainScaffold(title = "Match Schedule settings", onBack = { nav.popBackStack() }) { inner ->
        Column(
            Modifier.fillMaxSize().padding(inner).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            BoardCard {
                ToggleRow("Match Schedule API enabled", apiEnabled) { apiEnabled = it; saved = false }
                Gap(6)
                ToggleRow("Always use demo data", useDemo) { useDemo = it; saved = false }
                Gap(6)
                Text(
                    "Demo mode never uses the network. When the API is off or no token is set, demo matches are shown.",
                    style = MaterialTheme.typography.labelMedium, color = MutedGray
                )
            }

            Gap(14)
            BoardCard {
                Text("Custom date window (optional)", style = MaterialTheme.typography.titleMedium)
                Gap(4)
                Text(
                    "Leave both empty to use the default 10-day window (today + 9 days).",
                    style = MaterialTheme.typography.labelMedium, color = MutedGray
                )
                Gap(10)
                OutlinedTextField(
                    value = dateFrom,
                    onValueChange = { dateFrom = it; saved = false },
                    label = { Text("Date from (YYYY-MM-DD)") },
                    singleLine = true,
                    isError = fromError != null,
                    supportingText = { fromError?.let { Text(it, color = ErrorRed) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Gap(8)
                OutlinedTextField(
                    value = dateTo,
                    onValueChange = { dateTo = it; saved = false },
                    label = { Text("Date to (YYYY-MM-DD)") },
                    singleLine = true,
                    isError = toError != null,
                    supportingText = { toError?.let { Text(it, color = ErrorRed) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Gap(8)
                OutlinedTextField(
                    value = competition,
                    onValueChange = { competition = it.uppercase(); saved = false },
                    label = { Text("Competition code filter (optional, e.g. PL)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Gap(10)
                OutlinedButton(
                    onClick = {
                        dateFrom = ""; dateTo = ""; competition = ""; saved = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Reset to default 10-day window") }
            }

            Gap(14)
            Button(
                enabled = canSave,
                onClick = {
                    vm.updateMatchSchedule {
                        it.copy(
                            apiEnabled = apiEnabled,
                            useDemoData = useDemo,
                            dateFrom = dateFrom.trim(),
                            dateTo = dateTo.trim(),
                            competitionCode = competition.trim()
                        )
                    }
                    saved = true
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save settings") }

            if (saved) {
                Gap(6)
                Text("Settings saved.", color = MutedGray, style = MaterialTheme.typography.labelMedium)
            }

            Gap(10)
            OutlinedButton(
                onClick = { vm.clearMatchCache() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Clear match cache") }

            Gap(14)
            Text(AppText.SCHEDULE_DISCLAIMER, style = MaterialTheme.typography.labelMedium, color = MutedGray)
            Gap(24)
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

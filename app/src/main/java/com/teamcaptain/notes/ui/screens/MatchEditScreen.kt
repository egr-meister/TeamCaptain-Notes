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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.teamcaptain.notes.data.model.TeamMood
import com.teamcaptain.notes.ui.AppViewModel
import com.teamcaptain.notes.ui.components.CaptainScaffold
import com.teamcaptain.notes.ui.components.Gap
import com.teamcaptain.notes.ui.components.StatusChip
import com.teamcaptain.notes.ui.components.moodColor
import com.teamcaptain.notes.ui.navigation.Routes
import com.teamcaptain.notes.ui.theme.ErrorRed
import com.teamcaptain.notes.ui.theme.MutedGray
import com.teamcaptain.notes.util.DateUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MatchEditScreen(vm: AppViewModel, nav: NavController, matchId: String?) {
    val data by vm.appData.collectAsState()
    val existing = remember(matchId) { vm.match(matchId) }

    var date by remember { mutableStateOf(existing?.date ?: DateUtils.today()) }
    var time by remember { mutableStateOf(existing?.time ?: "") }
    var opponent by remember { mutableStateOf(existing?.opponentName ?: "") }
    var venue by remember { mutableStateOf(existing?.venue ?: "") }
    var mood by remember {
        mutableStateOf(existing?.teamMood ?: data.settings.defaultMood ?: TeamMood.NEUTRAL)
    }
    var preNotes by remember { mutableStateOf(existing?.preMatchNotes ?: "") }
    var postNotes by remember { mutableStateOf(existing?.postMatchNotes ?: "") }
    var result by remember { mutableStateOf(existing?.resultSummary ?: "") }

    var opponentError by remember { mutableStateOf<String?>(null) }
    val dateError = if (date.isNotBlank() && !DateUtils.isValidDate(date)) "Use format YYYY-MM-DD." else null
    val timeError = if (time.isNotBlank() && !DateUtils.isValidTime(time)) "Use format HH:mm (or leave empty)." else null

    CaptainScaffold(
        title = if (existing == null) "New match note" else "Edit match",
        onBack = { nav.popBackStack() }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = opponent,
                onValueChange = { opponent = it; opponentError = null },
                label = { Text("Opponent name") },
                singleLine = true,
                isError = opponentError != null,
                supportingText = { opponentError?.let { Text(it, color = ErrorRed) } },
                modifier = Modifier.fillMaxWidth()
            )
            Gap()
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    singleLine = true,
                    isError = dateError != null,
                    supportingText = { dateError?.let { Text(it, color = ErrorRed) } },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time (HH:mm)") },
                    singleLine = true,
                    isError = timeError != null,
                    supportingText = { timeError?.let { Text(it, color = ErrorRed) } },
                    modifier = Modifier.weight(1f)
                )
            }
            Gap()
            OutlinedTextField(
                value = venue,
                onValueChange = { venue = it },
                label = { Text("Venue (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Gap()
            Text("Team mood", style = MaterialTheme.typography.labelLarge)
            Gap(6)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TeamMood.entries.forEach { m ->
                    StatusChip(
                        text = m.label,
                        container = moodColor(m),
                        selected = m == mood,
                        onClick = { mood = m },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
            Gap()
            OutlinedTextField(
                value = preNotes,
                onValueChange = { preNotes = it },
                label = { Text("Pre-match notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Gap()
            OutlinedTextField(
                value = postNotes,
                onValueChange = { postNotes = it },
                label = { Text("Post-match notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Gap()
            OutlinedTextField(
                value = result,
                onValueChange = { result = it },
                label = { Text("Result summary (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Gap(20)
            Button(
                onClick = {
                    if (opponent.isBlank()) { opponentError = "Opponent name is required."; return@Button }
                    if (dateError != null || timeError != null) return@Button
                    val id = vm.upsertMatch(
                        existingId = existing?.id,
                        date = date, time = time, opponentName = opponent, venue = venue,
                        teamMood = mood, preMatchNotes = preNotes, postMatchNotes = postNotes,
                        resultSummary = result
                    )
                    if (existing == null) {
                        // Go straight to the new match's detail.
                        nav.popBackStack()
                        nav.navigate(Routes.matchDetail(id))
                    } else {
                        nav.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (existing == null) "Save match note" else "Save changes") }

            if (existing != null) {
                Gap()
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { nav.navigate(Routes.attendance(existing.id)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null)
                        Text("  Attendance")
                    }
                    OutlinedButton(
                        onClick = { nav.navigate(Routes.tasks(existing.id)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.PlaylistAddCheck, contentDescription = null)
                        Text("  Tasks")
                    }
                }
                Gap()
                OutlinedButton(
                    onClick = { vm.deleteMatch(existing.id); nav.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = ErrorRed)
                    Text("  Delete match", color = ErrorRed)
                }
            }
            Gap(8)
            Text(
                "Match notes are stored locally on this device only.",
                style = MaterialTheme.typography.labelMedium, color = MutedGray
            )
            Gap(24)
        }
    }
}

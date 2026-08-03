package com.teamcaptain.notes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.teamcaptain.notes.data.model.AttendanceStatus
import com.teamcaptain.notes.domain.Summaries
import com.teamcaptain.notes.ui.AppViewModel
import com.teamcaptain.notes.ui.components.BoardCard
import com.teamcaptain.notes.ui.components.CaptainScaffold
import com.teamcaptain.notes.ui.components.EmptyState
import com.teamcaptain.notes.ui.components.Gap
import com.teamcaptain.notes.ui.components.KeyValueRow
import com.teamcaptain.notes.ui.components.MoodBadge
import com.teamcaptain.notes.ui.components.StatusChip
import com.teamcaptain.notes.ui.components.attendanceColor
import com.teamcaptain.notes.ui.navigation.Routes
import com.teamcaptain.notes.ui.theme.CaptainBlue
import com.teamcaptain.notes.ui.theme.CaptainGreen
import com.teamcaptain.notes.ui.theme.MutedGray

@Composable
fun MatchDetailScreen(vm: AppViewModel, nav: NavController, matchId: String?) {
    val data by vm.appData.collectAsState()
    val match = data.matches.firstOrNull { it.id == matchId }

    CaptainScaffold(
        title = "Match detail",
        onBack = { nav.popBackStack() },
        actions = {
            if (match != null) {
                androidx.compose.material3.IconButton(onClick = { nav.navigate(Routes.matchEdit(match.id)) }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                }
            }
        }
    ) { inner ->
        if (match == null) {
            Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                EmptyState(
                    title = "Match not found.",
                    subtitle = "It may have been deleted. Go back to see your matches.",
                    icon = Icons.AutoMirrored.Filled.PlaylistAddCheck
                )
            }
            return@CaptainScaffold
        }

        val summary = Summaries.matchSummary(data, match.id)
        val attendance = data.attendance.filter { it.matchId == match.id }

        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            BoardCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "vs ${match.opponentName.ifBlank { "Unknown opponent" }}",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    MoodBadge(match.teamMood)
                }
                Gap(8)
                KeyValueRow("Date", match.date)
                KeyValueRow("Time", match.time)
                KeyValueRow("Venue", match.venue)
                if (match.resultSummary.isNotBlank()) KeyValueRow("Result", match.resultSummary)
            }

            Gap(14)
            // Attendance summary
            BoardCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Attendance", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    StatusChip("Manage", CaptainGreen, onClick = { nav.navigate(Routes.attendance(match.id)) })
                }
                Gap(8)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip("${summary.presentCount} present", attendanceColor(AttendanceStatus.PRESENT))
                    StatusChip("${summary.absentCount} absent", attendanceColor(AttendanceStatus.ABSENT))
                    StatusChip("${summary.lateCount} late", attendanceColor(AttendanceStatus.LATE))
                }
                Gap(10)
                if (attendance.isEmpty()) {
                    Text("No attendance marked yet.", color = MutedGray, style = MaterialTheme.typography.bodyMedium)
                } else {
                    attendance.forEach { rec ->
                        val name = vm.playerName(rec.playerId)
                        Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            StatusChip(rec.attendanceStatus.label, attendanceColor(rec.attendanceStatus))
                        }
                    }
                }
            }

            Gap(14)
            // Tasks summary
            BoardCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Match tasks", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    StatusChip("Manage", CaptainBlue, onClick = { nav.navigate(Routes.tasks(match.id)) })
                }
                Gap(8)
                Text(summary.tasksLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Gap(6)
                val tasks = data.tasks.filter { it.matchId == match.id }
                if (tasks.isEmpty()) {
                    Text("No tasks yet.", color = MutedGray, style = MaterialTheme.typography.bodyMedium)
                } else {
                    tasks.take(6).forEach { t ->
                        Text(
                            (if (t.isCompleted) "✓ " else "• ") + t.title.ifBlank { "Untitled task" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (t.isCompleted) CaptainGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Gap(14)
            BoardCard {
                Text("Pre-match notes", style = MaterialTheme.typography.titleMedium)
                Gap(4)
                Text(
                    match.preMatchNotes.ifBlank { "No pre-match notes." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (match.preMatchNotes.isBlank()) MutedGray else MaterialTheme.colorScheme.onSurface
                )
                Gap(12)
                Text("Post-match notes", style = MaterialTheme.typography.titleMedium)
                Gap(4)
                Text(
                    match.postMatchNotes.ifBlank { "No post-match notes." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (match.postMatchNotes.isBlank()) MutedGray else MaterialTheme.colorScheme.onSurface
                )
            }

            Gap(18)
            Button(onClick = { nav.navigate(Routes.matchEdit(match.id)) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Edit, contentDescription = null)
                Text("  Edit match")
            }
            Gap()
            OutlinedButton(
                onClick = { nav.navigate(Routes.attendance(match.id)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null)
                Text("  Open attendance")
            }
            Gap(24)
        }
    }
}

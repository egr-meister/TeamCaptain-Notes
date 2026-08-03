package com.teamcaptain.notes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.teamcaptain.notes.data.model.AttendanceStatus
import com.teamcaptain.notes.domain.Summaries
import com.teamcaptain.notes.ui.AppViewModel
import com.teamcaptain.notes.ui.components.BoardCard
import com.teamcaptain.notes.ui.components.CaptainScaffold
import com.teamcaptain.notes.ui.components.EmptyState
import com.teamcaptain.notes.ui.components.Gap
import com.teamcaptain.notes.ui.components.StatusChip
import com.teamcaptain.notes.ui.components.attendanceColor
import com.teamcaptain.notes.ui.theme.MutedGray

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AttendanceScreen(vm: AppViewModel, nav: NavController, matchId: String?) {
    val data by vm.appData.collectAsState()
    val match = data.matches.firstOrNull { it.id == matchId }

    CaptainScaffold(title = "Attendance", onBack = { nav.popBackStack() }) { inner ->
        if (match == null) {
            Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                EmptyState("Match not found.", "Go back and pick a match.", Icons.Filled.Groups)
            }
            return@CaptainScaffold
        }

        val players = data.players.sortedWith(compareByDescending<com.teamcaptain.notes.data.model.Player> { it.isActive }
            .thenBy { it.name.lowercase() })
        val summary = Summaries.matchSummary(data, match.id)

        if (players.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                EmptyState(
                    "No players yet.",
                    "Add players first, then mark attendance here.",
                    Icons.Filled.Groups
                )
            }
            return@CaptainScaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                BoardCard {
                    Text("vs ${match.opponentName.ifBlank { "Unknown opponent" }}", style = MaterialTheme.typography.titleMedium)
                    Gap(8)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusChip("${summary.presentCount} present", attendanceColor(AttendanceStatus.PRESENT))
                        StatusChip("${summary.absentCount} absent", attendanceColor(AttendanceStatus.ABSENT))
                        StatusChip("${summary.lateCount} late", attendanceColor(AttendanceStatus.LATE))
                    }
                }
            }
            items(players, key = { it.id }) { player ->
                val record = data.attendance.firstOrNull { it.matchId == match.id && it.playerId == player.id }
                val status = record?.attendanceStatus ?: AttendanceStatus.UNKNOWN
                var noteText by remember(player.id, match.id) {
                    mutableStateOf(record?.note ?: "")
                }
                BoardCard {
                    Text(player.name.ifBlank { "Unnamed player" }, style = MaterialTheme.typography.titleMedium)
                    Text(
                        player.preferredPosition.label + (player.shirtNumber?.let { " · #$it" } ?: ""),
                        style = MaterialTheme.typography.labelMedium, color = MutedGray
                    )
                    Gap(8)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AttendanceStatus.entries.forEach { s ->
                            StatusChip(
                                text = s.label,
                                container = attendanceColor(s),
                                selected = s == status,
                                onClick = { vm.setAttendance(match.id, player.id, s) },
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                    Gap(8)
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = {
                            noteText = it
                            vm.setAttendanceNote(match.id, player.id, it)
                        },
                        label = { Text("Note (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

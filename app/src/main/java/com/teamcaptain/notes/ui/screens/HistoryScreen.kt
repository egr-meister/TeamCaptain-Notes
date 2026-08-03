package com.teamcaptain.notes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.teamcaptain.notes.data.model.MatchRecord
import com.teamcaptain.notes.domain.Summaries
import com.teamcaptain.notes.ui.AppViewModel
import com.teamcaptain.notes.ui.components.BoardCard
import com.teamcaptain.notes.ui.components.CaptainScaffold
import com.teamcaptain.notes.ui.components.EmptyState
import com.teamcaptain.notes.ui.components.Gap
import com.teamcaptain.notes.ui.components.MoodBadge
import com.teamcaptain.notes.ui.navigation.Routes
import com.teamcaptain.notes.ui.theme.ErrorRed
import com.teamcaptain.notes.ui.theme.MutedGray

@Composable
fun HistoryScreen(vm: AppViewModel, nav: NavController) {
    val data by vm.appData.collectAsState()
    val matches = Summaries.matchesNewestFirst(data)

    CaptainScaffold(
        title = "Match history",
        onBack = { nav.popBackStack() },
        floating = {
            ExtendedFloatingActionButton(
                onClick = { nav.navigate(Routes.matchEdit()) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("New match") }
            )
        }
    ) { inner ->
        if (matches.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                EmptyState("No match records yet.", "Create your first team note.", Icons.Filled.History)
            }
            return@CaptainScaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(matches, key = { it.id }) { match ->
                val summary = Summaries.matchSummary(data, match.id)
                HistoryCard(
                    match = match,
                    attendanceLabel = summary.attendanceLabel,
                    tasksLabel = summary.tasksLabel,
                    onOpen = { nav.navigate(Routes.matchDetail(match.id)) },
                    onEdit = { nav.navigate(Routes.matchEdit(match.id)) },
                    onDelete = { vm.deleteMatch(match.id) }
                )
            }
        }
    }
}

@Composable
private fun HistoryCard(
    match: MatchRecord,
    attendanceLabel: String,
    tasksLabel: String,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    BoardCard(onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
                Text("vs ${match.opponentName.ifBlank { "Unknown opponent" }}", style = MaterialTheme.typography.titleMedium)
                Text(
                    listOf(match.date, match.time, match.venue).filter { it.isNotBlank() }.joinToString(" · ")
                        .ifBlank { "No date set" },
                    style = MaterialTheme.typography.labelMedium, color = MutedGray
                )
            }
            MoodBadge(match.teamMood)
        }
        Gap(8)
        Text(attendanceLabel, style = MaterialTheme.typography.bodyMedium)
        Text(tasksLabel, style = MaterialTheme.typography.bodyMedium, color = MutedGray)
        if (match.postMatchNotes.isNotBlank()) {
            Gap(6)
            Text(
                "Post-match: ${match.postMatchNotes}",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Gap(6)
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Edit") }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = ErrorRed) }
        }
    }
}

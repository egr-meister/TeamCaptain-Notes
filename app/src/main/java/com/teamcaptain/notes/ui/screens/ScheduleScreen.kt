package com.teamcaptain.notes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.teamcaptain.notes.data.model.MatchSource
import com.teamcaptain.notes.data.model.NormalizedMatch
import com.teamcaptain.notes.ui.AppViewModel
import com.teamcaptain.notes.ui.AppText
import com.teamcaptain.notes.ui.MatchScheduleViewModel
import com.teamcaptain.notes.ui.components.BoardCard
import com.teamcaptain.notes.ui.components.CaptainScaffold
import com.teamcaptain.notes.ui.components.EmptyState
import com.teamcaptain.notes.ui.components.Gap
import com.teamcaptain.notes.ui.components.StatusChip
import com.teamcaptain.notes.ui.navigation.Routes
import com.teamcaptain.notes.ui.theme.CaptainBlue
import com.teamcaptain.notes.ui.theme.CaptainGreen
import com.teamcaptain.notes.ui.theme.InfoBlue
import com.teamcaptain.notes.ui.theme.MutedGray

@Composable
fun ScheduleScreen(
    vm: AppViewModel,
    scheduleVm: MatchScheduleViewModel,
    nav: NavController
) {
    val data by vm.appData.collectAsState()
    val state by scheduleVm.state.collectAsState()

    // Show cached/demo immediately, no network on open.
    LaunchedEffect(Unit) {
        scheduleVm.initialize(data.matchScheduleCache, data.settings.matchSchedule)
    }

    CaptainScaffold(
        title = "Match Schedule",
        onBack = { nav.popBackStack() },
        actions = {
            IconButton(onClick = { nav.navigate(Routes.SCHEDULE_SETTINGS) }) {
                Icon(Icons.Filled.Settings, contentDescription = "Schedule settings")
            }
            IconButton(
                onClick = {
                    scheduleVm.refresh(
                        settings = data.settings.matchSchedule,
                        cache = data.matchScheduleCache
                    ) { matches, error, from, to ->
                        vm.saveMatchCache(matches, error, from, to)
                    }
                }
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
            }
        }
    ) { inner ->
        Column(Modifier.fillMaxSize().padding(inner)) {
            // Header info card
            BoardCard(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Secondary reference", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    SourceChip(state.source)
                }
                Gap(6)
                val windowLabel = if (state.usingDefaultWindow) {
                    "Today + 9 days"
                } else {
                    "${state.dateFrom} → ${state.dateTo}"
                }
                Text("Window: $windowLabel", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                if (state.lastUpdatedAt.isNotBlank()) {
                    Text("Last updated: ${state.lastUpdatedAt}", style = MaterialTheme.typography.labelMedium, color = MutedGray)
                }
                Gap(6)
                Text(AppText.SCHEDULE_SHORT_NOTE, style = MaterialTheme.typography.labelMedium, color = MutedGray)
                if (state.error.isNotBlank()) {
                    Gap(8)
                    Text(state.error, style = MaterialTheme.typography.bodyMedium, color = CaptainBlue)
                }
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            if (state.matches.isEmpty() && !state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        "No matches available.",
                        "Try refreshing or check API settings.",
                        Icons.Filled.SportsSoccer
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.matches, key = { it.id.ifBlank { it.homeTeam + it.awayTeam + it.date } }) { match ->
                        MatchScheduleCard(match)
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceChip(source: MatchSource) {
    val (color, label) = when (source) {
        MatchSource.API -> CaptainGreen to "Live data"
        MatchSource.CACHE -> InfoBlue to "Cached data"
        MatchSource.DEMO -> MutedGray to "Demo data"
    }
    StatusChip(text = label, container = color)
}

@Composable
private fun MatchScheduleCard(match: NormalizedMatch) {
    BoardCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.SportsSoccer, contentDescription = null, tint = CaptainBlue, modifier = Modifier.size(20.dp))
            Column(Modifier.weight(1f).padding(start = 10.dp)) {
                Text(
                    match.competitionName.ifBlank { "Unknown competition" } +
                        (match.competitionCode.takeIf { it.isNotBlank() }?.let { " ($it)" } ?: ""),
                    style = MaterialTheme.typography.labelMedium, color = MutedGray
                )
            }
            if (match.status.isNotBlank()) {
                StatusChip(text = prettyStatus(match.status), container = InfoBlue)
            }
        }
        Gap(8)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                match.homeTeam.ifBlank { "Unknown" },
                style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f)
            )
            val score = if (match.homeScore != null || match.awayScore != null) {
                "${match.homeScore ?: "-"} : ${match.awayScore ?: "-"}"
            } else "vs"
            Text(score, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CaptainGreen)
            Text(
                match.awayTeam.ifBlank { "Unknown" },
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
        Gap(6)
        Text(
            listOf(match.date, match.time).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { "Date to be confirmed" },
            style = MaterialTheme.typography.labelMedium, color = MutedGray
        )
    }
}

private fun prettyStatus(status: String): String = when (status.uppercase()) {
    "SCHEDULED", "TIMED" -> "Scheduled"
    "IN_PLAY" -> "In play"
    "PAUSED" -> "Paused"
    "FINISHED" -> "Finished"
    "POSTPONED" -> "Postponed"
    "SUSPENDED" -> "Suspended"
    "CANCELLED", "CANCELED" -> "Cancelled"
    else -> status.lowercase().replaceFirstChar { it.uppercase() }
}

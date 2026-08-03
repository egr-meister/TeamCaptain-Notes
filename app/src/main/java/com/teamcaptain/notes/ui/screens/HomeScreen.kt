package com.teamcaptain.notes.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.teamcaptain.notes.data.model.MatchRecord
import com.teamcaptain.notes.domain.Summaries
import com.teamcaptain.notes.ui.AppViewModel
import com.teamcaptain.notes.ui.components.BoardCard
import com.teamcaptain.notes.ui.components.Gap
import com.teamcaptain.notes.ui.components.MoodBadge
import com.teamcaptain.notes.ui.components.QuickActionTile
import com.teamcaptain.notes.ui.components.StatusChip
import com.teamcaptain.notes.ui.navigation.Routes
import com.teamcaptain.notes.ui.theme.CaptainBlue
import com.teamcaptain.notes.ui.theme.CaptainBlueDeep
import com.teamcaptain.notes.ui.theme.CaptainGreen
import com.teamcaptain.notes.ui.theme.CaptainGreenDeep
import com.teamcaptain.notes.ui.theme.InfoBlue
import com.teamcaptain.notes.ui.theme.MutedGray
import com.teamcaptain.notes.util.DateUtils

@Composable
fun HomeScreen(vm: AppViewModel, nav: NavController) {
    val data by vm.appData.collectAsState()
    val today = DateUtils.today()
    val featured = Summaries.featuredMatch(data, today)
    val summary = Summaries.matchSummary(data, featured?.id)

    Column(Modifier.fillMaxSize()) {
        // --- Green-blue header band ---
        Surface(
            color = CaptainGreenDeep,
            shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(14.dp), color = CaptainBlueDeep) {
                    Icon(
                        Icons.AutoMirrored.Filled.PlaylistAddCheck, contentDescription = null,
                        tint = Color.White, modifier = Modifier.padding(10.dp).size(24.dp)
                    )
                }
                Gap(0)
                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                    Text("TeamCaptain Notes", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Text("Team captain helper", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFCFE6D9))
                }
                IconButton(onClick = { nav.navigate(Routes.SETTINGS) }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                "How is my team prepared for the next match?",
                style = MaterialTheme.typography.titleMedium
            )
            Gap(12)

            // --- Next / latest match card ---
            if (featured != null) {
                NextMatchCard(featured, today, summary, onOpen = { nav.navigate(Routes.matchDetail(featured.id)) })
            } else {
                BoardCard {
                    Text("No team records yet.", style = MaterialTheme.typography.titleMedium)
                    Gap(4)
                    Text("Create your first captain note.", color = MutedGray, style = MaterialTheme.typography.bodyMedium)
                    Gap(12)
                    StatusChip(
                        text = "+ New Match Note", container = CaptainGreen,
                        onClick = { nav.navigate(Routes.matchEdit()) }
                    )
                }
            }

            Gap(16)

            // --- Stat row: players / attendance / tasks ---
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = "${Summaries.totalPlayers(data)}",
                    label = "Players",
                    sub = "${Summaries.activePlayers(data)} active",
                    accent = CaptainBlue
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = "${summary.presentCount}",
                    label = "Present",
                    sub = "${summary.absentCount} absent · ${summary.lateCount} late",
                    accent = CaptainGreen
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = "${summary.completedTasks}/${summary.totalTasks}",
                    label = "Tasks",
                    sub = "completed",
                    accent = InfoBlue
                )
            }

            Gap(16)
            Text("Quick actions", style = MaterialTheme.typography.titleMedium)
            Gap(10)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                QuickActionTile("New Match Note", Icons.Filled.NoteAdd, CaptainGreen, Modifier.weight(1f)) {
                    nav.navigate(Routes.matchEdit())
                }
                QuickActionTile("Players", Icons.Filled.Groups, CaptainBlue, Modifier.weight(1f)) {
                    nav.navigate(Routes.PLAYERS)
                }
            }
            Gap(12)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                QuickActionTile("Attendance", Icons.Filled.CheckCircle, CaptainGreen, Modifier.weight(1f)) {
                    val id = featured?.id
                    if (id != null) nav.navigate(Routes.attendance(id)) else nav.navigate(Routes.matchEdit())
                }
                QuickActionTile("Tasks", Icons.AutoMirrored.Filled.PlaylistAddCheck, CaptainBlue, Modifier.weight(1f)) {
                    val id = featured?.id
                    if (id != null) nav.navigate(Routes.tasks(id)) else nav.navigate(Routes.matchEdit())
                }
            }
            Gap(12)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                QuickActionTile("History", Icons.Filled.History, InfoBlue, Modifier.weight(1f)) {
                    nav.navigate(Routes.HISTORY)
                }
                // spacer tile
                Box(Modifier.weight(1f))
            }

            Gap(18)
            // --- Secondary Match Schedule card (kept visually small) ---
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { nav.navigate(Routes.SCHEDULE) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.SportsSoccer, contentDescription = null, tint = CaptainBlueDeep)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text("Match Schedule", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Extra reference · football-data.org",
                            style = MaterialTheme.typography.labelMedium, color = MutedGray
                        )
                    }
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = CaptainBlue)
                }
            }
            Gap(24)
        }
    }
}

@Composable
private fun NextMatchCard(
    match: MatchRecord,
    today: String,
    summary: com.teamcaptain.notes.domain.MatchSummary,
    onOpen: () -> Unit
) {
    val isUpcoming = match.date.isNotBlank() && match.date >= today
    BoardCard(onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusChip(
                text = if (isUpcoming) "Next match" else "Latest match",
                container = if (isUpcoming) CaptainGreen else CaptainBlue
            )
            Box(Modifier.weight(1f))
            MoodBadge(match.teamMood)
        }
        Gap(10)
        Text(
            "vs ${match.opponentName.ifBlank { "Unknown opponent" }}",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            listOf(match.date, match.time, match.venue).filter { it.isNotBlank() }.joinToString(" · ")
                .ifBlank { "No date set" },
            style = MaterialTheme.typography.bodyMedium, color = MutedGray
        )
        Gap(10)
        Text(summary.attendanceLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Text(summary.tasksLabel, style = MaterialTheme.typography.bodyMedium, color = MutedGray)
        Gap(6)
        Text("Tap to open match details", style = MaterialTheme.typography.labelMedium, color = MutedGray)
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    sub: String,
    accent: Color
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier,
        shadowElevation = 1.dp
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = accent)
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(sub, style = MaterialTheme.typography.labelMedium, color = MutedGray)
        }
    }
}

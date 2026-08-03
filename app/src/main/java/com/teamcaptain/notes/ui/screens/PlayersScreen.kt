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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.teamcaptain.notes.data.model.Player
import com.teamcaptain.notes.ui.AppViewModel
import com.teamcaptain.notes.ui.components.BoardCard
import com.teamcaptain.notes.ui.components.CaptainScaffold
import com.teamcaptain.notes.ui.components.EmptyState
import com.teamcaptain.notes.ui.components.Gap
import com.teamcaptain.notes.ui.navigation.Routes
import com.teamcaptain.notes.ui.theme.CaptainBlue
import com.teamcaptain.notes.ui.theme.CaptainGreen
import com.teamcaptain.notes.ui.theme.MutedGray
import com.teamcaptain.notes.ui.theme.SuccessGreen

@Composable
fun PlayersScreen(vm: AppViewModel, nav: NavController) {
    val data by vm.appData.collectAsState()
    var sortByNumber by remember { mutableStateOf(false) }

    val players = remember(data.players, sortByNumber) {
        if (sortByNumber) {
            data.players.sortedWith(compareBy({ it.shirtNumber ?: Int.MAX_VALUE }, { it.name.lowercase() }))
        } else {
            data.players.sortedBy { it.name.lowercase() }
        }
    }

    CaptainScaffold(
        title = "Players",
        onBack = { nav.popBackStack() },
        actions = {
            IconButton(onClick = { sortByNumber = !sortByNumber }) {
                Icon(Icons.Filled.SwapVert, contentDescription = "Toggle sort")
            }
        },
        floating = {
            ExtendedFloatingActionButton(
                onClick = { nav.navigate(Routes.playerEdit()) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Add player") }
            )
        }
    ) { inner ->
        if (players.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                EmptyState(
                    title = "No players yet.",
                    subtitle = "Add your first player.",
                    icon = Icons.Filled.Groups
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(inner),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Sorted by ${if (sortByNumber) "shirt number" else "name"} · ${players.size} players",
                        style = MaterialTheme.typography.labelMedium, color = MutedGray
                    )
                }
                items(players, key = { it.id }) { player ->
                    PlayerCard(
                        player = player,
                        onEdit = { nav.navigate(Routes.playerEdit(player.id)) },
                        onToggleActive = { vm.setPlayerActive(player.id, it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerCard(
    player: Player,
    onEdit: () -> Unit,
    onToggleActive: (Boolean) -> Unit
) {
    BoardCard(onClick = onEdit) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = CaptainGreen, modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        player.shirtNumber?.toString() ?: "–",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(player.name.ifBlank { "Unnamed player" }, style = MaterialTheme.typography.titleMedium)
                Text(player.preferredPosition.label, style = MaterialTheme.typography.bodyMedium, color = CaptainBlue)
                if (player.captainNote.isNotBlank()) {
                    Text(
                        player.captainNote,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedGray,
                        maxLines = 1
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Switch(checked = player.isActive, onCheckedChange = onToggleActive)
                Text(
                    if (player.isActive) "Active" else "Inactive",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (player.isActive) SuccessGreen else MutedGray
                )
            }
        }
    }
}

package com.teamcaptain.notes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.teamcaptain.notes.data.model.CaptainTask
import com.teamcaptain.notes.ui.AppViewModel
import com.teamcaptain.notes.ui.components.BoardCard
import com.teamcaptain.notes.ui.components.CaptainScaffold
import com.teamcaptain.notes.ui.components.EmptyState
import com.teamcaptain.notes.ui.components.Gap
import com.teamcaptain.notes.ui.components.StatusChip
import com.teamcaptain.notes.ui.theme.CaptainBlue
import com.teamcaptain.notes.ui.theme.CaptainGreen
import com.teamcaptain.notes.ui.theme.ErrorRed
import com.teamcaptain.notes.ui.theme.MutedGray

private val TASK_SUGGESTIONS = listOf(
    "Bring captain armband", "Bring match ball", "Bring water",
    "Warm-up leader", "Corner routine reminder", "Defensive focus", "Locker room note"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TasksScreen(vm: AppViewModel, nav: NavController, matchId: String?) {
    val data by vm.appData.collectAsState()
    val match = data.matches.firstOrNull { it.id == matchId }

    var editing by remember { mutableStateOf<CaptainTask?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    CaptainScaffold(
        title = "Match tasks",
        onBack = { nav.popBackStack() },
        floating = {
            if (match != null) {
                ExtendedFloatingActionButton(
                    onClick = { editing = null; showDialog = true },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Add task") }
                )
            }
        }
    ) { inner ->
        if (match == null) {
            Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                EmptyState("Match not found.", "Go back and pick a match.", Icons.AutoMirrored.Filled.PlaylistAddCheck)
            }
            return@CaptainScaffold
        }

        val tasks = data.tasks.filter { it.matchId == match.id }

        if (tasks.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                EmptyState("No tasks yet.", "Add your first match task.", Icons.AutoMirrored.Filled.PlaylistAddCheck)
            }
        } else {
            val done = tasks.count { it.isCompleted }
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(inner),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        "$done / ${tasks.size} tasks completed",
                        style = MaterialTheme.typography.labelLarge, color = CaptainGreen
                    )
                }
                items(tasks, key = { it.id }) { task ->
                    TaskRow(
                        task = task,
                        assignee = task.assignedPlayerId?.let { vm.playerName(it) }?.takeIf { it != "Unassigned" },
                        onToggle = { vm.toggleTask(task.id, it) },
                        onEdit = { editing = task; showDialog = true },
                        onDelete = { vm.deleteTask(task.id) }
                    )
                }
            }
        }
    }

    if (showDialog && match != null) {
        TaskDialog(
            vm = vm,
            matchId = match.id,
            existing = editing,
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun TaskRow(
    task: CaptainTask,
    assignee: String?,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    BoardCard(onClick = onEdit) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.isCompleted, onCheckedChange = onToggle)
            Column(Modifier.weight(1f).padding(start = 4.dp)) {
                Text(
                    task.title.ifBlank { "Untitled task" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    color = if (task.isCompleted) MutedGray else MaterialTheme.colorScheme.onSurface
                )
                if (task.description.isNotBlank()) {
                    Text(task.description, style = MaterialTheme.typography.bodyMedium, color = MutedGray)
                }
                if (assignee != null) {
                    Gap(4)
                    StatusChip("Assigned: $assignee", CaptainBlue)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete task", tint = ErrorRed)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaskDialog(
    vm: AppViewModel,
    matchId: String,
    existing: CaptainTask?,
    onDismiss: () -> Unit
) {
    val data by vm.appData.collectAsState()
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var assignedId by remember { mutableStateOf(existing?.assignedPlayerId) }
    var titleError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add task" else "Edit task") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; titleError = null },
                    label = { Text("Title") },
                    singleLine = true,
                    isError = titleError != null,
                    supportingText = { titleError?.let { Text(it, color = ErrorRed) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Gap(8)
                if (existing == null) {
                    Text("Quick suggestions", style = MaterialTheme.typography.labelMedium, color = MutedGray)
                    Gap(4)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TASK_SUGGESTIONS.forEach { s ->
                            StatusChip(
                                text = s, container = CaptainGreen,
                                selected = false, onClick = { title = s },
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                    Gap(8)
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                Gap(8)
                Text("Assign to (optional)", style = MaterialTheme.typography.labelMedium, color = MutedGray)
                Gap(4)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.heightIn(max = 160.dp).verticalScroll(rememberScrollState())
                ) {
                    StatusChip(
                        text = "Unassigned", container = CaptainBlue,
                        selected = assignedId == null, onClick = { assignedId = null },
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                    data.players.forEach { p ->
                        StatusChip(
                            text = p.name.ifBlank { "Unnamed" }, container = CaptainBlue,
                            selected = assignedId == p.id, onClick = { assignedId = p.id },
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isBlank()) { titleError = "Title is required."; return@TextButton }
                vm.upsertTask(
                    existingId = existing?.id,
                    matchId = matchId,
                    title = title,
                    description = description,
                    assignedPlayerId = assignedId,
                    isCompleted = existing?.isCompleted ?: false
                )
                onDismiss()
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

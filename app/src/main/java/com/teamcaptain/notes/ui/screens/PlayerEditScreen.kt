package com.teamcaptain.notes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.teamcaptain.notes.data.model.PreferredPosition
import com.teamcaptain.notes.ui.AppViewModel
import com.teamcaptain.notes.ui.components.CaptainScaffold
import com.teamcaptain.notes.ui.components.Gap
import com.teamcaptain.notes.ui.components.StatusChip
import com.teamcaptain.notes.ui.theme.CaptainBlue
import com.teamcaptain.notes.ui.theme.ErrorRed
import com.teamcaptain.notes.ui.theme.MutedGray
import com.teamcaptain.notes.util.Validation

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayerEditScreen(vm: AppViewModel, nav: NavController, playerId: String?) {
    val existing = remember(playerId) { vm.player(playerId) }

    var name by remember { mutableStateOf(existing?.name ?: "") }
    var shirt by remember { mutableStateOf(existing?.shirtNumber?.toString() ?: "") }
    var position by remember { mutableStateOf(existing?.preferredPosition ?: PreferredPosition.UNKNOWN) }
    var note by remember { mutableStateOf(existing?.captainNote ?: "") }
    var active by remember { mutableStateOf(existing?.isActive ?: true) }

    var nameError by remember { mutableStateOf<String?>(null) }
    val shirtError = Validation.shirtNumberError(shirt)

    CaptainScaffold(
        title = if (existing == null) "Add player" else "Edit player",
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
                value = name,
                onValueChange = { name = it; nameError = null },
                label = { Text("Player name") },
                singleLine = true,
                isError = nameError != null,
                supportingText = { nameError?.let { Text(it, color = ErrorRed) } },
                modifier = Modifier.fillMaxWidth()
            )
            Gap()
            OutlinedTextField(
                value = shirt,
                onValueChange = { new -> shirt = new.filter { it.isDigit() }.take(2) },
                label = { Text("Shirt number (1-99, optional)") },
                singleLine = true,
                isError = shirtError != null,
                supportingText = { shirtError?.let { Text(it, color = ErrorRed) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Gap()
            Text("Preferred position", style = MaterialTheme.typography.labelLarge)
            Gap(6)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PreferredPosition.entries.forEach { pos ->
                    StatusChip(
                        text = pos.label,
                        container = CaptainBlue,
                        selected = pos == position,
                        onClick = { position = pos },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
            Gap()
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Captain note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Gap()
            androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = active, onCheckedChange = { active = it })
                Text(
                    if (active) "  Active player" else "  Inactive player",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Gap(20)
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = "Player name is required."
                        return@Button
                    }
                    if (shirtError != null) return@Button
                    vm.upsertPlayer(
                        existingId = existing?.id,
                        name = name,
                        shirtNumber = shirt.toIntOrNull(),
                        position = position,
                        captainNote = note,
                        isActive = active
                    )
                    nav.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (existing == null) "Save player" else "Save changes") }

            if (existing != null) {
                Gap()
                OutlinedButton(
                    onClick = { vm.deletePlayer(existing.id); nav.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = ErrorRed)
                    Text("  Delete player", color = ErrorRed)
                }
            }
            Gap(8)
            Text(
                "Player details are stored locally on this device only.",
                style = MaterialTheme.typography.labelMedium, color = MutedGray
            )
            Gap(24)
        }
    }
}

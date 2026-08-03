package com.teamcaptain.notes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teamcaptain.notes.ui.AppText
import com.teamcaptain.notes.ui.components.BoardCard
import com.teamcaptain.notes.ui.components.Gap
import com.teamcaptain.notes.ui.theme.CaptainBlue
import com.teamcaptain.notes.ui.theme.CaptainGreen
import com.teamcaptain.notes.ui.theme.CaptainGreenDeep
import com.teamcaptain.notes.ui.theme.MutedGray

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Gap(16)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CaptainGreenDeep,
            modifier = Modifier.size(72.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.PlaylistAddCheck, contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.padding(16.dp)
            )
        }
        Gap(16)
        Text("TeamCaptain Notes", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Organize your team before the match.",
            style = MaterialTheme.typography.bodyLarge, color = MutedGray
        )
        Gap(20)

        FeatureRow(Icons.Filled.Groups, "Players", "Build and manage your player list.")
        FeatureRow(Icons.Filled.CheckCircle, "Attendance", "Mark who is present, absent, or late.")
        FeatureRow(Icons.AutoMirrored.Filled.PlaylistAddCheck, "Match tasks", "Track a pre-match checklist.")
        FeatureRow(Icons.Filled.Notes, "Team notes", "Keep pre- and post-match notes.")
        FeatureRow(Icons.Filled.Mood, "Team mood", "Record how the team feels.")
        FeatureRow(Icons.Filled.SportsSoccer, "Match Schedule", "View football matches as an extra reference.")

        Gap(16)
        BoardCard {
            Text("Good to know", style = MaterialTheme.typography.titleMedium)
            Gap(6)
            Text(
                "No account. No ads. No betting. No official logos.",
                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium
            )
            Gap(10)
            Text(AppText.CAPTAIN_DISCLAIMER, style = MaterialTheme.typography.bodyMedium, color = MutedGray)
            Gap(10)
            Text(AppText.SCHEDULE_DISCLAIMER, style = MaterialTheme.typography.bodyMedium, color = MutedGray)
            Gap(10)
            Text(
                "Your team data is stored locally on this device only.",
                style = MaterialTheme.typography.bodyMedium, color = MutedGray
            )
        }

        Gap(20)
        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Start Organizing") }
        TextButton(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Skip") }
        Gap(24)
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, title: String, subtitle: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(shape = RoundedCornerShape(12.dp), color = CaptainGreen.copy(alpha = 0.12f)) {
            Icon(icon, contentDescription = null, tint = CaptainBlue, modifier = Modifier.padding(8.dp).size(22.dp))
        }
        Spacer(Modifier.size(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MutedGray)
        }
    }
}

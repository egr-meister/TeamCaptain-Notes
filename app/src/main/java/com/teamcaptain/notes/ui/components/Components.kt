package com.teamcaptain.notes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.teamcaptain.notes.data.model.AttendanceStatus
import com.teamcaptain.notes.data.model.TeamMood
import com.teamcaptain.notes.ui.theme.CaptainBlue
import com.teamcaptain.notes.ui.theme.CaptainBlueDeep
import com.teamcaptain.notes.ui.theme.CaptainGreen
import com.teamcaptain.notes.ui.theme.CaptainGreenDeep
import com.teamcaptain.notes.ui.theme.ErrorRed
import com.teamcaptain.notes.ui.theme.InfoBlue
import com.teamcaptain.notes.ui.theme.MutedGray
import com.teamcaptain.notes.ui.theme.SuccessGreen
import com.teamcaptain.notes.ui.theme.WarningYellow

/** White content card with soft elevation — the primary surface of the board. */
@Composable
fun BoardCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val base = modifier.fillMaxWidth()
    Card(
        modifier = if (onClick != null) base.clickable { onClick() } else base,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) { content() }
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MutedGray,
        modifier = modifier.padding(start = 4.dp, bottom = 6.dp)
    )
}

/** Small rounded status chip used for attendance, mood, source, etc. */
@Composable
fun StatusChip(
    text: String,
    container: Color,
    contentColor: Color = Color.White,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    selected: Boolean = true
) {
    val shape = RoundedCornerShape(50)
    val base = modifier
        .background(if (selected) container else Color.Transparent, shape)
        .border(1.dp, container, shape)
    Box(
        modifier = if (onClick != null) base.clickable { onClick() } else base
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) contentColor else container,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

fun moodColor(mood: TeamMood): Color = when (mood) {
    TeamMood.GREAT -> SuccessGreen
    TeamMood.GOOD -> CaptainGreen
    TeamMood.NEUTRAL -> InfoBlue
    TeamMood.LOW -> WarningYellow
    TeamMood.TENSE -> ErrorRed
}

fun attendanceColor(status: AttendanceStatus): Color = when (status) {
    AttendanceStatus.PRESENT -> SuccessGreen
    AttendanceStatus.ABSENT -> ErrorRed
    AttendanceStatus.LATE -> WarningYellow
    AttendanceStatus.UNKNOWN -> MutedGray
}

@Composable
fun MoodBadge(mood: TeamMood, modifier: Modifier = Modifier) {
    val c = moodColor(mood)
    StatusChip(
        text = "Mood: ${mood.label}",
        container = c,
        contentColor = if (mood == TeamMood.LOW) CaptainGreenDeep else Color.White,
        modifier = modifier
    )
}

/** Quick action tile for the Home board (icon + label). */
@Composable
fun QuickActionTile(
    label: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(shape = RoundedCornerShape(12.dp), color = accent.copy(alpha = 0.14f)) {
                Icon(
                    icon, contentDescription = null, tint = accent,
                    modifier = Modifier.padding(8.dp).size(22.dp)
                )
            }
            Text(label, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/** Friendly empty-state block used across list screens. */
@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MutedGray, modifier = Modifier.size(40.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(
            subtitle, style = MaterialTheme.typography.bodyMedium,
            color = MutedGray, overflow = TextOverflow.Ellipsis
        )
    }
}

/** Two-tone rounded gradient-like header band (green -> blue) used at screen tops. */
@Composable
fun CaptainHeaderBand(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        color = CaptainGreenDeep,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(Modifier.fillMaxWidth()) {
            Box(Modifier.weight(1f).background(CaptainGreenDeep)) { content() }
            Box(Modifier.width(10.dp).background(CaptainBlueDeep).height(1.dp))
        }
    }
}

@Composable
fun KeyValueRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MutedGray)
        Text(
            value.ifBlank { "—" },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** Convenience vertical gap. */
@Composable
fun Gap(height: Int = 12) = Spacer(Modifier.height(height.dp))

/** Convenience horizontal gap. */
@Composable
fun HGap(width: Int = 8) = Spacer(Modifier.width(width.dp))

val ScreenPadding = PaddingValues(16.dp)

// Re-export accent colors for screen convenience.
val AccentGreen = CaptainGreen
val AccentBlue = CaptainBlue

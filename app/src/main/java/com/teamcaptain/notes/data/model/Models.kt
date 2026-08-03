package com.teamcaptain.notes.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * All persisted domain models for TeamCaptain Notes.
 *
 * Every model is a plain [Serializable] data class stored as JSON inside
 * DataStore Preferences. Enums declare an [UNKNOWN]-style fallback so that a
 * corrupted or forward-incompatible value can never crash deserialization
 * (see the `safe*` helpers).
 */

@Serializable
enum class PreferredPosition {
    @SerialName("Goalkeeper") GOALKEEPER,
    @SerialName("Defender") DEFENDER,
    @SerialName("Midfielder") MIDFIELDER,
    @SerialName("Forward") FORWARD,
    @SerialName("Utility") UTILITY,
    @SerialName("Unknown") UNKNOWN;

    val label: String
        get() = when (this) {
            GOALKEEPER -> "Goalkeeper"
            DEFENDER -> "Defender"
            MIDFIELDER -> "Midfielder"
            FORWARD -> "Forward"
            UTILITY -> "Utility"
            UNKNOWN -> "Unknown"
        }

    companion object {
        fun safe(name: String?): PreferredPosition =
            entries.firstOrNull { it.name.equals(name, true) || it.label.equals(name, true) }
                ?: UNKNOWN
    }
}

@Serializable
enum class TeamMood {
    @SerialName("Great") GREAT,
    @SerialName("Good") GOOD,
    @SerialName("Neutral") NEUTRAL,
    @SerialName("Low") LOW,
    @SerialName("Tense") TENSE;

    val label: String
        get() = when (this) {
            GREAT -> "Great"
            GOOD -> "Good"
            NEUTRAL -> "Neutral"
            LOW -> "Low"
            TENSE -> "Tense"
        }

    companion object {
        fun safe(name: String?): TeamMood =
            entries.firstOrNull { it.name.equals(name, true) || it.label.equals(name, true) }
                ?: NEUTRAL
    }
}

@Serializable
enum class AttendanceStatus {
    @SerialName("Present") PRESENT,
    @SerialName("Absent") ABSENT,
    @SerialName("Late") LATE,
    @SerialName("Unknown") UNKNOWN;

    val label: String
        get() = when (this) {
            PRESENT -> "Present"
            ABSENT -> "Absent"
            LATE -> "Late"
            UNKNOWN -> "Unknown"
        }

    companion object {
        fun safe(name: String?): AttendanceStatus =
            entries.firstOrNull { it.name.equals(name, true) || it.label.equals(name, true) }
                ?: UNKNOWN
    }
}

@Serializable
enum class MatchSource {
    @SerialName("Api") API,
    @SerialName("Cache") CACHE,
    @SerialName("Demo") DEMO;

    val label: String
        get() = when (this) {
            API -> "Live"
            CACHE -> "Cached"
            DEMO -> "Demo"
        }
}

@Serializable
data class Player(
    val id: String,
    val name: String,
    val shirtNumber: Int? = null,
    val preferredPosition: PreferredPosition = PreferredPosition.UNKNOWN,
    val captainNote: String = "",
    val isActive: Boolean = true,
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Serializable
data class MatchRecord(
    val id: String,
    val date: String = "",
    val time: String = "",
    val opponentName: String = "",
    val venue: String = "",
    val teamMood: TeamMood = TeamMood.NEUTRAL,
    val preMatchNotes: String = "",
    val postMatchNotes: String = "",
    val resultSummary: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Serializable
data class AttendanceRecord(
    val id: String,
    val matchId: String,
    val playerId: String,
    val attendanceStatus: AttendanceStatus = AttendanceStatus.UNKNOWN,
    val note: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Serializable
data class CaptainTask(
    val id: String,
    val matchId: String,
    val title: String = "",
    val description: String = "",
    val assignedPlayerId: String? = null,
    val isCompleted: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Serializable
data class NormalizedMatch(
    val id: String = "",
    val utcDate: String = "",
    val date: String = "",
    val time: String = "",
    val competitionName: String = "",
    val competitionCode: String = "",
    val homeTeam: String = "",
    val awayTeam: String = "",
    val status: String = "",
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val winner: String = "",
    val source: MatchSource = MatchSource.DEMO
)

@Serializable
data class MatchScheduleSettings(
    val apiEnabled: Boolean = true,
    val useDemoData: Boolean = false,
    val dateFrom: String = "",
    val dateTo: String = "",
    val competitionCode: String = ""
)

@Serializable
data class Settings(
    val onboardingCompleted: Boolean = false,
    val compactMode: Boolean = false,
    val defaultMood: TeamMood? = null,
    val matchSchedule: MatchScheduleSettings = MatchScheduleSettings()
)

@Serializable
data class MatchScheduleCache(
    val cachedMatches: List<NormalizedMatch> = emptyList(),
    val lastUpdatedAt: String = "",
    val lastError: String = "",
    val lastDateFrom: String = "",
    val lastDateTo: String = ""
)

/**
 * The single root object persisted to DataStore. Every field has a default so
 * that missing fields in stored JSON are filled in on load (forward/backward
 * compatible), and a totally empty/corrupt store falls back to this default.
 */
@Serializable
data class AppData(
    val players: List<Player> = emptyList(),
    val matches: List<MatchRecord> = emptyList(),
    val attendance: List<AttendanceRecord> = emptyList(),
    val tasks: List<CaptainTask> = emptyList(),
    val settings: Settings = Settings(),
    val matchScheduleCache: MatchScheduleCache = MatchScheduleCache()
)

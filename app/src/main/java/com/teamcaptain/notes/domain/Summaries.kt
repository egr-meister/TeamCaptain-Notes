package com.teamcaptain.notes.domain

import com.teamcaptain.notes.data.model.AppData
import com.teamcaptain.notes.data.model.AttendanceStatus
import com.teamcaptain.notes.data.model.MatchRecord
import com.teamcaptain.notes.data.model.TeamMood

/** Immutable per-match roll-up used across Home, History and detail screens. */
data class MatchSummary(
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val lateCount: Int = 0,
    val unknownCount: Int = 0,
    val completedTasks: Int = 0,
    val totalTasks: Int = 0
) {
    val attendanceLabel: String
        get() = "$presentCount present · $absentCount absent · $lateCount late"

    val tasksLabel: String
        get() = "$completedTasks / $totalTasks tasks done"
}

/**
 * Pure, null-safe summary helpers. Every function tolerates empty lists,
 * missing references and zero totals without throwing.
 */
object Summaries {

    fun totalPlayers(data: AppData): Int = data.players.size

    fun activePlayers(data: AppData): Int = data.players.count { it.isActive }

    fun matchSummary(data: AppData, matchId: String?): MatchSummary {
        if (matchId.isNullOrBlank()) return MatchSummary()
        val att = data.attendance.filter { it.matchId == matchId }
        val tasks = data.tasks.filter { it.matchId == matchId }
        return MatchSummary(
            presentCount = att.count { it.attendanceStatus == AttendanceStatus.PRESENT },
            absentCount = att.count { it.attendanceStatus == AttendanceStatus.ABSENT },
            lateCount = att.count { it.attendanceStatus == AttendanceStatus.LATE },
            unknownCount = att.count { it.attendanceStatus == AttendanceStatus.UNKNOWN },
            completedTasks = tasks.count { it.isCompleted },
            totalTasks = tasks.size
        )
    }

    /** Matches sorted newest first by (date, time), tolerating blank/invalid values. */
    fun matchesNewestFirst(data: AppData): List<MatchRecord> =
        data.matches.sortedWith(
            compareByDescending<MatchRecord> { it.date.ifBlank { "0000-00-00" } }
                .thenByDescending { it.time.ifBlank { "00:00" } }
                .thenByDescending { it.createdAt }
        )

    /** The most relevant match to feature on Home: nearest upcoming, else latest. */
    fun featuredMatch(data: AppData, today: String): MatchRecord? {
        if (data.matches.isEmpty()) return null
        val upcoming = data.matches
            .filter { it.date.isNotBlank() && it.date >= today }
            .sortedWith(compareBy({ it.date }, { it.time }))
        return upcoming.firstOrNull() ?: matchesNewestFirst(data).firstOrNull()
    }

    fun latestMood(data: AppData, today: String): TeamMood? =
        featuredMatch(data, today)?.teamMood
}

package com.teamcaptain.notes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.teamcaptain.notes.data.local.LocalRepository
import com.teamcaptain.notes.data.model.AppData
import com.teamcaptain.notes.data.model.AttendanceRecord
import com.teamcaptain.notes.data.model.AttendanceStatus
import com.teamcaptain.notes.data.model.CaptainTask
import com.teamcaptain.notes.data.model.MatchRecord
import com.teamcaptain.notes.data.model.MatchScheduleCache
import com.teamcaptain.notes.data.model.MatchScheduleSettings
import com.teamcaptain.notes.data.model.NormalizedMatch
import com.teamcaptain.notes.data.model.Player
import com.teamcaptain.notes.data.model.PreferredPosition
import com.teamcaptain.notes.data.model.Settings
import com.teamcaptain.notes.data.model.TeamMood
import com.teamcaptain.notes.util.DateUtils
import com.teamcaptain.notes.util.Ids
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Central ViewModel holding all local app data and exposing safe CRUD
 * operations. One shared instance is used by every screen so that edits are
 * immediately reflected everywhere.
 */
class AppViewModel(private val repo: LocalRepository) : ViewModel() {

    private val _isReady = MutableStateFlow(false)

    /** Becomes true after the first real read from disk completes. */
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    val appData: StateFlow<AppData> = repo.appData
        .onEach { _isReady.value = true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppData())

    private fun now() = DateUtils.nowIsoTimestamp()

    // ---------------- Lookups (null-safe) ----------------

    fun player(id: String?): Player? = appData.value.players.firstOrNull { it.id == id }
    fun match(id: String?): MatchRecord? = appData.value.matches.firstOrNull { it.id == id }
    fun task(id: String?): CaptainTask? = appData.value.tasks.firstOrNull { it.id == id }

    fun playerName(id: String?): String =
        if (id.isNullOrBlank()) "Unassigned" else player(id)?.name ?: "Unassigned"

    // ---------------- Players ----------------

    fun upsertPlayer(
        existingId: String?,
        name: String,
        shirtNumber: Int?,
        position: PreferredPosition,
        captainNote: String,
        isActive: Boolean
    ) = viewModelScope.launch {
        repo.update { data ->
            val ts = now()
            val list = data.players.toMutableList()
            val idx = list.indexOfFirst { it.id == existingId }
            if (idx >= 0) {
                list[idx] = list[idx].copy(
                    name = name.trim(),
                    shirtNumber = shirtNumber,
                    preferredPosition = position,
                    captainNote = captainNote.trim(),
                    isActive = isActive,
                    updatedAt = ts
                )
            } else {
                list.add(
                    Player(
                        id = Ids.newId(),
                        name = name.trim(),
                        shirtNumber = shirtNumber,
                        preferredPosition = position,
                        captainNote = captainNote.trim(),
                        isActive = isActive,
                        createdAt = ts,
                        updatedAt = ts
                    )
                )
            }
            data.copy(players = list)
        }
    }

    fun deletePlayer(id: String) = viewModelScope.launch {
        repo.update { data ->
            data.copy(
                players = data.players.filterNot { it.id == id },
                attendance = data.attendance.filterNot { it.playerId == id },
                tasks = data.tasks.map { if (it.assignedPlayerId == id) it.copy(assignedPlayerId = null) else it }
            )
        }
    }

    fun setPlayerActive(id: String, active: Boolean) = viewModelScope.launch {
        repo.update { data ->
            data.copy(players = data.players.map {
                if (it.id == id) it.copy(isActive = active, updatedAt = now()) else it
            })
        }
    }

    // ---------------- Matches ----------------

    fun upsertMatch(
        existingId: String?,
        date: String,
        time: String,
        opponentName: String,
        venue: String,
        teamMood: TeamMood,
        preMatchNotes: String,
        postMatchNotes: String,
        resultSummary: String
    ): String {
        val newId = existingId ?: Ids.newId()
        viewModelScope.launch {
            repo.update { data ->
                val ts = now()
                val list = data.matches.toMutableList()
                val idx = list.indexOfFirst { it.id == existingId }
                if (idx >= 0) {
                    list[idx] = list[idx].copy(
                        date = date.trim(), time = time.trim(),
                        opponentName = opponentName.trim(), venue = venue.trim(),
                        teamMood = teamMood, preMatchNotes = preMatchNotes.trim(),
                        postMatchNotes = postMatchNotes.trim(), resultSummary = resultSummary.trim(),
                        updatedAt = ts
                    )
                } else {
                    list.add(
                        MatchRecord(
                            id = newId, date = date.trim(), time = time.trim(),
                            opponentName = opponentName.trim(), venue = venue.trim(),
                            teamMood = teamMood, preMatchNotes = preMatchNotes.trim(),
                            postMatchNotes = postMatchNotes.trim(), resultSummary = resultSummary.trim(),
                            createdAt = ts, updatedAt = ts
                        )
                    )
                }
                data.copy(matches = list)
            }
        }
        return newId
    }

    fun deleteMatch(id: String) = viewModelScope.launch {
        repo.update { data ->
            data.copy(
                matches = data.matches.filterNot { it.id == id },
                attendance = data.attendance.filterNot { it.matchId == id },
                tasks = data.tasks.filterNot { it.matchId == id }
            )
        }
    }

    // ---------------- Attendance ----------------

    fun setAttendance(matchId: String, playerId: String, status: AttendanceStatus) =
        viewModelScope.launch {
            repo.update { data ->
                val ts = now()
                val list = data.attendance.toMutableList()
                val idx = list.indexOfFirst { it.matchId == matchId && it.playerId == playerId }
                if (idx >= 0) {
                    list[idx] = list[idx].copy(attendanceStatus = status, updatedAt = ts)
                } else {
                    list.add(
                        AttendanceRecord(
                            id = Ids.newId(), matchId = matchId, playerId = playerId,
                            attendanceStatus = status, note = "", createdAt = ts, updatedAt = ts
                        )
                    )
                }
                data.copy(attendance = list)
            }
        }

    fun setAttendanceNote(matchId: String, playerId: String, note: String) =
        viewModelScope.launch {
            repo.update { data ->
                val ts = now()
                val list = data.attendance.toMutableList()
                val idx = list.indexOfFirst { it.matchId == matchId && it.playerId == playerId }
                if (idx >= 0) {
                    list[idx] = list[idx].copy(note = note, updatedAt = ts)
                } else {
                    list.add(
                        AttendanceRecord(
                            id = Ids.newId(), matchId = matchId, playerId = playerId,
                            attendanceStatus = AttendanceStatus.UNKNOWN, note = note,
                            createdAt = ts, updatedAt = ts
                        )
                    )
                }
                data.copy(attendance = list)
            }
        }

    fun attendanceFor(matchId: String?, playerId: String?): AttendanceRecord? =
        appData.value.attendance.firstOrNull { it.matchId == matchId && it.playerId == playerId }

    // ---------------- Tasks ----------------

    fun upsertTask(
        existingId: String?,
        matchId: String,
        title: String,
        description: String,
        assignedPlayerId: String?,
        isCompleted: Boolean
    ) = viewModelScope.launch {
        repo.update { data ->
            val ts = now()
            val list = data.tasks.toMutableList()
            val idx = list.indexOfFirst { it.id == existingId }
            if (idx >= 0) {
                list[idx] = list[idx].copy(
                    title = title.trim(), description = description.trim(),
                    assignedPlayerId = assignedPlayerId, isCompleted = isCompleted, updatedAt = ts
                )
            } else {
                list.add(
                    CaptainTask(
                        id = Ids.newId(), matchId = matchId, title = title.trim(),
                        description = description.trim(), assignedPlayerId = assignedPlayerId,
                        isCompleted = isCompleted, createdAt = ts, updatedAt = ts
                    )
                )
            }
            data.copy(tasks = list)
        }
    }

    fun toggleTask(id: String, completed: Boolean) = viewModelScope.launch {
        repo.update { data ->
            data.copy(tasks = data.tasks.map {
                if (it.id == id) it.copy(isCompleted = completed, updatedAt = now()) else it
            })
        }
    }

    fun deleteTask(id: String) = viewModelScope.launch {
        repo.update { data -> data.copy(tasks = data.tasks.filterNot { it.id == id }) }
    }

    // ---------------- Settings ----------------

    fun completeOnboarding() = updateSettings { it.copy(onboardingCompleted = true) }
    fun showOnboardingAgain() = updateSettings { it.copy(onboardingCompleted = false) }
    fun setCompactMode(on: Boolean) = updateSettings { it.copy(compactMode = on) }
    fun setDefaultMood(mood: TeamMood?) = updateSettings { it.copy(defaultMood = mood) }

    fun updateMatchSchedule(transform: (MatchScheduleSettings) -> MatchScheduleSettings) =
        updateSettings { it.copy(matchSchedule = transform(it.matchSchedule)) }

    private fun updateSettings(transform: (Settings) -> Settings) = viewModelScope.launch {
        repo.update { data -> data.copy(settings = transform(data.settings)) }
    }

    // ---------------- Match schedule cache ----------------

    fun saveMatchCache(
        matches: List<NormalizedMatch>,
        error: String,
        dateFrom: String,
        dateTo: String
    ) = viewModelScope.launch {
        repo.update { data ->
            data.copy(
                matchScheduleCache = MatchScheduleCache(
                    cachedMatches = matches,
                    lastUpdatedAt = now(),
                    lastError = error,
                    lastDateFrom = dateFrom,
                    lastDateTo = dateTo
                )
            )
        }
    }

    fun clearMatchCache() = viewModelScope.launch { repo.clearMatchCache() }

    // ---------------- Destructive ----------------

    fun deleteAllPlayers() = viewModelScope.launch {
        repo.update { it.copy(players = emptyList(), attendance = emptyList()) }
    }

    fun deleteAllMatches() = viewModelScope.launch {
        repo.update { it.copy(matches = emptyList(), attendance = emptyList(), tasks = emptyList()) }
    }

    fun deleteAllTasksAndAttendance() = viewModelScope.launch {
        repo.update { it.copy(tasks = emptyList(), attendance = emptyList()) }
    }

    fun resetAll() = viewModelScope.launch { repo.resetAll() }

    companion object {
        fun factory(repo: LocalRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AppViewModel(repo) as T
                }
            }
    }
}

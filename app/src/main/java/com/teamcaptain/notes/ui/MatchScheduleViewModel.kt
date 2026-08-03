package com.teamcaptain.notes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.teamcaptain.notes.data.model.MatchScheduleCache
import com.teamcaptain.notes.data.model.MatchScheduleSettings
import com.teamcaptain.notes.data.model.MatchSource
import com.teamcaptain.notes.data.model.NormalizedMatch
import com.teamcaptain.notes.data.remote.DemoMatches
import com.teamcaptain.notes.data.remote.FootballDataRepository
import com.teamcaptain.notes.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MatchScheduleUiState(
    val isLoading: Boolean = false,
    val matches: List<NormalizedMatch> = emptyList(),
    val error: String = "",
    val source: MatchSource = MatchSource.DEMO,
    val lastUpdatedAt: String = "",
    val dateFrom: String = "",
    val dateTo: String = "",
    val usingDefaultWindow: Boolean = true,
    val hasToken: Boolean = false,
    val initialized: Boolean = false
)

/**
 * UI state holder for the secondary Match Schedule screen. Fetching is delegated
 * to [FootballDataRepository]; persistence is delegated back to the caller so
 * this VM stays focused and never touches DataStore directly.
 */
class MatchScheduleViewModel(
    private val footballRepo: FootballDataRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MatchScheduleUiState(hasToken = footballRepo.hasToken))
    val state: StateFlow<MatchScheduleUiState> = _state.asStateFlow()

    private fun window(settings: MatchScheduleSettings): Triple<String, String, Boolean> {
        val from = if (DateUtils.isValidDate(settings.dateFrom)) settings.dateFrom else DateUtils.today()
        val to = if (DateUtils.isValidDate(settings.dateTo)) settings.dateTo else DateUtils.todayPlus(9)
        val isDefault = !DateUtils.isValidDate(settings.dateFrom) && !DateUtils.isValidDate(settings.dateTo)
        return Triple(from, to, isDefault)
    }

    /** Called once when the screen opens: show cache/demo immediately, no network. */
    fun initialize(cache: MatchScheduleCache, settings: MatchScheduleSettings) {
        if (_state.value.initialized) return
        val (from, to, isDefault) = window(settings)
        val state = when {
            cache.cachedMatches.isNotEmpty() -> MatchScheduleUiState(
                matches = cache.cachedMatches,
                source = cache.cachedMatches.firstOrNull()?.source ?: MatchSource.CACHE,
                lastUpdatedAt = cache.lastUpdatedAt,
                error = cache.lastError,
                dateFrom = cache.lastDateFrom.ifBlank { from },
                dateTo = cache.lastDateTo.ifBlank { to },
                usingDefaultWindow = isDefault,
                hasToken = footballRepo.hasToken,
                initialized = true
            )
            else -> MatchScheduleUiState(
                matches = DemoMatches.list(),
                source = MatchSource.DEMO,
                error = if (!footballRepo.hasToken)
                    "API token is not configured. Showing demo matches."
                else "",
                dateFrom = from, dateTo = to,
                usingDefaultWindow = isDefault,
                hasToken = footballRepo.hasToken,
                initialized = true
            )
        }
        _state.value = state
    }

    /**
     * Manual refresh. Uses the current settings window. Persists a successful
     * result via [onPersist]. Never crashes: falls back to cache then demo.
     */
    fun refresh(
        settings: MatchScheduleSettings,
        cache: MatchScheduleCache,
        onPersist: (List<NormalizedMatch>, String, String, String) -> Unit
    ) {
        val (from, to, isDefault) = window(settings)
        _state.value = _state.value.copy(
            isLoading = true,
            dateFrom = from,
            dateTo = to,
            usingDefaultWindow = isDefault,
            hasToken = footballRepo.hasToken
        )

        // Respect settings: demo mode or API disabled -> demo data, no network.
        if (settings.useDemoData || !settings.apiEnabled || !footballRepo.hasToken) {
            val demo = DemoMatches.list()
            val msg = when {
                settings.useDemoData -> "Demo data is enabled in settings."
                !settings.apiEnabled -> "Match Schedule API is turned off in settings. Showing demo matches."
                else -> "API token is not configured. Showing demo matches."
            }
            _state.value = _state.value.copy(
                isLoading = false, matches = demo, source = MatchSource.DEMO,
                error = msg, lastUpdatedAt = DateUtils.nowIsoTimestamp()
            )
            return
        }

        viewModelScope.launch {
            val result = footballRepo.fetchMatches(
                dateFrom = from, dateTo = to, competitionCode = settings.competitionCode
            )
            when {
                result.ok && result.matches.isNotEmpty() -> {
                    onPersist(result.matches, "", from, to)
                    _state.value = _state.value.copy(
                        isLoading = false, matches = result.matches,
                        source = if (result.usedDemoData) MatchSource.DEMO else MatchSource.API,
                        error = if (result.usedDemoData) result.error else "",
                        lastUpdatedAt = DateUtils.nowIsoTimestamp()
                    )
                }
                result.ok && result.matches.isEmpty() -> {
                    // Valid empty window.
                    onPersist(emptyList(), "", from, to)
                    _state.value = _state.value.copy(
                        isLoading = false, matches = emptyList(),
                        source = MatchSource.API, error = "",
                        lastUpdatedAt = DateUtils.nowIsoTimestamp()
                    )
                }
                else -> {
                    // Failure: prefer cache, else demo.
                    if (cache.cachedMatches.isNotEmpty()) {
                        _state.value = _state.value.copy(
                            isLoading = false, matches = cache.cachedMatches,
                            source = MatchSource.CACHE, error = result.error,
                            lastUpdatedAt = cache.lastUpdatedAt
                        )
                    } else {
                        _state.value = _state.value.copy(
                            isLoading = false, matches = DemoMatches.list(),
                            source = MatchSource.DEMO, error = result.error,
                            lastUpdatedAt = DateUtils.nowIsoTimestamp()
                        )
                    }
                }
            }
        }
    }

    companion object {
        fun factory(footballRepo: FootballDataRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MatchScheduleViewModel(footballRepo) as T
                }
            }
    }
}

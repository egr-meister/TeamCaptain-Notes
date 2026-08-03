package com.teamcaptain.notes.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * DTOs mirroring the football-data.org v4 `/matches` response.
 *
 * Every field is nullable with a default so a missing or reshaped response can
 * never crash deserialization. Unknown keys are ignored by the Json config.
 */
@Serializable
data class MatchesResponseDto(
    val matches: List<MatchDto>? = null
)

@Serializable
data class MatchDto(
    val id: Long? = null,
    val utcDate: String? = null,
    val status: String? = null,
    val competition: CompetitionDto? = null,
    val homeTeam: TeamDto? = null,
    val awayTeam: TeamDto? = null,
    val score: ScoreDto? = null
)

@Serializable
data class CompetitionDto(
    val id: Long? = null,
    val name: String? = null,
    val code: String? = null
)

@Serializable
data class TeamDto(
    val id: Long? = null,
    val name: String? = null,
    val shortName: String? = null,
    val tla: String? = null
)

@Serializable
data class ScoreDto(
    val winner: String? = null,
    val fullTime: ScoreTimeDto? = null
)

@Serializable
data class ScoreTimeDto(
    val home: Int? = null,
    val away: Int? = null
)

package com.teamcaptain.notes.data.remote

import com.teamcaptain.notes.data.remote.dto.MatchesResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for the football-data.org v4 `/matches` endpoint.
 *
 * Only the matches endpoint is exposed. No odds, predictions, bookmaker or
 * live-betting endpoints exist here by design.
 */
interface FootballDataApiService {

    @GET("matches")
    suspend fun getMatches(
        @Query("dateFrom") dateFrom: String,
        @Query("dateTo") dateTo: String,
        @Query("competitions") competitions: String? = null
    ): MatchesResponseDto
}

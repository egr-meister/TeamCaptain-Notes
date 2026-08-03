package com.teamcaptain.notes.data.remote

import com.teamcaptain.notes.BuildConfig
import com.teamcaptain.notes.data.model.MatchSource
import com.teamcaptain.notes.data.model.NormalizedMatch
import com.teamcaptain.notes.data.remote.dto.MatchDto
import com.teamcaptain.notes.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/** Stable, UI-safe result of a match fetch. Never throws into the UI. */
data class FootballApiResult(
    val ok: Boolean,
    val matches: List<NormalizedMatch>,
    val error: String,
    val usedDemoData: Boolean
)

/**
 * Isolated integration point for football-data.org. All API concerns live here.
 * The raw API token is read from [BuildConfig] and never logged.
 */
class FootballDataRepository {

    private val placeholderToken = "your_api_token_here"

    private val token: String = BuildConfig.FOOTBALL_DATA_API_TOKEN.trim()
    private val baseUrl: String = BuildConfig.FOOTBALL_API_BASE_URL.trim()
        .let { if (it.endsWith("/")) it else "$it/" }

    val hasToken: Boolean
        get() = token.isNotBlank() && token != placeholderToken

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val service: FootballDataApiService? by lazy {
        runCatching {
            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    // X-Auth-Token header; token value is never written to logs.
                    val request = chain.request().newBuilder()
                        .addHeader("X-Auth-Token", token)
                        .build()
                    chain.proceed(request)
                }
                .build()

            Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(FootballDataApiService::class.java)
        }.getOrNull()
    }

    /**
     * Fetch matches for the given window. If [dateFrom]/[dateTo] are blank the
     * default 10-day window (today .. today+9) is used.
     */
    suspend fun fetchMatches(
        dateFrom: String = "",
        dateTo: String = "",
        competitionCode: String = ""
    ): FootballApiResult = withContext(Dispatchers.IO) {
        val from = if (DateUtils.isValidDate(dateFrom)) dateFrom else DateUtils.today()
        val to = if (DateUtils.isValidDate(dateTo)) dateTo else DateUtils.todayPlus(9)

        if (!hasToken) {
            return@withContext FootballApiResult(
                ok = true,
                matches = DemoMatches.list(),
                error = "API token is not configured. Showing demo matches.",
                usedDemoData = true
            )
        }

        val api = service
            ?: return@withContext FootballApiResult(
                ok = false,
                matches = emptyList(),
                error = "Could not initialise the match service. Showing cached or demo data if available.",
                usedDemoData = false
            )

        try {
            val comp = competitionCode.trim().ifBlank { null }
            val response = api.getMatches(dateFrom = from, dateTo = to, competitions = comp)
            val normalized = (response.matches ?: emptyList()).map { it.normalize() }
            FootballApiResult(
                ok = true,
                matches = normalized,
                error = "",
                usedDemoData = false
            )
        } catch (e: HttpException) {
            val message = when (e.code()) {
                429 -> "API request limit reached. Showing cached or demo data if available."
                403, 401 -> "API access was refused for this plan. Showing cached or demo data if available."
                else -> "Could not load the latest matches (server error ${e.code()})."
            }
            FootballApiResult(false, emptyList(), message, false)
        } catch (e: UnknownHostException) {
            FootballApiResult(false, emptyList(), "No internet connection. Showing cached or demo data if available.", false)
        } catch (e: SocketTimeoutException) {
            FootballApiResult(false, emptyList(), "The request timed out. Showing cached or demo data if available.", false)
        } catch (e: IOException) {
            FootballApiResult(false, emptyList(), "Network error. Showing cached or demo data if available.", false)
        } catch (e: Exception) {
            // Includes serialization errors if the response format changes.
            FootballApiResult(false, emptyList(), "The match data could not be read. Showing cached or demo data if available.", false)
        }
    }

    /** Null-safe normalization from API DTO to the app model. */
    private fun MatchDto.normalize(): NormalizedMatch {
        val (date, time) = DateUtils.splitUtcDate(utcDate)
        return NormalizedMatch(
            id = id?.toString() ?: (utcDate ?: "") + homeTeam?.name,
            utcDate = utcDate ?: "",
            date = date,
            time = time,
            competitionName = competition?.name ?: "",
            competitionCode = competition?.code ?: "",
            homeTeam = homeTeam?.name?.ifBlank { "Unknown" } ?: "Unknown",
            awayTeam = awayTeam?.name?.ifBlank { "Unknown" } ?: "Unknown",
            status = status ?: "Unknown",
            homeScore = score?.fullTime?.home,
            awayScore = score?.fullTime?.away,
            winner = score?.winner ?: "",
            source = MatchSource.API
        )
    }
}

package com.teamcaptain.notes.data.remote

import com.teamcaptain.notes.data.model.MatchSource
import com.teamcaptain.notes.data.model.NormalizedMatch
import com.teamcaptain.notes.util.DateUtils

/**
 * Bundled offline demo matches. Used whenever no API token is configured, the
 * API fails with no cache, or demo mode is enabled in settings.
 *
 * Team names are neutral placeholders — no official clubs, no logos, no photos.
 */
object DemoMatches {

    fun list(): List<NormalizedMatch> {
        val d0 = DateUtils.today()
        val d1 = DateUtils.todayPlus(1)
        val d3 = DateUtils.todayPlus(3)
        val d5 = DateUtils.todayPlus(5)
        val d8 = DateUtils.todayPlus(8)
        return listOf(
            NormalizedMatch(
                id = "demo-1",
                date = d0, time = "18:00",
                competitionName = "Community Friendly Series", competitionCode = "CFS",
                homeTeam = "Riverside United", awayTeam = "Hillcrest Rangers",
                status = "SCHEDULED", source = MatchSource.DEMO
            ),
            NormalizedMatch(
                id = "demo-2",
                date = d1, time = "20:15",
                competitionName = "Community Friendly Series", competitionCode = "CFS",
                homeTeam = "Parkside Athletic", awayTeam = "Old Mill FC",
                status = "SCHEDULED", source = MatchSource.DEMO
            ),
            NormalizedMatch(
                id = "demo-3",
                date = d3, time = "16:30",
                competitionName = "Weekend Amateur Cup", competitionCode = "WAC",
                homeTeam = "Northgate Town", awayTeam = "Lakeview Sporting",
                status = "SCHEDULED", source = MatchSource.DEMO
            ),
            NormalizedMatch(
                id = "demo-4",
                date = d5, time = "19:00",
                competitionName = "Weekend Amateur Cup", competitionCode = "WAC",
                homeTeam = "Greenfield Wanderers", awayTeam = "Station Road FC",
                status = "SCHEDULED", source = MatchSource.DEMO
            ),
            NormalizedMatch(
                id = "demo-5",
                date = d8, time = "14:00",
                competitionName = "Local League", competitionCode = "LL",
                homeTeam = "Harbour City", awayTeam = "Meadow Rovers",
                status = "SCHEDULED", source = MatchSource.DEMO
            )
        )
    }
}

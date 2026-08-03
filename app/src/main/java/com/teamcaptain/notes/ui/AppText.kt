package com.teamcaptain.notes.ui

/** Central place for the required disclaimer / privacy copy. */
object AppText {

    const val CAPTAIN_DISCLAIMER =
        "TeamCaptain Notes is a manual football captain helper app. Player attendance, " +
            "match tasks, team notes, mood, and post-match notes are entered by the user. " +
            "The app is not an official football organization tool and does not provide " +
            "professional coaching advice."

    const val SCHEDULE_DISCLAIMER =
        "Match data is provided by football-data.org. Availability, accuracy, competitions, " +
            "and update frequency depend on the API provider and the current API plan."

    const val SCHEDULE_SHORT_NOTE =
        "Match data is provided by football-data.org and may depend on your API plan."

    const val PRIVACY_NOTE =
        "TeamCaptain Notes stores player lists, attendance records, match tasks, team mood, " +
            "notes, settings, and cached match data on this device. The app uses internet only " +
            "to load football match data from football-data.org. No account, no ads, no analytics, " +
            "no payments, no Firebase, no location, no notifications, no sensors, no Google Fit, " +
            "and no Health Connect."

    const val APP_INFO =
        "TeamCaptain Notes v1.0.0 — an offline-first team captain planner. Organize players, " +
            "track attendance, manage match tasks, and keep team notes before and after the match."
}

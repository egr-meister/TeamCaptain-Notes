package com.teamcaptain.notes

import android.app.Application
import com.teamcaptain.notes.data.local.LocalRepository
import com.teamcaptain.notes.data.remote.FootballDataRepository

/**
 * Application-scoped dependency container. Simple manual DI keeps the app
 * dependency-light and stable.
 */
class TeamCaptainApp : Application() {

    lateinit var localRepository: LocalRepository
        private set

    lateinit var footballRepository: FootballDataRepository
        private set

    override fun onCreate() {
        super.onCreate()
        localRepository = LocalRepository(applicationContext)
        footballRepository = FootballDataRepository()
    }
}

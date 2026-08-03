package com.teamcaptain.notes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.teamcaptain.notes.ui.navigation.AppNavGraph
import com.teamcaptain.notes.ui.theme.TeamCaptainTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Custom splash screen (AndroidX). Must be called before super/ setContent.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as TeamCaptainApp

        setContent {
            TeamCaptainTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph(
                        localRepository = app.localRepository,
                        footballRepository = app.footballRepository
                    )
                }
            }
        }
    }
}

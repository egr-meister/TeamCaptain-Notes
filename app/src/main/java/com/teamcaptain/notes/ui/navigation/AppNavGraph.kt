package com.teamcaptain.notes.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.teamcaptain.notes.data.local.LocalRepository
import com.teamcaptain.notes.data.remote.FootballDataRepository
import com.teamcaptain.notes.ui.AppViewModel
import com.teamcaptain.notes.ui.MatchScheduleViewModel
import com.teamcaptain.notes.ui.screens.AttendanceScreen
import com.teamcaptain.notes.ui.screens.HistoryScreen
import com.teamcaptain.notes.ui.screens.HomeScreen
import com.teamcaptain.notes.ui.screens.MatchDetailScreen
import com.teamcaptain.notes.ui.screens.MatchEditScreen
import com.teamcaptain.notes.ui.screens.OnboardingScreen
import com.teamcaptain.notes.ui.screens.PlayerEditScreen
import com.teamcaptain.notes.ui.screens.PlayersScreen
import com.teamcaptain.notes.ui.screens.ScheduleScreen
import com.teamcaptain.notes.ui.screens.ScheduleSettingsScreen
import com.teamcaptain.notes.ui.screens.SettingsScreen
import com.teamcaptain.notes.ui.screens.TasksScreen

/** Central route table. */
object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val PLAYERS = "players"
    const val PLAYER_EDIT = "player_edit" // ?playerId=
    const val MATCH_EDIT = "match_edit"    // ?matchId=
    const val MATCH_DETAIL = "match_detail" // /{matchId}
    const val ATTENDANCE = "attendance"    // /{matchId}
    const val TASKS = "tasks"              // /{matchId}
    const val HISTORY = "history"
    const val SCHEDULE = "schedule"
    const val SCHEDULE_SETTINGS = "schedule_settings"
    const val SETTINGS = "settings"

    fun playerEdit(playerId: String? = null) =
        if (playerId.isNullOrBlank()) PLAYER_EDIT else "$PLAYER_EDIT?playerId=$playerId"

    fun matchEdit(matchId: String? = null) =
        if (matchId.isNullOrBlank()) MATCH_EDIT else "$MATCH_EDIT?matchId=$matchId"

    fun matchDetail(matchId: String) = "$MATCH_DETAIL/$matchId"
    fun attendance(matchId: String) = "$ATTENDANCE/$matchId"
    fun tasks(matchId: String) = "$TASKS/$matchId"
}

@Composable
fun AppNavGraph(
    localRepository: LocalRepository,
    footballRepository: FootballDataRepository
) {
    val appViewModel: AppViewModel = viewModel(factory = AppViewModel.factory(localRepository))
    val scheduleViewModel: MatchScheduleViewModel =
        viewModel(factory = MatchScheduleViewModel.factory(footballRepository))

    val ready by appViewModel.isReady.collectAsState()

    if (!ready) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val navController = rememberNavController()
    // Decide start once, after data has loaded, so returning users skip onboarding.
    val startRoute = remember {
        if (appViewModel.startAtOnboarding) Routes.ONBOARDING else Routes.HOME
    }

    NavHost(navController = navController, startDestination = startRoute) {

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinish = {
                    appViewModel.completeOnboarding()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(vm = appViewModel, nav = navController)
        }

        composable(Routes.PLAYERS) {
            PlayersScreen(vm = appViewModel, nav = navController)
        }

        composable(
            route = "${Routes.PLAYER_EDIT}?playerId={playerId}",
            arguments = listOf(navArgument("playerId") {
                type = NavType.StringType; nullable = true; defaultValue = null
            })
        ) { entry ->
            PlayerEditScreen(
                vm = appViewModel,
                nav = navController,
                playerId = entry.arguments?.getString("playerId")
            )
        }

        composable(
            route = "${Routes.MATCH_EDIT}?matchId={matchId}",
            arguments = listOf(navArgument("matchId") {
                type = NavType.StringType; nullable = true; defaultValue = null
            })
        ) { entry ->
            MatchEditScreen(
                vm = appViewModel,
                nav = navController,
                matchId = entry.arguments?.getString("matchId")
            )
        }

        composable(
            route = "${Routes.MATCH_DETAIL}/{matchId}",
            arguments = listOf(navArgument("matchId") { type = NavType.StringType })
        ) { entry ->
            MatchDetailScreen(
                vm = appViewModel,
                nav = navController,
                matchId = entry.arguments?.getString("matchId")
            )
        }

        composable(
            route = "${Routes.ATTENDANCE}/{matchId}",
            arguments = listOf(navArgument("matchId") { type = NavType.StringType })
        ) { entry ->
            AttendanceScreen(
                vm = appViewModel,
                nav = navController,
                matchId = entry.arguments?.getString("matchId")
            )
        }

        composable(
            route = "${Routes.TASKS}/{matchId}",
            arguments = listOf(navArgument("matchId") { type = NavType.StringType })
        ) { entry ->
            TasksScreen(
                vm = appViewModel,
                nav = navController,
                matchId = entry.arguments?.getString("matchId")
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(vm = appViewModel, nav = navController)
        }

        composable(Routes.SCHEDULE) {
            ScheduleScreen(
                vm = appViewModel,
                scheduleVm = scheduleViewModel,
                nav = navController
            )
        }

        composable(Routes.SCHEDULE_SETTINGS) {
            ScheduleSettingsScreen(vm = appViewModel, nav = navController)
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(vm = appViewModel, nav = navController)
        }
    }
}

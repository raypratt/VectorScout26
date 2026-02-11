package com.example.vectorscout26.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.vectorscout26.data.database.ScoutDatabase
import com.example.vectorscout26.data.model.ActionRecord
import com.example.vectorscout26.data.model.ActionType
import com.example.vectorscout26.data.model.MatchPhase
import com.example.vectorscout26.data.repository.PitScoutRepository
import com.example.vectorscout26.data.repository.ScoutRepository
import com.example.vectorscout26.ui.home.HomeScreen
import com.example.vectorscout26.ui.match.MatchScoutingScreen
import com.example.vectorscout26.ui.match.MatchScoutingViewModel
import com.example.vectorscout26.ui.match.MatchScoutingViewModelFactory
import com.example.vectorscout26.ui.match.action.ActionDetailScreen
import com.example.vectorscout26.ui.pit.PitScoutingScreen
import com.example.vectorscout26.ui.pit.PitScoutingViewModel
import com.example.vectorscout26.ui.pit.PitScoutingViewModelFactory
import com.example.vectorscout26.ui.qrcode.QRCodeScreen
import com.example.vectorscout26.ui.qrcode.PitQRCodeScreen

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val database = remember { ScoutDatabase.getDatabase(context) }
    val matchRepository = remember { ScoutRepository(database.matchScoutDao()) }
    val pitRepository = remember { PitScoutRepository(database.pitScoutDao()) }
    val matchViewModelFactory = remember { MatchScoutingViewModelFactory(matchRepository) }
    val pitViewModelFactory = remember { PitScoutingViewModelFactory(pitRepository) }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onMatchScoutingClick = {
                    navController.navigate("match_flow")
                },
                onPitScoutingClick = {
                    navController.navigate("pit_flow")
                }
            )
        }

        // Nested navigation for match scouting flow to share ViewModel
        navigation(
            startDestination = Screen.MatchScouting.route,
            route = "match_flow"
        ) {
            composable(Screen.MatchScouting.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("match_flow")
                }
                val viewModel: MatchScoutingViewModel = viewModel(parentEntry, factory = matchViewModelFactory)

                MatchScoutingScreen(
                    viewModel = viewModel,
                    onActionClick = { phase, actionType ->
                        navController.navigate(
                            Screen.ActionDetail.createRoute(phase.name, actionType.name)
                        )
                    },
                    onSubmitComplete = { matchScoutId ->
                        navController.navigate(Screen.QRCode.createRoute(matchScoutId))
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.ActionDetail.route,
                arguments = listOf(
                    navArgument("phase") { type = NavType.StringType },
                    navArgument("actionType") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("match_flow")
                }
                val viewModel: MatchScoutingViewModel = viewModel(parentEntry, factory = matchViewModelFactory)
                val state = viewModel.state.collectAsState()

                val phase = backStackEntry.arguments?.getString("phase") ?: ""
                val actionType = backStackEntry.arguments?.getString("actionType") ?: ""

                val matchPhase = MatchPhase.valueOf(phase)
                val action = ActionType.fromString(actionType, matchPhase)

                ActionDetailScreen(
                    phase = phase,
                    actionType = actionType,
                    robotDesignation = state.value.robotDesignation,
                    isBlueRight = state.value.isBlueRight,
                    opposingTeams = state.value.opposingTeams,
                    onComplete = { qualitativeData, elapsedMs ->
                        // Save action record
                        if (action != null) {
                            val record = ActionRecord(
                                phase = matchPhase,
                                actionType = action,
                                startTimeMs = System.currentTimeMillis() - elapsedMs,
                                endTimeMs = System.currentTimeMillis(),
                                qualitativeData = qualitativeData
                            )
                            viewModel.addActionRecord(record)
                        }
                        navController.popBackStack()
                    },
                    onCancel = {
                        // Decrement counter if action has counter
                        if (action != null && action.hasCounter) {
                            viewModel.removeLastActionRecord(matchPhase, action)
                        }
                        navController.popBackStack()
                    }
                )
            }

            // QRCode screen inside match_flow to share ViewModel and preserve state
            composable(
                route = Screen.QRCode.route,
                arguments = listOf(
                    navArgument("matchScoutId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val matchScoutId = backStackEntry.arguments?.getLong("matchScoutId") ?: 0L

                QRCodeScreen(
                    matchScoutId = matchScoutId,
                    onNewMatch = {
                        // Pop back to MatchScouting screen - state is already preserved in ViewModel
                        navController.popBackStack(Screen.MatchScouting.route, inclusive = false)
                    },
                    onHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
        }

        // Nested navigation for pit scouting flow to share ViewModel
        navigation(
            startDestination = Screen.PitScouting.route,
            route = "pit_flow"
        ) {
            composable(Screen.PitScouting.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("pit_flow")
                }
                val viewModel: PitScoutingViewModel = viewModel(parentEntry, factory = pitViewModelFactory)

                PitScoutingScreen(
                    viewModel = viewModel,
                    onSubmitSuccess = { pitScoutId ->
                        navController.navigate(Screen.PitQRCode.createRoute(pitScoutId))
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.PitQRCode.route,
                arguments = listOf(
                    navArgument("pitScoutId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("pit_flow")
                }
                val viewModel: PitScoutingViewModel = viewModel(parentEntry, factory = pitViewModelFactory)
                val pitScoutId = backStackEntry.arguments?.getLong("pitScoutId") ?: 0L

                PitQRCodeScreen(
                    pitScoutId = pitScoutId,
                    onNewPitScout = {
                        viewModel.resetStateForNewScout()
                        navController.popBackStack(Screen.PitScouting.route, inclusive = false)
                    },
                    onHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

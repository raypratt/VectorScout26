package com.example.vectorscout26.ui.match.action

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vectorscout26.data.model.*
import com.example.vectorscout26.ui.match.action.components.*

@Composable
fun ActionDetailScreen(
    phase: String,
    actionType: String,
    robotDesignation: String,
    isBlueRight: Boolean,
    opposingTeams: Map<String, Int> = emptyMap(),
    onComplete: (qualitativeData: QualitativeData?, elapsedMs: Long) -> Unit,
    onCancel: () -> Unit,
    actionViewModel: ActionViewModel = viewModel()
) {
    val timerState by actionViewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    val matchPhase = MatchPhase.valueOf(phase)
    val action = ActionType.fromString(actionType, matchPhase)

    var qualitativeData by remember { mutableStateOf<QualitativeData?>(null) }
    var isDataComplete by remember { mutableStateOf(action?.hasCounter == false) } // Foul/Damaged don't need data

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "$phase - $actionType",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Show timer if action has timer
                if (action?.hasTimer == true && timerState.isRunning) {
                    TimerDisplay(elapsedMs = timerState.elapsedMs)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Show appropriate form based on action type
                when (action) {
                    ActionType.Load -> LoadForm(
                        robotDesignation = robotDesignation,
                        isBlueRight = isBlueRight,
                        onDataChange = {
                            qualitativeData = it
                            isDataComplete = true
                        }
                    )
                    ActionType.Shoot -> ShootForm(
                        robotDesignation = robotDesignation,
                        isBlueRight = isBlueRight,
                        onDataChange = {
                            qualitativeData = it
                            isDataComplete = true
                        }
                    )
                    ActionType.Ferry -> FerryForm(
                        robotDesignation = robotDesignation,
                        isBlueRight = isBlueRight,
                        onDataChange = {
                            qualitativeData = it
                            isDataComplete = true
                        }
                    )
                    ActionType.AutonClimb, ActionType.EndGameClimb -> ClimbForm(
                        phase = matchPhase,
                        onDataChange = {
                            qualitativeData = it
                            isDataComplete = true
                        }
                    )
                    ActionType.Defense -> DefenseForm(
                        robotDesignation = robotDesignation,
                        opposingTeams = opposingTeams,
                        onDataChange = {
                            qualitativeData = it
                            isDataComplete = true
                        }
                    )
                    ActionType.Foul -> FoulForm(
                        onDataChange = {
                            qualitativeData = it
                            isDataComplete = true
                        }
                    )
                    ActionType.Damaged -> DamagedForm(
                        onDataChange = {
                            qualitativeData = it
                            isDataComplete = true
                        }
                    )
                    ActionType.Incapacitated, ActionType.Tipped -> {
                        SimpleForm()
                        LaunchedEffect(Unit) {
                            qualitativeData = EmptyData()
                            isDataComplete = true
                        }
                    }
                    else -> {
                        Text("Unknown action type")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        actionViewModel.stopTimer()
                        onCancel()
                    },
                    modifier = Modifier.weight(1f).height(84.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cancel", fontSize = 24.sp)
                }

                Button(
                    onClick = {
                        actionViewModel.stopTimer()
                        val elapsedMs = actionViewModel.getElapsedTime()
                        onComplete(qualitativeData, elapsedMs)
                    },
                    modifier = Modifier.weight(1f).height(84.dp),
                    enabled = isDataComplete
                ) {
                    val buttonText = when {
                        action?.hasTimer == false -> "Done"
                        else -> "End $actionType"
                    }
                    Text(buttonText, fontSize = 24.sp)
                }
            }
        }
    }
}

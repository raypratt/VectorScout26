package com.example.vectorscout26.ui.foul

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vectorscout26.data.model.FoulTeamEntry

private val RedTeamColor = Color(0xFFD32F2F)
private val BlueTeamColor = Color(0xFF1565C0)
private val RedTeamBackground = Color(0xFFFFEBEE)
private val BlueTeamBackground = Color(0xFFE3F2FD)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoulScoutingScreen(
    viewModel: FoulScoutingViewModel,
    onGenerateQR: () -> Unit,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.initializeState(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Foul Scouting") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Schedule status
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (state.scheduleLoaded)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Text(
                    text = if (state.scheduleLoaded)
                        "Schedule: ${state.event}"
                    else
                        "No schedule loaded — enter team numbers manually",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }

            // Match number input
            OutlinedTextField(
                value = state.matchNumber,
                onValueChange = { viewModel.updateMatchNumber(it) },
                label = { Text("Match Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Team rows
            Card {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Team",
                            modifier = Modifier.width(80.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Major",
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Minor",
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    HorizontalDivider()

                    state.teams.forEach { team ->
                        FoulTeamRow(
                            entry = team,
                            scheduleLoaded = state.scheduleLoaded,
                            onTeamNumberChange = { viewModel.updateTeamNumber(team.designation, it) },
                            onIncrementMajor = { viewModel.incrementFoul(team.designation, isMajor = true) },
                            onDecrementMajor = { viewModel.decrementFoul(team.designation, isMajor = true) },
                            onIncrementMinor = { viewModel.incrementFoul(team.designation, isMajor = false) },
                            onDecrementMinor = { viewModel.decrementFoul(team.designation, isMajor = false) }
                        )
                        HorizontalDivider(thickness = 0.5.dp)
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.generateQR()
                    onGenerateQR()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp),
                enabled = state.matchNumber.isNotBlank()
            ) {
                Text("Generate QR Code", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FoulTeamRow(
    entry: FoulTeamEntry,
    scheduleLoaded: Boolean,
    onTeamNumberChange: (Int) -> Unit,
    onIncrementMajor: () -> Unit,
    onDecrementMajor: () -> Unit,
    onIncrementMinor: () -> Unit,
    onDecrementMinor: () -> Unit
) {
    val isRed = entry.designation.startsWith("Red")
    val rowColor = if (isRed) RedTeamBackground else BlueTeamBackground
    val textColor = if (isRed) RedTeamColor else BlueTeamColor

    var teamNumberText by remember(entry.teamNumber) {
        mutableStateOf(if (entry.teamNumber > 0) entry.teamNumber.toString() else "")
    }

    Surface(color = rowColor) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Designation + team number
            Column(modifier = Modifier.width(80.dp)) {
                Text(
                    text = entry.designation,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = 13.sp
                )
                if (scheduleLoaded && entry.teamNumber > 0) {
                    Text(
                        text = entry.teamNumber.toString(),
                        fontSize = 13.sp,
                        color = textColor
                    )
                } else {
                    OutlinedTextField(
                        value = teamNumberText,
                        onValueChange = { v ->
                            teamNumberText = v
                            v.toIntOrNull()?.let { onTeamNumberChange(it) }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .width(72.dp)
                            .height(48.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                        singleLine = true
                    )
                }
            }

            // Major counter
            FoulCounter(
                count = entry.majorCount,
                onIncrement = onIncrementMajor,
                onDecrement = onDecrementMajor,
                modifier = Modifier.weight(1f)
            )

            // Minor counter
            FoulCounter(
                count = entry.minorCount,
                onIncrement = onIncrementMinor,
                onDecrement = onDecrementMinor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FoulCounter(
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onDecrement,
            modifier = Modifier.size(36.dp)
        ) {
            Text("−", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            text = count.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.widthIn(min = 28.dp),
            textAlign = TextAlign.Center
        )
        IconButton(
            onClick = onIncrement,
            modifier = Modifier.size(36.dp)
        ) {
            Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

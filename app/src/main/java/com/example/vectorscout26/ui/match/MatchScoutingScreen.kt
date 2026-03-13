package com.example.vectorscout26.ui.match

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vectorscout26.data.model.ActionType
import com.example.vectorscout26.data.model.Event
import com.example.vectorscout26.data.model.MatchPhase
import com.example.vectorscout26.data.repository.EventRepository
import com.example.vectorscout26.data.repository.ScheduleRepository
import com.example.vectorscout26.ui.match.components.*

private val RedTeamColor = Color(0xFFD32F2F)
private val BlueTeamColor = Color(0xFF1565C0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchScoutingScreen(
    viewModel: MatchScoutingViewModel,
    onActionClick: (MatchPhase, ActionType) -> Unit,
    onSubmitComplete: (Long) -> Unit,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var selectedTab by remember { mutableIntStateOf(0) }

    // Load events from repository
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    LaunchedEffect(Unit) {
        events = EventRepository.loadEvents(context)
        viewModel.initializeScheduleState(context)
    }

    // Schedule loading dialog state
    var showScheduleDialog by remember { mutableStateOf(false) }
    var scheduleLoadMessage by remember { mutableStateOf("") }
    var selectedEventForSchedule by remember { mutableStateOf<Event?>(null) }

    // File picker for importing schedule JSON
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedEventForSchedule?.let { event ->
                viewModel.importScheduleFromFile(context, uri, event.eventCode, event.eventName) { success, message ->
                    scheduleLoadMessage = message
                    if (success) {
                        showScheduleDialog = false
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Match Scouting") },
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
                .imePadding()
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Scout") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Schedule") }
                )
            }

            when (selectedTab) {
                0 -> ScoutTab(
                    state = state,
                    scrollState = scrollState,
                    focusManager = focusManager,
                    context = context,
                    events = events,
                    scheduleLoadMessage = scheduleLoadMessage,
                    viewModel = viewModel,
                    onActionClick = onActionClick,
                    onSubmitComplete = onSubmitComplete,
                    onShowScheduleDialog = { showScheduleDialog = true }
                )
                1 -> ScheduleTab(
                    state = state,
                    currentMatchNumber = state.matchNumber.toIntOrNull()
                )
            }
        }
    }

    // Schedule Loading Dialog
    if (showScheduleDialog) {
        AlertDialog(
            onDismissRequest = { showScheduleDialog = false },
            title = { Text("Load Match Schedule") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Select an event:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        items(events) { event ->
                            val isCached = ScheduleRepository.isScheduleCached(context, event.eventCode)
                            val isSelected = selectedEventForSchedule?.eventCode == event.eventCode
                            Card(
                                onClick = { selectedEventForSchedule = event },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        isSelected -> MaterialTheme.colorScheme.secondaryContainer
                                        isCached -> MaterialTheme.colorScheme.primaryContainer
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = event.eventName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            text = event.date,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    if (isCached) {
                                        Text(
                                            text = "Cached",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    if (selectedEventForSchedule == null) {
                        Text(
                            text = "Select an event above first.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Selected: ${selectedEventForSchedule!!.eventName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                selectedEventForSchedule?.let { event ->
                                    viewModel.loadSchedule(context, event.eventCode, event.eventName) { success, message ->
                                        scheduleLoadMessage = message
                                        if (success) showScheduleDialog = false
                                    }
                                }
                            },
                            enabled = selectedEventForSchedule != null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Load TBA")
                        }

                        OutlinedButton(
                            onClick = {
                                if (selectedEventForSchedule != null) {
                                    filePickerLauncher.launch("*/*")
                                }
                            },
                            enabled = selectedEventForSchedule != null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Import JSON")
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.enableManualEntryMode(context)
                            scheduleLoadMessage = "Manual entry mode enabled"
                            showScheduleDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Manual Entry")
                    }

                    if (state.isLoadingSchedule) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showScheduleDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun ScoutTab(
    state: MatchScoutState,
    scrollState: androidx.compose.foundation.ScrollState,
    focusManager: androidx.compose.ui.focus.FocusManager,
    context: android.content.Context,
    events: List<Event>,
    scheduleLoadMessage: String,
    viewModel: MatchScoutingViewModel,
    onActionClick: (MatchPhase, ActionType) -> Unit,
    onSubmitComplete: (Long) -> Unit,
    onShowScheduleDialog: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Schedule Status Card
        Card(
            onClick = onShowScheduleDialog,
            colors = CardDefaults.cardColors(
                containerColor = if (state.scheduleLoaded)
                    MaterialTheme.colorScheme.primaryContainer
                else if (state.isManualEntryMode)
                    MaterialTheme.colorScheme.surfaceVariant
                else
                    MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = when {
                            state.scheduleLoaded -> "Schedule: ${state.scheduleEventName}"
                            state.isManualEntryMode -> "Manual Entry Mode"
                            else -> "No Schedule Loaded"
                        },
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = when {
                            state.scheduleLoaded -> "${state.scheduleMatchCount} matches"
                            state.isManualEntryMode -> "Enter team numbers manually"
                            else -> "Tap to load schedule"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (state.isLoadingSchedule) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        // Show schedule load message
        if (scheduleLoadMessage.isNotEmpty()) {
            Text(
                text = scheduleLoadMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Show errors if any
        if (state.errors.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Please fix the following errors:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    state.errors.forEach { error ->
                        Text(
                            text = "• $error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        // Pre-Match Section
        PreMatchSection(
            events = events,
            event = state.event,
            matchNumber = state.matchNumber,
            robotDesignation = state.robotDesignation,
            scoutName = state.scoutName,
            teamNumber = state.teamNumber,
            startPosition = state.startPosition,
            loaded = state.loaded,
            noShow = state.noShow,
            isBlueRight = state.isBlueRight,
            teamNumberAutoFilled = state.teamNumberAutoFilled,
            onEventChange = { eventCode, _ -> viewModel.updateEvent(eventCode, eventCode) },
            onMatchNumberChange = viewModel::updateMatchNumber,
            onRobotDesignationChange = viewModel::updateRobotDesignation,
            onScoutNameChange = viewModel::updateScoutName,
            onTeamNumberChange = viewModel::updateTeamNumber,
            onStartPositionChange = viewModel::updateStartPosition,
            onLoadedToggle = viewModel::toggleLoaded,
            onNoShowToggle = viewModel::toggleNoShow,
            onToggleOrientation = viewModel::toggleFieldOrientation
        )

        // Auton Section
        AutonSection(
            loadCount = state.getActionCount(MatchPhase.AUTON, ActionType.Load),
            shootCount = state.getActionCount(MatchPhase.AUTON, ActionType.Shoot),
            ferryCount = state.getActionCount(MatchPhase.AUTON, ActionType.Ferry),
            climbCount = state.getActionCount(MatchPhase.AUTON, ActionType.AutonClimb),
            onActionClick = { actionType ->
                onActionClick(MatchPhase.AUTON, actionType)
            },
            onDeleteAction = { actionType ->
                viewModel.removeLastActionRecord(MatchPhase.AUTON, actionType)
            }
        )

        // Teleop Section
        TeleopSection(
            loadCount = state.getActionCount(MatchPhase.TELEOP, ActionType.Load),
            shootCount = state.getActionCount(MatchPhase.TELEOP, ActionType.Shoot),
            ferryCount = state.getActionCount(MatchPhase.TELEOP, ActionType.Ferry),
            defenseCount = state.getActionCount(MatchPhase.TELEOP, ActionType.Defense),
            incapacitatedCount = state.getActionCount(MatchPhase.TELEOP, ActionType.Incapacitated),
            tippedCount = state.getActionCount(MatchPhase.TELEOP, ActionType.Tipped),
            onActionClick = { actionType ->
                onActionClick(MatchPhase.TELEOP, actionType)
            },
            onDeleteAction = { actionType ->
                viewModel.removeLastActionRecord(MatchPhase.TELEOP, actionType)
            }
        )

        // End Game Section
        EndGameSection(
            climbCount = state.getActionCount(MatchPhase.ENDGAME, ActionType.EndGameClimb),
            onActionClick = { actionType ->
                onActionClick(MatchPhase.ENDGAME, actionType)
            },
            onDeleteAction = { actionType ->
                viewModel.removeLastActionRecord(MatchPhase.ENDGAME, actionType)
            }
        )

        // Submit Button
        Button(
            onClick = {
                viewModel.submitMatch(onSuccess = onSubmitComplete)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp),
            enabled = !state.isSubmitting
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = "Submit Match",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 33.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ScheduleTab(
    state: MatchScoutState,
    currentMatchNumber: Int?
) {
    val schedule = if (state.scheduleLoaded) ScheduleRepository.getCurrentSchedule() else null

    if (schedule == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No schedule loaded.\nGo to the Scout tab to load one.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "M#",
                    modifier = Modifier.width(40.dp),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp
                )
                Text(
                    text = "R1",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = RedTeamColor
                )
                Text(
                    text = "R2",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = RedTeamColor
                )
                Text(
                    text = "R3",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = RedTeamColor
                )
                Text(
                    text = "B1",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = BlueTeamColor
                )
                Text(
                    text = "B2",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = BlueTeamColor
                )
                Text(
                    text = "B3",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = BlueTeamColor
                )
            }
            HorizontalDivider(thickness = 1.dp)

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(schedule.matches) { match ->
                    val isCurrentMatch = match.matchNumber == currentMatchNumber
                    val rowBackground = if (isCurrentMatch)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surface

                    Surface(color = rowBackground) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = match.matchNumber.toString(),
                                modifier = Modifier.width(40.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                fontWeight = if (isCurrentMatch) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = match.red1.toString(),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                color = RedTeamColor
                            )
                            Text(
                                text = match.red2.toString(),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                color = RedTeamColor
                            )
                            Text(
                                text = match.red3.toString(),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                color = RedTeamColor
                            )
                            Text(
                                text = match.blue1.toString(),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                color = BlueTeamColor
                            )
                            Text(
                                text = match.blue2.toString(),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                color = BlueTeamColor
                            )
                            Text(
                                text = match.blue3.toString(),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                color = BlueTeamColor
                            )
                        }
                    }
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }
        }
    }
}

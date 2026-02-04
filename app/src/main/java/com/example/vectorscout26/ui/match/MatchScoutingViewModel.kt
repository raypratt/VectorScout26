package com.example.vectorscout26.ui.match

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vectorscout26.data.model.*
import com.example.vectorscout26.data.repository.LoadResult
import com.example.vectorscout26.data.repository.ScheduleRepository
import com.example.vectorscout26.data.repository.ScheduleSource
import com.example.vectorscout26.data.repository.ScoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MatchScoutState(
    // Pre-match
    val event: String = "",
    val eventCode: String = "",  // TBA event code for schedule lookup
    val matchNumber: String = "",
    val robotDesignation: String = "",
    val scoutName: String = "",
    val teamNumber: String = "",
    val startPosition: String = "",
    val loaded: Boolean = false,
    val noShow: Boolean = false,
    val isBlueRight: Boolean = true,  // Field orientation for maps
    val teamNumberAutoFilled: Boolean = false,  // Track if team number was auto-filled
    val opposingTeams: Map<String, Int> = emptyMap(),  // For defense tracking

    // Schedule state
    val scheduleLoaded: Boolean = false,
    val scheduleEventName: String = "",
    val scheduleMatchCount: Int = 0,
    val isManualEntryMode: Boolean = false,

    // Action records
    val actionRecords: List<ActionRecord> = emptyList(),

    // UI state
    val isSubmitting: Boolean = false,
    val isLoadingSchedule: Boolean = false,
    val errors: List<String> = emptyList()
) {
    // Helper to get action count
    fun getActionCount(phase: MatchPhase, actionType: ActionType): Int {
        return actionRecords.count { it.phase == phase && it.actionType == actionType }
    }

    // Helper to get total time for an action
    fun getTotalActionTime(phase: MatchPhase, actionType: ActionType): Long {
        return actionRecords
            .filter { it.phase == phase && it.actionType == actionType }
            .sumOf { it.durationMs }
    }
}

class MatchScoutingViewModel(
    private val repository: ScoutRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MatchScoutState())
    val state: StateFlow<MatchScoutState> = _state.asStateFlow()

    /**
     * Initialize schedule state from repository.
     */
    fun initializeScheduleState(context: Context) {
        val schedule = ScheduleRepository.getCurrentSchedule()
        val isManualMode = ScheduleRepository.isManualEntryMode(context)

        _state.value = _state.value.copy(
            scheduleLoaded = schedule != null,
            scheduleEventName = schedule?.eventName ?: "",
            scheduleMatchCount = schedule?.matches?.size ?: 0,
            isManualEntryMode = isManualMode
        )
    }

    fun updateEvent(event: String, eventCode: String = "") {
        _state.value = _state.value.copy(event = event, eventCode = eventCode)
        tryAutoFillTeamNumber()
    }

    fun updateMatchNumber(matchNumber: String) {
        _state.value = _state.value.copy(matchNumber = matchNumber)
        tryAutoFillTeamNumber()
    }

    fun updateRobotDesignation(designation: String) {
        _state.value = _state.value.copy(robotDesignation = designation)
        tryAutoFillTeamNumber()
    }

    /**
     * Try to auto-fill team number and opposing teams from schedule.
     */
    private fun tryAutoFillTeamNumber() {
        val currentState = _state.value

        // Need match number and robot designation
        val matchNum = currentState.matchNumber.toIntOrNull() ?: return
        val designation = currentState.robotDesignation
        if (designation.isBlank()) return

        // Get opposing teams (always update this if we have schedule)
        val opposingTeams = ScheduleRepository.getOpposingTeams(matchNum, designation) ?: emptyMap()

        // Don't auto-fill team number if in manual entry mode
        if (currentState.isManualEntryMode) {
            _state.value = _state.value.copy(opposingTeams = opposingTeams)
            return
        }

        // Try to get team number from schedule
        val teamNumber = ScheduleRepository.getTeamNumber(matchNum, designation)
        if (teamNumber != null && teamNumber > 0) {
            _state.value = _state.value.copy(
                teamNumber = teamNumber.toString(),
                teamNumberAutoFilled = true,
                opposingTeams = opposingTeams
            )
        } else {
            _state.value = _state.value.copy(opposingTeams = opposingTeams)
        }
    }

    /**
     * Load schedule for an event.
     */
    fun loadSchedule(context: Context, eventCode: String, eventName: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingSchedule = true)

            when (val result = ScheduleRepository.loadSchedule(context, eventCode, eventName)) {
                is LoadResult.Success -> {
                    val sourceText = when (result.source) {
                        ScheduleSource.CACHE -> "cache"
                        ScheduleSource.API -> "The Blue Alliance"
                        ScheduleSource.FILE -> "file"
                    }
                    _state.value = _state.value.copy(
                        isLoadingSchedule = false,
                        scheduleLoaded = true,
                        scheduleEventName = eventName,
                        scheduleMatchCount = result.matchCount,
                        isManualEntryMode = false
                    )
                    ScheduleRepository.setManualEntryMode(context, false)
                    onResult(true, "Loaded ${result.matchCount} matches from $sourceText")
                    tryAutoFillTeamNumber()
                }
                is LoadResult.Failed -> {
                    _state.value = _state.value.copy(isLoadingSchedule = false)
                    onResult(false, "Failed to load schedule")
                }
            }
        }
    }

    /**
     * Import schedule from a file.
     */
    fun importScheduleFromFile(context: Context, uri: Uri, eventCode: String, eventName: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingSchedule = true)

            when (val result = ScheduleRepository.importFromFile(context, uri, eventCode, eventName)) {
                is LoadResult.Success -> {
                    _state.value = _state.value.copy(
                        isLoadingSchedule = false,
                        scheduleLoaded = true,
                        scheduleEventName = eventName,
                        scheduleMatchCount = result.matchCount,
                        isManualEntryMode = false
                    )
                    ScheduleRepository.setManualEntryMode(context, false)
                    onResult(true, "Imported ${result.matchCount} matches from file")
                    tryAutoFillTeamNumber()
                }
                is LoadResult.Failed -> {
                    _state.value = _state.value.copy(isLoadingSchedule = false)
                    onResult(false, "Failed to import schedule")
                }
            }
        }
    }

    /**
     * Enable manual entry mode.
     */
    fun enableManualEntryMode(context: Context) {
        ScheduleRepository.setManualEntryMode(context, true)
        _state.value = _state.value.copy(isManualEntryMode = true)
    }

    fun updateScoutName(name: String) {
        _state.value = _state.value.copy(scoutName = name)
    }

    fun updateTeamNumber(teamNumber: String) {
        _state.value = _state.value.copy(teamNumber = teamNumber)
    }

    fun updateStartPosition(position: String) {
        _state.value = _state.value.copy(startPosition = position)
    }

    fun toggleLoaded() {
        _state.value = _state.value.copy(loaded = !_state.value.loaded)
    }

    fun toggleNoShow() {
        _state.value = _state.value.copy(noShow = !_state.value.noShow)
    }

    fun toggleFieldOrientation() {
        _state.value = _state.value.copy(isBlueRight = !_state.value.isBlueRight)
    }

    fun addActionRecord(record: ActionRecord) {
        val currentRecords = _state.value.actionRecords.toMutableList()
        currentRecords.add(record)
        _state.value = _state.value.copy(actionRecords = currentRecords)
    }

    fun removeLastActionRecord(phase: MatchPhase, actionType: ActionType) {
        val currentRecords = _state.value.actionRecords.toMutableList()
        val lastIndex = currentRecords.indexOfLast { it.phase == phase && it.actionType == actionType }
        if (lastIndex >= 0) {
            currentRecords.removeAt(lastIndex)
            _state.value = _state.value.copy(actionRecords = currentRecords)
        }
    }

    fun submitMatch(onSuccess: (Long) -> Unit) {
        val currentState = _state.value

        // Validation
        val errors = mutableListOf<String>()
        if (currentState.event.isBlank()) errors.add("Event is required")
        if (currentState.matchNumber.isBlank()) errors.add("Match number is required")
        if (currentState.robotDesignation.isBlank()) errors.add("Robot designation is required")
        if (currentState.scoutName.isBlank()) errors.add("Scout name is required")
        if (currentState.teamNumber.isBlank()) errors.add("Team number is required")

        if (errors.isNotEmpty()) {
            _state.value = _state.value.copy(errors = errors)
            return
        }

        _state.value = _state.value.copy(isSubmitting = true, errors = emptyList())

        viewModelScope.launch {
            try {
                val matchScoutData = MatchScoutData(
                    event = currentState.event,
                    matchNumber = currentState.matchNumber,
                    robotDesignation = currentState.robotDesignation,
                    scoutName = currentState.scoutName,
                    teamNumber = currentState.teamNumber,
                    startPosition = currentState.startPosition,
                    loaded = currentState.loaded,
                    actionRecords = currentState.actionRecords
                )

                val matchScoutId = repository.saveMatchScout(matchScoutData)
                onSuccess(matchScoutId)

                // Reset state for new match, preserving some fields
                val nextMatchNumber = (currentState.matchNumber.toIntOrNull() ?: 0) + 1
                _state.value = MatchScoutState(
                    // Keep these fields
                    event = currentState.event,
                    eventCode = currentState.eventCode,
                    robotDesignation = currentState.robotDesignation,
                    scoutName = currentState.scoutName,
                    isBlueRight = currentState.isBlueRight,
                    // Increment match number
                    matchNumber = nextMatchNumber.toString(),
                    // Keep schedule state
                    scheduleLoaded = currentState.scheduleLoaded,
                    scheduleEventName = currentState.scheduleEventName,
                    scheduleMatchCount = currentState.scheduleMatchCount,
                    isManualEntryMode = currentState.isManualEntryMode
                    // These reset to defaults: teamNumber, startPosition, loaded, actionRecords
                )
                // Auto-fill team number for new match
                tryAutoFillTeamNumber()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    errors = listOf("Error saving match: ${e.message}")
                )
            }
        }
    }

    fun clearErrors() {
        _state.value = _state.value.copy(errors = emptyList())
    }
}

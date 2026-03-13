package com.example.vectorscout26.ui.foul

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.vectorscout26.data.model.FoulTeamEntry
import com.example.vectorscout26.data.repository.ScheduleRepository
import com.example.vectorscout26.utils.FoulJsonSerializer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private val DESIGNATIONS = listOf("Red1", "Red2", "Red3", "Blue1", "Blue2", "Blue3")

data class FoulScoutState(
    val event: String = "",
    val matchNumber: String = "",
    val scheduleLoaded: Boolean = false,
    val teams: List<FoulTeamEntry> = DESIGNATIONS.map { FoulTeamEntry(it) },
    val qrJson: String = ""
)

class FoulScoutingViewModel : ViewModel() {

    private val _state = MutableStateFlow(FoulScoutState())
    val state: StateFlow<FoulScoutState> = _state.asStateFlow()

    fun initializeState(context: Context) {
        val schedule = ScheduleRepository.getCurrentSchedule()
        _state.value = _state.value.copy(
            event = schedule?.eventName ?: "",
            scheduleLoaded = schedule != null
        )
    }

    fun updateMatchNumber(matchNumber: String) {
        _state.value = _state.value.copy(matchNumber = matchNumber, qrJson = "")
        tryAutoFillTeams()
    }

    private fun tryAutoFillTeams() {
        val matchNum = _state.value.matchNumber.toIntOrNull() ?: return
        val schedule = ScheduleRepository.getCurrentSchedule() ?: return
        val match = schedule.getMatch(matchNum) ?: return

        val updated = DESIGNATIONS.map { designation ->
            val teamNum = match.getTeamNumber(designation) ?: 0
            FoulTeamEntry(designation = designation, teamNumber = teamNum)
        }
        _state.value = _state.value.copy(teams = updated)
    }

    fun updateTeamNumber(designation: String, teamNumber: Int) {
        val updated = _state.value.teams.map { entry ->
            if (entry.designation == designation) entry.copy(teamNumber = teamNumber) else entry
        }
        _state.value = _state.value.copy(teams = updated, qrJson = "")
    }

    fun incrementFoul(designation: String, isMajor: Boolean) {
        val updated = _state.value.teams.map { entry ->
            if (entry.designation == designation) {
                if (isMajor) entry.copy(majorCount = entry.majorCount + 1)
                else entry.copy(minorCount = entry.minorCount + 1)
            } else entry
        }
        _state.value = _state.value.copy(teams = updated, qrJson = "")
    }

    fun decrementFoul(designation: String, isMajor: Boolean) {
        val updated = _state.value.teams.map { entry ->
            if (entry.designation == designation) {
                if (isMajor) entry.copy(majorCount = maxOf(0, entry.majorCount - 1))
                else entry.copy(minorCount = maxOf(0, entry.minorCount - 1))
            } else entry
        }
        _state.value = _state.value.copy(teams = updated, qrJson = "")
    }

    fun generateQR(): String {
        val state = _state.value
        val matchNum = state.matchNumber.toIntOrNull() ?: 0
        val json = FoulJsonSerializer.toCompactJson(
            event = state.event,
            matchNumber = matchNum,
            teams = state.teams
        )
        _state.value = _state.value.copy(qrJson = json)
        return json
    }

    fun resetForNewMatch() {
        val nextMatch = (_state.value.matchNumber.toIntOrNull() ?: 0) + 1
        _state.value = _state.value.copy(
            matchNumber = nextMatch.toString(),
            teams = DESIGNATIONS.map { FoulTeamEntry(it) },
            qrJson = ""
        )
        tryAutoFillTeams()
    }
}

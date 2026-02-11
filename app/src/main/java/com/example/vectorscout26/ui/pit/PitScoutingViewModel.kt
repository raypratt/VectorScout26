package com.example.vectorscout26.ui.pit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vectorscout26.data.model.*
import com.example.vectorscout26.data.repository.PitScoutRepository
import com.example.vectorscout26.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PitScoutState(
    val event: String = "",
    val eventCode: String = "",
    val teamNumber: String = "",
    val drivetrainType: String = "",
    val preferredRole: String = "",
    val preferredPath: String = "",
    val photoPath: String? = null,
    val autoPaths: List<AutoPathState> = listOf(AutoPathState()),
    val currentPathIndex: Int = 0,
    val isSubmitting: Boolean = false,
    val errors: List<String> = emptyList()
)

data class AutoPathState(
    val name: String = "A1",
    val steps: List<AutoPathStep> = emptyList(),
    val currentCategory: PathCategory = PathCategory.START,
    val drawingPath: String? = null,
    val drawingStrokes: List<List<Pair<Float, Float>>> = emptyList()  // List of strokes, each stroke is list of points
)

class PitScoutingViewModel(
    private val repository: PitScoutRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PitScoutState())
    val state: StateFlow<PitScoutState> = _state.asStateFlow()

    // Update event
    fun updateEvent(event: String, eventCode: String) {
        _state.value = _state.value.copy(event = event, eventCode = eventCode)
    }

    // Update team number
    fun updateTeamNumber(teamNumber: String) {
        _state.value = _state.value.copy(teamNumber = teamNumber)
    }

    // Update drivetrain type
    fun updateDrivetrainType(type: String) {
        _state.value = _state.value.copy(drivetrainType = type)
    }

    // Update preferred role
    fun updatePreferredRole(role: String) {
        _state.value = _state.value.copy(preferredRole = role)
    }

    // Update preferred path
    fun updatePreferredPath(path: String) {
        _state.value = _state.value.copy(preferredPath = path)
    }

    // Update photo path
    fun updatePhotoPath(path: String?) {
        _state.value = _state.value.copy(photoPath = path)
    }

    // Switch to a different auto path tab
    fun selectAutoPath(index: Int) {
        _state.value = _state.value.copy(currentPathIndex = index)
    }

    // Add a new auto path
    fun addAutoPath() {
        val currentPaths = _state.value.autoPaths.toMutableList()
        val newName = "A${currentPaths.size + 1}"
        currentPaths.add(AutoPathState(name = newName))
        _state.value = _state.value.copy(
            autoPaths = currentPaths,
            currentPathIndex = currentPaths.size - 1
        )
    }

    // Remove current auto path
    fun removeCurrentAutoPath() {
        val currentPaths = _state.value.autoPaths.toMutableList()
        if (currentPaths.size <= 1) return // Keep at least one path

        val currentIndex = _state.value.currentPathIndex
        currentPaths.removeAt(currentIndex)

        // Rename remaining paths
        currentPaths.forEachIndexed { index, path ->
            currentPaths[index] = path.copy(name = "A${index + 1}")
        }

        val newIndex = if (currentIndex >= currentPaths.size) currentPaths.size - 1 else currentIndex
        _state.value = _state.value.copy(
            autoPaths = currentPaths,
            currentPathIndex = newIndex
        )
    }

    // Add a step to the current auto path
    fun addStep(step: AutoPathStep) {
        val currentPaths = _state.value.autoPaths.toMutableList()
        val currentIndex = _state.value.currentPathIndex
        val currentPath = currentPaths[currentIndex]

        val newSteps = currentPath.steps + step
        val nextCategory = getNextCategory(newSteps)

        // If Climb action is added, auto-complete with L1
        if (step.type == StepType.ACTION && step.value == "Climb") {
            val climbStep = AutoPathStep(StepType.LOCATION, "L1")
            val stepsWithClimb = newSteps + climbStep
            currentPaths[currentIndex] = currentPath.copy(
                steps = stepsWithClimb,
                currentCategory = PathCategory.ACTION
            )
        } else {
            currentPaths[currentIndex] = currentPath.copy(
                steps = newSteps,
                currentCategory = nextCategory
            )
        }

        _state.value = _state.value.copy(autoPaths = currentPaths)
    }

    // Delete a step and all subsequent steps from the current auto path
    fun deleteStepAndAfter(stepIndex: Int) {
        val currentPaths = _state.value.autoPaths.toMutableList()
        val currentIndex = _state.value.currentPathIndex
        val currentPath = currentPaths[currentIndex]

        val newSteps = currentPath.steps.take(stepIndex)
        val nextCategory = getNextCategory(newSteps)

        currentPaths[currentIndex] = currentPath.copy(
            steps = newSteps,
            currentCategory = nextCategory
        )

        _state.value = _state.value.copy(autoPaths = currentPaths)
    }

    // Clear all steps from current auto path
    fun clearCurrentPath() {
        val currentPaths = _state.value.autoPaths.toMutableList()
        val currentIndex = _state.value.currentPathIndex
        val currentPath = currentPaths[currentIndex]

        currentPaths[currentIndex] = currentPath.copy(
            steps = emptyList(),
            currentCategory = PathCategory.START
        )

        _state.value = _state.value.copy(autoPaths = currentPaths)
    }

    // Update drawing path for current auto path
    fun updateDrawingPath(path: String?) {
        val currentPaths = _state.value.autoPaths.toMutableList()
        val currentIndex = _state.value.currentPathIndex
        val currentPath = currentPaths[currentIndex]

        currentPaths[currentIndex] = currentPath.copy(drawingPath = path)
        _state.value = _state.value.copy(autoPaths = currentPaths)
    }

    // Update drawing strokes for current auto path
    fun updateDrawingStrokes(strokes: List<List<Pair<Float, Float>>>) {
        val currentPaths = _state.value.autoPaths.toMutableList()
        val currentIndex = _state.value.currentPathIndex
        val currentPath = currentPaths[currentIndex]

        currentPaths[currentIndex] = currentPath.copy(drawingStrokes = strokes)
        _state.value = _state.value.copy(autoPaths = currentPaths)
    }

    // Clear drawing strokes for current auto path
    fun clearDrawingStrokes() {
        updateDrawingStrokes(emptyList())
    }

    // Undo last stroke for current auto path
    fun undoLastStroke() {
        val currentPath = _state.value.autoPaths[_state.value.currentPathIndex]
        if (currentPath.drawingStrokes.isNotEmpty()) {
            updateDrawingStrokes(currentPath.drawingStrokes.dropLast(1))
        }
    }

    // Get location options based on the last action
    fun getLocationOptionsForCurrentPath(): List<String> {
        val currentPath = _state.value.autoPaths[_state.value.currentPathIndex]
        val lastAction = findLastAction(currentPath.steps)
        return when (lastAction) {
            "Load" -> Constants.LOAD_LOCATIONS
            "Score" -> Constants.PIT_SCORE_LOCATIONS
            "Ferry" -> Constants.PIT_FERRY_LOCATIONS
            "Move" -> Constants.MOVE_LOCATIONS
            "Climb" -> Constants.PIT_CLIMB_LOCATIONS
            else -> emptyList()
        }
    }

    // Submit pit scout data
    fun submitPitScout(onSuccess: (Long) -> Unit) {
        val currentState = _state.value
        val errors = mutableListOf<String>()

        // Validation
        if (currentState.event.isBlank()) {
            errors.add("Event is required")
        }
        if (currentState.teamNumber.isBlank()) {
            errors.add("Team number is required")
        }
        if (currentState.drivetrainType.isBlank()) {
            errors.add("Drivetrain type is required")
        }
        if (currentState.preferredRole.isBlank()) {
            errors.add("Preferred role is required")
        }
        if (currentState.preferredPath.isBlank()) {
            errors.add("Preferred path is required")
        }

        // Check if at least one auto path has steps
        val hasValidPath = currentState.autoPaths.any { it.steps.isNotEmpty() }
        if (!hasValidPath) {
            errors.add("At least one auto path is required")
        }

        if (errors.isNotEmpty()) {
            _state.value = currentState.copy(errors = errors)
            return
        }

        _state.value = currentState.copy(isSubmitting = true, errors = emptyList())

        viewModelScope.launch {
            try {
                val teamNum = currentState.teamNumber.toIntOrNull() ?: 0

                val autoPaths = currentState.autoPaths
                    .filter { it.steps.isNotEmpty() }
                    .map { pathState ->
                        AutoPath(
                            name = pathState.name,
                            steps = pathState.steps,
                            drawingPath = pathState.drawingPath
                        )
                    }

                val pitScoutData = PitScoutData(
                    event = currentState.event,
                    teamNumber = teamNum,
                    drivetrainType = currentState.drivetrainType,
                    preferredRole = currentState.preferredRole,
                    preferredPath = currentState.preferredPath,
                    photoPath = currentState.photoPath,
                    autoPaths = autoPaths
                )

                val id = repository.savePitScout(pitScoutData)
                _state.value = _state.value.copy(isSubmitting = false)
                onSuccess(id)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    errors = listOf("Failed to save: ${e.message}")
                )
            }
        }
    }

    // Clear errors
    fun clearErrors() {
        _state.value = _state.value.copy(errors = emptyList())
    }

    // Reset state for new pit scout (preserves event selection)
    fun resetStateForNewScout() {
        val currentEvent = _state.value.event
        val currentEventCode = _state.value.eventCode
        _state.value = PitScoutState(
            event = currentEvent,
            eventCode = currentEventCode
        )
    }

    // Full reset (clears everything including event)
    fun resetState() {
        _state.value = PitScoutState()
    }
}

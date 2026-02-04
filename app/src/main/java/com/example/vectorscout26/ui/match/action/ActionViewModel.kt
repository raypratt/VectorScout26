package com.example.vectorscout26.ui.match.action

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ActionState(
    val startTimeMs: Long = System.currentTimeMillis(),
    val elapsedMs: Long = 0,
    val isRunning: Boolean = true
)

class ActionViewModel : ViewModel() {
    private val _state = MutableStateFlow(ActionState())
    val state: StateFlow<ActionState> = _state.asStateFlow()

    init {
        startTimer()
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (_state.value.isRunning) {
                delay(100) // Update every 100ms for smooth display
                val elapsed = System.currentTimeMillis() - _state.value.startTimeMs
                _state.value = _state.value.copy(elapsedMs = elapsed)
            }
        }
    }

    fun stopTimer() {
        _state.value = _state.value.copy(isRunning = false)
    }

    fun getElapsedTime(): Long = _state.value.elapsedMs
}

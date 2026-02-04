package com.example.vectorscout26.data.model

data class MatchScoutData(
    val id: Long = 0,
    // Pre-match fields
    val event: String = "",
    val matchNumber: String = "",
    val robotDesignation: String = "",  // "Red1", "Blue3", etc.
    val scoutName: String = "",
    val teamNumber: String = "",
    val startPosition: String = "",  // Zone from field map
    val loaded: Boolean = false,
    val noShow: Boolean = false,

    // Action records
    val actionRecords: List<ActionRecord> = emptyList(),

    // Metadata
    val timestamp: Long = System.currentTimeMillis(),
    val qrGenerated: Boolean = false
) {
    // Helper methods to aggregate action data
    fun getActionCount(phase: MatchPhase, actionType: ActionType): Int {
        return actionRecords.count { it.phase == phase && it.actionType == actionType }
    }

    fun getTotalActionTime(phase: MatchPhase, actionType: ActionType): Long {
        return actionRecords
            .filter { it.phase == phase && it.actionType == actionType }
            .sumOf { it.durationMs }
    }
}

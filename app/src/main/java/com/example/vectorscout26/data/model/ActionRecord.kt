package com.example.vectorscout26.data.model

data class ActionRecord(
    val phase: MatchPhase,
    val actionType: ActionType,
    val startTimeMs: Long,
    val endTimeMs: Long? = null,
    val qualitativeData: QualitativeData? = null
) {
    val durationMs: Long
        get() = if (endTimeMs != null) endTimeMs - startTimeMs else 0L
}

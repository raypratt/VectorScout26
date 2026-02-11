package com.example.vectorscout26.data.model

data class PitScoutData(
    val id: Long = 0,
    val event: String = "",
    val teamNumber: Int = 0,
    val drivetrainType: String = "",
    val preferredRole: String = "",
    val preferredPath: String = "",
    val photoPath: String? = null,
    val autoPaths: List<AutoPath> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class AutoPath(
    val id: Long = 0,
    val name: String = "A1",
    val steps: List<AutoPathStep> = emptyList(),
    val drawingPath: String? = null
)

data class AutoPathStep(
    val type: StepType,
    val value: String
)

enum class StepType {
    START,
    ACTION,
    LOCATION
}

// Helper to determine next category based on current steps
fun getNextCategory(steps: List<AutoPathStep>): PathCategory {
    if (steps.isEmpty()) return PathCategory.START
    val lastStep = steps.last()
    return when (lastStep.type) {
        StepType.START -> PathCategory.ACTION
        StepType.ACTION -> PathCategory.LOCATION
        StepType.LOCATION -> PathCategory.ACTION
    }
}

// Helper to get location options based on last action
fun getLocationOptions(lastAction: String): List<String> {
    return when (lastAction) {
        "Load" -> listOf("Neutral", "Depot", "Outpost", "Alliance")
        "Score" -> listOf("H", "R1", "R2", "L1", "L2")
        "Ferry" -> listOf("Shoot Alliance", "Dump Alliance", "Dump Outpost")
        "Move" -> listOf("Neutral", "Alliance", "Depot", "Outpost")
        "Climb" -> listOf("L1")
        else -> emptyList()
    }
}

// Helper to find the last action in steps
fun findLastAction(steps: List<AutoPathStep>): String? {
    return steps.lastOrNull { it.type == StepType.ACTION }?.value
}

enum class PathCategory {
    START,
    ACTION,
    LOCATION
}

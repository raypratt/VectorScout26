package com.example.vectorscout26.data.model

sealed class ActionType(
    val name: String,
    val hasTimer: Boolean,
    val hasCounter: Boolean
) {
    // Auton + Teleop
    object Load : ActionType("Load", hasTimer = true, hasCounter = true)
    object Shoot : ActionType("Shoot", hasTimer = true, hasCounter = true)
    object Ferry : ActionType("Ferry", hasTimer = true, hasCounter = true)

    // Auton only
    object AutonClimb : ActionType("Climb", hasTimer = true, hasCounter = true)

    // Teleop only
    object Defense : ActionType("Defense", hasTimer = true, hasCounter = true)
    object Incapacitated : ActionType("Incapacitated", hasTimer = true, hasCounter = true)
    object Tipped : ActionType("Tipped", hasTimer = true, hasCounter = true)

    // Special cases - no timer, no counter
    object Foul : ActionType("Foul", hasTimer = false, hasCounter = false)
    object Damaged : ActionType("Damaged", hasTimer = false, hasCounter = false)

    // End game
    object EndGameClimb : ActionType("Climb", hasTimer = true, hasCounter = true)

    companion object {
        fun fromString(name: String, phase: MatchPhase): ActionType? {
            return when (phase) {
                MatchPhase.AUTON -> when (name.uppercase()) {
                    "LOAD" -> Load
                    "SHOOT" -> Shoot
                    "FERRY" -> Ferry
                    "CLIMB" -> AutonClimb
                    "FOUL" -> Foul
                    else -> null
                }
                MatchPhase.TELEOP -> when (name.uppercase()) {
                    "LOAD" -> Load
                    "SHOOT" -> Shoot
                    "FERRY" -> Ferry
                    "DEFENSE" -> Defense
                    "FOUL" -> Foul
                    "INCAPACITATED" -> Incapacitated
                    "TIPPED" -> Tipped
                    "DAMAGED" -> Damaged
                    else -> null
                }
                MatchPhase.ENDGAME -> when (name.uppercase()) {
                    "CLIMB" -> EndGameClimb
                    else -> null
                }
            }
        }
    }
}

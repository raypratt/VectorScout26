package com.example.vectorscout26.data.model

/**
 * Represents a single match in the schedule.
 */
data class MatchScheduleEntry(
    val matchNumber: Int,
    val red1: Int,
    val red2: Int,
    val red3: Int,
    val blue1: Int,
    val blue2: Int,
    val blue3: Int
) {
    /**
     * Get team number by robot designation (Red1, Red2, Red3, Blue1, Blue2, Blue3)
     */
    fun getTeamNumber(robotDesignation: String): Int? {
        return when (robotDesignation) {
            "Red1" -> red1
            "Red2" -> red2
            "Red3" -> red3
            "Blue1" -> blue1
            "Blue2" -> blue2
            "Blue3" -> blue3
            else -> null
        }
    }

    /**
     * Get opposing alliance teams as a map of designation to team number.
     * If scouting a Red robot, returns Blue teams. If scouting Blue, returns Red teams.
     */
    fun getOpposingTeams(robotDesignation: String): Map<String, Int> {
        val isRed = robotDesignation.startsWith("Red")
        return if (isRed) {
            mapOf("Blue1" to blue1, "Blue2" to blue2, "Blue3" to blue3)
        } else {
            mapOf("Red1" to red1, "Red2" to red2, "Red3" to red3)
        }
    }
}

/**
 * Represents a complete event schedule.
 */
data class EventSchedule(
    val eventCode: String,
    val eventName: String,
    val matches: List<MatchScheduleEntry>
) {
    /**
     * Get a match by match number.
     */
    fun getMatch(matchNumber: Int): MatchScheduleEntry? {
        return matches.find { it.matchNumber == matchNumber }
    }

    /**
     * Get team number for a specific match and robot designation.
     */
    fun getTeamNumber(matchNumber: Int, robotDesignation: String): Int? {
        return getMatch(matchNumber)?.getTeamNumber(robotDesignation)
    }
}

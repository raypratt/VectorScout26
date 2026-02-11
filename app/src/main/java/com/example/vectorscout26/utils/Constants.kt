package com.example.vectorscout26.utils

object Constants {
    // Robot Designations
    val ROBOT_DESIGNATIONS = listOf(
        "Red1", "Red2", "Red3",
        "Blue1", "Blue2", "Blue3"
    )

    // Start Locations (field map zones)
    val START_LOCATIONS = listOf("L1", "L2", "L3", "L3a", "L3b", "L4", "L5")

    // Shoot Locations (shoot zones)
    val SHOOT_LOCATIONS = listOf("H", "R1", "R2", "L1", "L2")

    // Field Zones (5x5 grid) - used for shoot, load, ferry locations
    val FIELD_ZONES = listOf(
        "A1", "A2", "A3", "A4", "A5",
        "B1", "B2", "B3", "B4", "B5",
        "C1", "C2", "C3", "C4", "C5",
        "D1", "D2", "D3", "D4", "D5",
        "E1", "E2", "E3", "E4", "E5"
    )

    // Ferry options
    val FERRY_TYPES = listOf("Shoot", "Dump")

    // Climb options
    val AUTON_CLIMB_RESULTS = listOf("L1", "Fail")
    val TELEOP_CLIMB_RESULTS = listOf("L1", "L2", "L3", "Fail")

    // Defense options
    val DEFENSE_TYPES = listOf("Pin", "Altered Shot", "Block")

    // Foul options
    val FOUL_TYPES = listOf("Major", "Minor")

    // Damaged options
    val DAMAGED_COMPONENTS = listOf("Drivetrain", "Intake", "Shooter", "Climber")

    // Pit Scouting - Drivetrain types
    val DRIVETRAIN_TYPES = listOf("Swerve", "Tank", "Mecanum", "Other")

    // Pit Scouting - Preferred roles
    val PREFERRED_ROLES = listOf("Score", "Ferry", "Defense")

    // Pit Scouting - Preferred paths
    val PREFERRED_PATHS = listOf("Trench", "Bump", "Both")

    // Pit Scouting - Auto path start positions (no L prefix)
    val PIT_START_POSITIONS = listOf("1", "2", "3", "3a", "3b", "4", "5")

    // Pit Scouting - Auto path actions
    val PATH_ACTIONS = listOf("Load", "Score", "Ferry", "Move", "Climb")

    // Pit Scouting - Location options by action
    val LOAD_LOCATIONS = listOf("Neutral", "Depot", "Outpost", "Alliance")
    val PIT_SCORE_LOCATIONS = listOf("H", "R1", "R2", "L1", "L2")
    val PIT_FERRY_LOCATIONS = listOf("Shoot Alliance", "Dump Alliance", "Dump Outpost")
    val MOVE_LOCATIONS = listOf("Neutral", "Alliance", "Depot", "Outpost")
    val PIT_CLIMB_LOCATIONS = listOf("L1")
}

object FieldZones {
    /**
     * Convert tap coordinates to zone name
     */
    fun getZoneFromCoordinates(x: Float, y: Float, width: Float, height: Float): String {
        val col = ((x / width) * 5).toInt().coerceIn(0, 4)
        val row = ((y / height) * 5).toInt().coerceIn(0, 4)
        val colChar = ('A' + row)
        return "$colChar${col + 1}"
    }

    /**
     * Get zone center coordinates for highlighting
     */
    fun getZoneCenter(zone: String, width: Float, height: Float): Pair<Float, Float> {
        val row = zone[0] - 'A'
        val col = zone[1].digitToInt() - 1
        val cellWidth = width / 5f
        val cellHeight = height / 5f
        return Pair(
            col * cellWidth + cellWidth / 2,
            row * cellHeight + cellHeight / 2
        )
    }
}

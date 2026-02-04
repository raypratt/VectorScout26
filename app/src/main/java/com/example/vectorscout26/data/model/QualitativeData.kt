package com.example.vectorscout26.data.model

import com.google.gson.Gson

// Base interface for all qualitative data
sealed interface QualitativeData {
    fun toJson(): String = Gson().toJson(this)
}

data class LoadData(
    val loadLocation: String  // Zone from map
) : QualitativeData

data class ShootData(
    val location: String  // Zone from map
) : QualitativeData

data class FerryData(
    val ferryType: String,  // "Shoot", "Dump"
    val ferryDelivery: String  // Zone from map
) : QualitativeData

data class ClimbData(
    val result: String,  // "L1", "L2", "L3", "Fail" (depends on phase)
    val phase: MatchPhase
) : QualitativeData

data class DefenseData(
    val types: List<String>,  // ["Pin", "Altered Shot", "Block"]
    val targetRobot: String   // Which robot they played defense on (e.g., "Red1", "Blue2")
) : QualitativeData

data class FoulData(
    val type: String  // "Major", "Minor"
) : QualitativeData

data class DamagedData(
    val components: List<String>  // ["Drivetrain", "Intake", "Shooter", "Climber"]
) : QualitativeData

data class EmptyData(
    val placeholder: String = ""
) : QualitativeData  // For Incapacitated, Tipped with no fields

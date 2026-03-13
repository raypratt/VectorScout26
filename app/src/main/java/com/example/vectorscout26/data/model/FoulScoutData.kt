package com.example.vectorscout26.data.model

data class FoulTeamEntry(
    val designation: String,   // "Red1", "Red2", "Red3", "Blue1", "Blue2", "Blue3"
    val teamNumber: Int = 0,
    val majorCount: Int = 0,
    val minorCount: Int = 0
)

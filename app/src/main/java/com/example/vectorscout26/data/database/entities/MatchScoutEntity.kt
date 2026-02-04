package com.example.vectorscout26.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "match_scouts")
data class MatchScoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val event: String,
    val matchNumber: String,
    val robotDesignation: String,  // "Red1", "Blue3", etc.
    val scoutName: String,
    val teamNumber: String,
    val startPosition: String,  // Zone name from field map
    val loaded: Boolean,
    val timestamp: Long,
    val qrGenerated: Boolean = false
)

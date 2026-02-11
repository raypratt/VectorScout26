package com.example.vectorscout26.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pit_scouts")
data class PitScoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val event: String,
    val teamNumber: Int,
    val drivetrainType: String,
    val preferredRole: String,
    val preferredPath: String,
    val photoPath: String?,
    val timestamp: Long
)

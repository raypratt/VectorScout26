package com.example.vectorscout26.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "action_records",
    foreignKeys = [
        ForeignKey(
            entity = MatchScoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["matchScoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("matchScoutId")]
)
data class ActionRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val matchScoutId: Long,  // Foreign key to MatchScoutEntity
    val phase: String,  // "AUTON", "TELEOP", "ENDGAME"
    val actionType: String,  // "LOAD", "VOLLEY", "FERRY", etc.
    val startTimeMs: Long,
    val endTimeMs: Long,
    val qualitativeData: String  // JSON string of action-specific fields
)

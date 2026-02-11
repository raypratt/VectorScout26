package com.example.vectorscout26.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "auto_paths",
    foreignKeys = [
        ForeignKey(
            entity = PitScoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["pitScoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["pitScoutId"])]
)
data class AutoPathEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pitScoutId: Long,
    val name: String,
    val stepsJson: String,
    val drawingPath: String?
)

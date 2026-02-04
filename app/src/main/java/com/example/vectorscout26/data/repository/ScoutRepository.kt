package com.example.vectorscout26.data.repository

import com.example.vectorscout26.data.database.dao.MatchScoutDao
import com.example.vectorscout26.data.database.entities.ActionRecordEntity
import com.example.vectorscout26.data.database.entities.MatchScoutEntity
import com.example.vectorscout26.data.model.*
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ScoutRepository(private val matchScoutDao: MatchScoutDao) {

    fun getAllMatchScouts(): Flow<List<MatchScoutData>> {
        return matchScoutDao.getAllMatchScouts().map { entities ->
            entities.map { entity ->
                val actionRecords = matchScoutDao.getActionRecordsByMatchId(entity.id)
                entityToMatchScoutData(entity, actionRecords)
            }
        }
    }

    suspend fun getMatchScoutById(matchScoutId: Long): MatchScoutData? {
        val entity = matchScoutDao.getMatchScoutById(matchScoutId) ?: return null
        val actionRecords = matchScoutDao.getActionRecordsByMatchId(matchScoutId)
        return entityToMatchScoutData(entity, actionRecords)
    }

    suspend fun saveMatchScout(matchScoutData: MatchScoutData): Long {
        val entity = MatchScoutEntity(
            id = matchScoutData.id,
            event = matchScoutData.event,
            matchNumber = matchScoutData.matchNumber,
            robotDesignation = matchScoutData.robotDesignation,
            scoutName = matchScoutData.scoutName,
            teamNumber = matchScoutData.teamNumber,
            startPosition = matchScoutData.startPosition,
            loaded = matchScoutData.loaded,
            timestamp = matchScoutData.timestamp,
            qrGenerated = matchScoutData.qrGenerated
        )

        val matchScoutId = matchScoutDao.insertMatchScout(entity)

        val actionRecordEntities = matchScoutData.actionRecords.map { record ->
            ActionRecordEntity(
                matchScoutId = matchScoutId,
                phase = record.phase.name,
                actionType = record.actionType.name,
                startTimeMs = record.startTimeMs,
                endTimeMs = record.endTimeMs ?: record.startTimeMs,
                qualitativeData = record.qualitativeData?.toJson() ?: ""
            )
        }

        if (actionRecordEntities.isNotEmpty()) {
            matchScoutDao.insertActionRecords(actionRecordEntities)
        }

        return matchScoutId
    }

    suspend fun updateMatchScout(matchScoutData: MatchScoutData) {
        val entity = MatchScoutEntity(
            id = matchScoutData.id,
            event = matchScoutData.event,
            matchNumber = matchScoutData.matchNumber,
            robotDesignation = matchScoutData.robotDesignation,
            scoutName = matchScoutData.scoutName,
            teamNumber = matchScoutData.teamNumber,
            startPosition = matchScoutData.startPosition,
            loaded = matchScoutData.loaded,
            timestamp = matchScoutData.timestamp,
            qrGenerated = matchScoutData.qrGenerated
        )

        matchScoutDao.updateMatchScout(entity)

        // Delete old action records and insert new ones
        matchScoutDao.deleteActionRecordsByMatchId(matchScoutData.id)

        val actionRecordEntities = matchScoutData.actionRecords.map { record ->
            ActionRecordEntity(
                matchScoutId = matchScoutData.id,
                phase = record.phase.name,
                actionType = record.actionType.name,
                startTimeMs = record.startTimeMs,
                endTimeMs = record.endTimeMs ?: record.startTimeMs,
                qualitativeData = record.qualitativeData?.toJson() ?: ""
            )
        }

        if (actionRecordEntities.isNotEmpty()) {
            matchScoutDao.insertActionRecords(actionRecordEntities)
        }
    }

    suspend fun deleteMatchScout(matchScoutData: MatchScoutData) {
        val entity = MatchScoutEntity(
            id = matchScoutData.id,
            event = matchScoutData.event,
            matchNumber = matchScoutData.matchNumber,
            robotDesignation = matchScoutData.robotDesignation,
            scoutName = matchScoutData.scoutName,
            teamNumber = matchScoutData.teamNumber,
            startPosition = matchScoutData.startPosition,
            loaded = matchScoutData.loaded,
            timestamp = matchScoutData.timestamp,
            qrGenerated = matchScoutData.qrGenerated
        )
        matchScoutDao.deleteMatchScout(entity)
    }

    private fun entityToMatchScoutData(
        entity: MatchScoutEntity,
        actionRecordEntities: List<ActionRecordEntity>
    ): MatchScoutData {
        val gson = Gson()
        val actionRecords = actionRecordEntities.map { recordEntity ->
            val phase = MatchPhase.valueOf(recordEntity.phase)
            val actionType = ActionType.fromString(recordEntity.actionType, phase)

            // Deserialize qualitativeData based on action type
            val qualData = deserializeQualitativeData(recordEntity.qualitativeData, actionType, phase, gson)

            ActionRecord(
                phase = phase,
                actionType = actionType ?: ActionType.Load,  // Default fallback
                startTimeMs = recordEntity.startTimeMs,
                endTimeMs = recordEntity.endTimeMs,
                qualitativeData = qualData
            )
        }

        return MatchScoutData(
            id = entity.id,
            event = entity.event,
            matchNumber = entity.matchNumber,
            robotDesignation = entity.robotDesignation,
            scoutName = entity.scoutName,
            teamNumber = entity.teamNumber,
            startPosition = entity.startPosition,
            loaded = entity.loaded,
            actionRecords = actionRecords,
            timestamp = entity.timestamp,
            qrGenerated = entity.qrGenerated
        )
    }

    private fun deserializeQualitativeData(
        jsonString: String,
        actionType: ActionType?,
        phase: MatchPhase,
        gson: Gson
    ): QualitativeData? {
        if (jsonString.isBlank()) return null

        return try {
            when (actionType) {
                ActionType.Load -> gson.fromJson(jsonString, LoadData::class.java)
                ActionType.Shoot -> gson.fromJson(jsonString, ShootData::class.java)
                ActionType.Ferry -> gson.fromJson(jsonString, FerryData::class.java)
                ActionType.AutonClimb, ActionType.EndGameClimb -> gson.fromJson(jsonString, ClimbData::class.java)
                ActionType.Defense -> gson.fromJson(jsonString, DefenseData::class.java)
                ActionType.Foul -> gson.fromJson(jsonString, FoulData::class.java)
                ActionType.Damaged -> gson.fromJson(jsonString, DamagedData::class.java)
                ActionType.Incapacitated, ActionType.Tipped -> gson.fromJson(jsonString, EmptyData::class.java)
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}

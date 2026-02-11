package com.example.vectorscout26.data.repository

import com.example.vectorscout26.data.database.dao.PitScoutDao
import com.example.vectorscout26.data.database.entities.AutoPathEntity
import com.example.vectorscout26.data.database.entities.PitScoutEntity
import com.example.vectorscout26.data.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PitScoutRepository(private val pitScoutDao: PitScoutDao) {
    private val gson = Gson()

    fun getAllPitScouts(): Flow<List<PitScoutData>> {
        return pitScoutDao.getAllPitScouts().map { entities ->
            entities.map { entity ->
                val autoPaths = pitScoutDao.getAutoPathsByPitScoutId(entity.id)
                entityToPitScoutData(entity, autoPaths)
            }
        }
    }

    suspend fun getPitScoutById(id: Long): PitScoutData? {
        val entity = pitScoutDao.getPitScoutById(id) ?: return null
        val autoPaths = pitScoutDao.getAutoPathsByPitScoutId(id)
        return entityToPitScoutData(entity, autoPaths)
    }

    suspend fun getPitScoutByTeam(teamNumber: Int): PitScoutData? {
        val entity = pitScoutDao.getPitScoutByTeam(teamNumber) ?: return null
        val autoPaths = pitScoutDao.getAutoPathsByPitScoutId(entity.id)
        return entityToPitScoutData(entity, autoPaths)
    }

    suspend fun savePitScout(pitScoutData: PitScoutData): Long {
        val entity = PitScoutEntity(
            id = pitScoutData.id,
            event = pitScoutData.event,
            teamNumber = pitScoutData.teamNumber,
            drivetrainType = pitScoutData.drivetrainType,
            preferredRole = pitScoutData.preferredRole,
            preferredPath = pitScoutData.preferredPath,
            photoPath = pitScoutData.photoPath,
            timestamp = pitScoutData.timestamp
        )

        val pitScoutId = pitScoutDao.insertPitScout(entity)

        val autoPathEntities = pitScoutData.autoPaths.map { path ->
            AutoPathEntity(
                pitScoutId = pitScoutId,
                name = path.name,
                stepsJson = gson.toJson(path.steps),
                drawingPath = path.drawingPath
            )
        }

        if (autoPathEntities.isNotEmpty()) {
            pitScoutDao.insertAutoPaths(autoPathEntities)
        }

        return pitScoutId
    }

    suspend fun updatePitScout(pitScoutData: PitScoutData) {
        val entity = PitScoutEntity(
            id = pitScoutData.id,
            event = pitScoutData.event,
            teamNumber = pitScoutData.teamNumber,
            drivetrainType = pitScoutData.drivetrainType,
            preferredRole = pitScoutData.preferredRole,
            preferredPath = pitScoutData.preferredPath,
            photoPath = pitScoutData.photoPath,
            timestamp = pitScoutData.timestamp
        )

        pitScoutDao.updatePitScout(entity)

        // Delete old auto paths and insert new ones
        pitScoutDao.deleteAutoPathsByPitScoutId(pitScoutData.id)

        val autoPathEntities = pitScoutData.autoPaths.map { path ->
            AutoPathEntity(
                pitScoutId = pitScoutData.id,
                name = path.name,
                stepsJson = gson.toJson(path.steps),
                drawingPath = path.drawingPath
            )
        }

        if (autoPathEntities.isNotEmpty()) {
            pitScoutDao.insertAutoPaths(autoPathEntities)
        }
    }

    suspend fun deletePitScout(pitScoutData: PitScoutData) {
        val entity = PitScoutEntity(
            id = pitScoutData.id,
            event = pitScoutData.event,
            teamNumber = pitScoutData.teamNumber,
            drivetrainType = pitScoutData.drivetrainType,
            preferredRole = pitScoutData.preferredRole,
            preferredPath = pitScoutData.preferredPath,
            photoPath = pitScoutData.photoPath,
            timestamp = pitScoutData.timestamp
        )
        pitScoutDao.deletePitScout(entity)
    }

    private fun entityToPitScoutData(
        entity: PitScoutEntity,
        autoPathEntities: List<AutoPathEntity>
    ): PitScoutData {
        val autoPaths = autoPathEntities.map { pathEntity ->
            val stepsType = object : TypeToken<List<AutoPathStep>>() {}.type
            val steps: List<AutoPathStep> = try {
                gson.fromJson(pathEntity.stepsJson, stepsType) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }

            AutoPath(
                id = pathEntity.id,
                name = pathEntity.name,
                steps = steps,
                drawingPath = pathEntity.drawingPath
            )
        }

        return PitScoutData(
            id = entity.id,
            event = entity.event,
            teamNumber = entity.teamNumber,
            drivetrainType = entity.drivetrainType,
            preferredRole = entity.preferredRole,
            preferredPath = entity.preferredPath,
            photoPath = entity.photoPath,
            autoPaths = autoPaths,
            timestamp = entity.timestamp
        )
    }
}

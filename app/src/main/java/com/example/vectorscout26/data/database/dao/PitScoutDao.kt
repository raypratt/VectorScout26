package com.example.vectorscout26.data.database.dao

import androidx.room.*
import com.example.vectorscout26.data.database.entities.AutoPathEntity
import com.example.vectorscout26.data.database.entities.PitScoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PitScoutDao {
    @Query("SELECT * FROM pit_scouts ORDER BY timestamp DESC")
    fun getAllPitScouts(): Flow<List<PitScoutEntity>>

    @Query("SELECT * FROM pit_scouts WHERE id = :id")
    suspend fun getPitScoutById(id: Long): PitScoutEntity?

    @Query("SELECT * FROM pit_scouts WHERE teamNumber = :teamNumber")
    suspend fun getPitScoutByTeam(teamNumber: Int): PitScoutEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPitScout(entity: PitScoutEntity): Long

    @Update
    suspend fun updatePitScout(entity: PitScoutEntity)

    @Delete
    suspend fun deletePitScout(entity: PitScoutEntity)

    @Query("SELECT * FROM auto_paths WHERE pitScoutId = :pitScoutId")
    suspend fun getAutoPathsByPitScoutId(pitScoutId: Long): List<AutoPathEntity>

    @Insert
    suspend fun insertAutoPaths(paths: List<AutoPathEntity>)

    @Query("DELETE FROM auto_paths WHERE pitScoutId = :pitScoutId")
    suspend fun deleteAutoPathsByPitScoutId(pitScoutId: Long)
}

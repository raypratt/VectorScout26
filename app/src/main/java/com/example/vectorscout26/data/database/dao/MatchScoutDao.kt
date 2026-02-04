package com.example.vectorscout26.data.database.dao

import androidx.room.*
import com.example.vectorscout26.data.database.entities.ActionRecordEntity
import com.example.vectorscout26.data.database.entities.MatchScoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchScoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatchScout(matchScout: MatchScoutEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActionRecords(actionRecords: List<ActionRecordEntity>)

    @Query("SELECT * FROM match_scouts ORDER BY timestamp DESC")
    fun getAllMatchScouts(): Flow<List<MatchScoutEntity>>

    @Query("SELECT * FROM match_scouts WHERE id = :matchScoutId")
    suspend fun getMatchScoutById(matchScoutId: Long): MatchScoutEntity?

    @Query("SELECT * FROM action_records WHERE matchScoutId = :matchScoutId")
    suspend fun getActionRecordsByMatchId(matchScoutId: Long): List<ActionRecordEntity>

    @Update
    suspend fun updateMatchScout(matchScout: MatchScoutEntity)

    @Delete
    suspend fun deleteMatchScout(matchScout: MatchScoutEntity)

    @Query("DELETE FROM action_records WHERE matchScoutId = :matchScoutId")
    suspend fun deleteActionRecordsByMatchId(matchScoutId: Long)
}

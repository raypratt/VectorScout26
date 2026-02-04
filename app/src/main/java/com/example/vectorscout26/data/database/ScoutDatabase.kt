package com.example.vectorscout26.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.vectorscout26.data.database.dao.MatchScoutDao
import com.example.vectorscout26.data.database.entities.ActionRecordEntity
import com.example.vectorscout26.data.database.entities.MatchScoutEntity

@Database(
    entities = [MatchScoutEntity::class, ActionRecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ScoutDatabase : RoomDatabase() {
    abstract fun matchScoutDao(): MatchScoutDao

    companion object {
        @Volatile
        private var INSTANCE: ScoutDatabase? = null

        fun getDatabase(context: Context): ScoutDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScoutDatabase::class.java,
                    "scout_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

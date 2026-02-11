package com.example.vectorscout26.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.vectorscout26.data.database.dao.MatchScoutDao
import com.example.vectorscout26.data.database.dao.PitScoutDao
import com.example.vectorscout26.data.database.entities.ActionRecordEntity
import com.example.vectorscout26.data.database.entities.AutoPathEntity
import com.example.vectorscout26.data.database.entities.MatchScoutEntity
import com.example.vectorscout26.data.database.entities.PitScoutEntity

@Database(
    entities = [
        MatchScoutEntity::class,
        ActionRecordEntity::class,
        PitScoutEntity::class,
        AutoPathEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class ScoutDatabase : RoomDatabase() {
    abstract fun matchScoutDao(): MatchScoutDao
    abstract fun pitScoutDao(): PitScoutDao

    companion object {
        @Volatile
        private var INSTANCE: ScoutDatabase? = null

        fun getDatabase(context: Context): ScoutDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScoutDatabase::class.java,
                    "scout_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

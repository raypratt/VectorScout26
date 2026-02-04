package com.example.vectorscout26.utils

import com.example.vectorscout26.data.model.MatchScoutData
import com.google.gson.Gson
import com.google.gson.JsonObject

object JsonSerializer {
    private val gson = Gson()

    /**
     * Serialize match scout data to compact JSON for QR code
     */
    fun toCompactJson(matchScoutData: MatchScoutData): String {
        val json = JsonObject()

        // Version for future compatibility
        json.addProperty("v", 1)

        // Pre-match data (abbreviated keys)
        json.addProperty("e", matchScoutData.event)
        json.addProperty("m", matchScoutData.matchNumber)
        json.addProperty("rd", matchScoutData.robotDesignation)
        json.addProperty("sn", matchScoutData.scoutName)
        json.addProperty("t", matchScoutData.teamNumber)
        json.addProperty("sp", matchScoutData.startPosition)
        json.addProperty("l", matchScoutData.loaded)
        json.addProperty("ns", matchScoutData.noShow)

        // Action records (individual - one per action for location tracking)
        val actions = matchScoutData.actionRecords.map { record ->
            JsonObject().apply {
                addProperty("p", record.phase.name)
                addProperty("at", record.actionType.name)
                addProperty("d", record.durationMs)
                if (record.qualitativeData != null) {
                    addProperty("qd", record.qualitativeData.toJson())
                }
            }
        }

        json.add("a", gson.toJsonTree(actions))

        return gson.toJson(json)
    }

    /**
     * Deserialize match scout data from JSON
     */
    fun fromCompactJson(jsonString: String): MatchScoutData {
        // This would be used by the receiving end (Google Sheets script)
        // For now, just return a placeholder
        return MatchScoutData()
    }
}

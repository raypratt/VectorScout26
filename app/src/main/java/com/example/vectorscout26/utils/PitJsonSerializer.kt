package com.example.vectorscout26.utils

import com.example.vectorscout26.data.model.PitScoutData
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject

object PitJsonSerializer {
    private val gson = Gson()

    /**
     * Serialize pit scout data to compact JSON for QR code
     * Note: Photo path is excluded as photos are transferred manually
     */
    fun toCompactJson(pitScoutData: PitScoutData): String {
        val json = JsonObject()

        // Version and type identifier
        json.addProperty("v", 1)
        json.addProperty("type", "pit")

        // Event and Team info (abbreviated keys)
        json.addProperty("e", pitScoutData.event)
        json.addProperty("t", pitScoutData.teamNumber)
        json.addProperty("dt", pitScoutData.drivetrainType)
        json.addProperty("pr", pitScoutData.preferredRole)
        json.addProperty("pp", pitScoutData.preferredPath)

        // Auto paths
        val pathsArray = JsonArray()
        pitScoutData.autoPaths.forEach { path ->
            val pathObj = JsonObject()
            pathObj.addProperty("n", path.name)

            // Steps as array of "type:value" strings for compactness
            val stepsArray = JsonArray()
            path.steps.forEach { step ->
                stepsArray.add("${step.type.name[0]}:${step.value}")
            }
            pathObj.add("s", stepsArray)

            // Drawing path (just filename, not full path)
            if (path.drawingPath != null) {
                val fileName = path.drawingPath.substringAfterLast("/")
                pathObj.addProperty("d", fileName)
            }

            pathsArray.add(pathObj)
        }
        json.add("ap", pathsArray)

        return gson.toJson(json)
    }
}

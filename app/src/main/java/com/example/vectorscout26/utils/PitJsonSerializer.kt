package com.example.vectorscout26.utils

import com.example.vectorscout26.data.model.AutoPath
import com.example.vectorscout26.data.model.PitScoutData
import com.example.vectorscout26.data.model.StepType
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject

object PitJsonSerializer {
    private val gson = Gson()

    /**
     * Serialize pit scout data to compact JSON for QR code
     * Outputs two separate data structures for different tables:
     * - "pit": PitScout table data (team info)
     * - "paths": AutonPath table data (path text only)
     * Note: Photo path is excluded as photos are transferred manually
     */
    fun toCompactJson(pitScoutData: PitScoutData): String {
        val json = JsonObject()

        // Version and type identifier
        json.addProperty("v", 2)
        json.addProperty("type", "pit")

        // PitScout table data
        val pitObj = JsonObject()
        pitObj.addProperty("e", pitScoutData.event)
        pitObj.addProperty("t", pitScoutData.teamNumber)
        pitObj.addProperty("dt", pitScoutData.drivetrainType)
        pitObj.addProperty("pr", pitScoutData.preferredRole)
        pitObj.addProperty("pp", pitScoutData.preferredPath)
        json.add("pit", pitObj)

        // AutonPath table data - each path as text with team number
        val pathsArray = JsonArray()
        pitScoutData.autoPaths.forEach { path ->
            val pathObj = JsonObject()
            pathObj.addProperty("t", pitScoutData.teamNumber)
            pathObj.addProperty("n", path.name)
            pathObj.addProperty("p", formatPathAsText(path))
            pathsArray.add(pathObj)
        }
        json.add("paths", pathsArray)

        return gson.toJson(json)
    }

    /**
     * Format an auto path as readable text
     * Example: "3 > Load Depot > Score H > Load Neutral > Score R1"
     */
    private fun formatPathAsText(path: AutoPath): String {
        val parts = mutableListOf<String>()
        var currentAction: String? = null

        path.steps.forEach { step ->
            when (step.type) {
                StepType.START -> {
                    parts.add(step.value)
                }
                StepType.ACTION -> {
                    currentAction = step.value
                }
                StepType.LOCATION -> {
                    if (currentAction != null) {
                        parts.add("$currentAction ${step.value}")
                        currentAction = null
                    } else {
                        parts.add(step.value)
                    }
                }
            }
        }

        return parts.joinToString(" > ")
    }
}

package com.example.vectorscout26.utils

import com.example.vectorscout26.data.model.FoulTeamEntry
import com.google.gson.JsonArray
import com.google.gson.JsonObject

object FoulJsonSerializer {

    fun toCompactJson(
        event: String,
        matchNumber: Int,
        teams: List<FoulTeamEntry>
    ): String {
        val json = JsonObject()
        json.addProperty("v", 1)
        json.addProperty("type", "foul")
        json.addProperty("e", event)
        json.addProperty("m", matchNumber)

        val actionsArray = JsonArray()
        teams.forEach { team ->
            repeat(team.minorCount) {
                val action = JsonObject()
                action.addProperty("d", team.designation)
                action.addProperty("t", team.teamNumber)
                action.addProperty("pts", 5)
                actionsArray.add(action)
            }
            repeat(team.majorCount) {
                val action = JsonObject()
                action.addProperty("d", team.designation)
                action.addProperty("t", team.teamNumber)
                action.addProperty("pts", 15)
                actionsArray.add(action)
            }
        }
        json.add("actions", actionsArray)

        return json.toString()
    }
}

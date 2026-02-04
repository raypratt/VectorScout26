package com.example.vectorscout26.data.api

import com.example.vectorscout26.data.model.MatchScheduleEntry
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Client for The Blue Alliance API.
 */
object TbaApi {
    private const val BASE_URL = "https://www.thebluealliance.com/api/v3"
    private const val API_KEY = "M0Y9OuuNo4Gi9MY6whnflkXulr5Hwq8yCjLXeTfap4vdGFcHGDpVGIK2MDHhed6x"

    /**
     * Fetch qualification match schedule for an event.
     * Returns list of MatchScheduleEntry or null if request fails.
     */
    suspend fun fetchEventSchedule(eventKey: String): List<MatchScheduleEntry>? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/event/$eventKey/matches")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("X-TBA-Auth-Key", API_KEY)
                connection.setRequestProperty("Accept", "application/json")
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    parseMatchesResponse(response)
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Parse TBA matches response JSON into list of MatchScheduleEntry.
     * Only includes qualification matches (comp_level = "qm").
     */
    private fun parseMatchesResponse(json: String): List<MatchScheduleEntry> {
        val gson = Gson()
        val matches = gson.fromJson(json, JsonArray::class.java)

        return matches
            .map { it.asJsonObject }
            .filter { it.get("comp_level").asString == "qm" }  // Only qualification matches
            .mapNotNull { match ->
                try {
                    val matchNumber = match.get("match_number").asInt
                    val alliances = match.getAsJsonObject("alliances")

                    val redAlliance = alliances.getAsJsonObject("red")
                    val blueAlliance = alliances.getAsJsonObject("blue")

                    val redTeams = redAlliance.getAsJsonArray("team_keys")
                    val blueTeams = blueAlliance.getAsJsonArray("team_keys")

                    MatchScheduleEntry(
                        matchNumber = matchNumber,
                        red1 = parseTeamKey(redTeams[0].asString),
                        red2 = parseTeamKey(redTeams[1].asString),
                        red3 = parseTeamKey(redTeams[2].asString),
                        blue1 = parseTeamKey(blueTeams[0].asString),
                        blue2 = parseTeamKey(blueTeams[1].asString),
                        blue3 = parseTeamKey(blueTeams[2].asString)
                    )
                } catch (e: Exception) {
                    null
                }
            }
            .sortedBy { it.matchNumber }
    }

    /**
     * Parse team key (e.g., "frc5460") to team number (e.g., 5460).
     */
    private fun parseTeamKey(teamKey: String): Int {
        return teamKey.removePrefix("frc").toIntOrNull() ?: 0
    }
}

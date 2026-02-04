package com.example.vectorscout26.data.repository

import android.content.Context
import android.net.Uri
import com.example.vectorscout26.data.api.TbaApi
import com.example.vectorscout26.data.model.EventSchedule
import com.example.vectorscout26.data.model.MatchScheduleEntry
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Repository for managing match schedules.
 * Handles loading from TBA API, local cache, and file imports.
 */
object ScheduleRepository {
    private const val PREFS_NAME = "schedule_prefs"
    private const val KEY_MANUAL_ENTRY_MODE = "manual_entry_mode"
    private const val KEY_CURRENT_EVENT = "current_event"
    private const val SCHEDULE_DIR = "schedules"

    // In-memory cache of current schedule
    private var currentSchedule: EventSchedule? = null

    /**
     * Get the currently loaded schedule.
     */
    fun getCurrentSchedule(): EventSchedule? = currentSchedule

    /**
     * Check if a schedule is loaded.
     */
    fun hasSchedule(): Boolean = currentSchedule != null

    /**
     * Get team number for current schedule, match, and robot designation.
     */
    fun getTeamNumber(matchNumber: Int, robotDesignation: String): Int? {
        return currentSchedule?.getTeamNumber(matchNumber, robotDesignation)
    }

    /**
     * Get opposing alliance teams for a match.
     * Returns map of designation (e.g., "Blue1") to team number, or null if no schedule.
     */
    fun getOpposingTeams(matchNumber: Int, robotDesignation: String): Map<String, Int>? {
        val match = currentSchedule?.getMatch(matchNumber) ?: return null
        return match.getOpposingTeams(robotDesignation)
    }

    /**
     * Check if manual entry mode is enabled.
     */
    fun isManualEntryMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_MANUAL_ENTRY_MODE, false)
    }

    /**
     * Set manual entry mode.
     */
    fun setManualEntryMode(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_MANUAL_ENTRY_MODE, enabled).apply()
    }

    /**
     * Load schedule for an event. Tries in order:
     * 1. Local cache
     * 2. TBA API (if internet available)
     *
     * Returns LoadResult indicating success/failure and source.
     */
    suspend fun loadSchedule(context: Context, eventCode: String, eventName: String): LoadResult {
        // Try local cache first
        val cachedSchedule = loadFromCache(context, eventCode)
        if (cachedSchedule != null) {
            currentSchedule = EventSchedule(eventCode, eventName, cachedSchedule)
            saveCurrentEvent(context, eventCode)
            return LoadResult.Success(ScheduleSource.CACHE, currentSchedule!!.matches.size)
        }

        // Try TBA API
        val apiSchedule = TbaApi.fetchEventSchedule(eventCode)
        if (apiSchedule != null && apiSchedule.isNotEmpty()) {
            currentSchedule = EventSchedule(eventCode, eventName, apiSchedule)
            saveToCache(context, eventCode, apiSchedule)
            saveCurrentEvent(context, eventCode)
            return LoadResult.Success(ScheduleSource.API, apiSchedule.size)
        }

        // Failed to load
        return LoadResult.Failed
    }

    /**
     * Import schedule from a JSON file (TBA API format).
     */
    suspend fun importFromFile(context: Context, uri: Uri, eventCode: String, eventName: String): LoadResult {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val json = inputStream?.bufferedReader()?.use { it.readText() } ?: return@withContext LoadResult.Failed

                val matches = parseTbaMatchesJson(json)
                if (matches.isEmpty()) {
                    return@withContext LoadResult.Failed
                }

                currentSchedule = EventSchedule(eventCode, eventName, matches)
                saveToCache(context, eventCode, matches)
                saveCurrentEvent(context, eventCode)
                LoadResult.Success(ScheduleSource.FILE, matches.size)
            } catch (e: Exception) {
                e.printStackTrace()
                LoadResult.Failed
            }
        }
    }

    /**
     * Parse TBA matches JSON format.
     */
    private fun parseTbaMatchesJson(json: String): List<MatchScheduleEntry> {
        return try {
            val gson = Gson()
            val matches = gson.fromJson(json, JsonArray::class.java)

            matches
                .map { it.asJsonObject }
                .filter { it.get("comp_level").asString == "qm" }
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
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseTeamKey(teamKey: String): Int {
        return teamKey.removePrefix("frc").toIntOrNull() ?: 0
    }

    /**
     * Load schedule from local cache.
     */
    private fun loadFromCache(context: Context, eventCode: String): List<MatchScheduleEntry>? {
        return try {
            val file = getScheduleFile(context, eventCode)
            if (!file.exists()) return null

            val json = file.readText()
            val gson = Gson()
            val type = object : TypeToken<List<MatchScheduleEntry>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Save schedule to local cache.
     */
    private fun saveToCache(context: Context, eventCode: String, matches: List<MatchScheduleEntry>) {
        try {
            val dir = File(context.filesDir, SCHEDULE_DIR)
            if (!dir.exists()) dir.mkdirs()

            val file = getScheduleFile(context, eventCode)
            val gson = Gson()
            file.writeText(gson.toJson(matches))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getScheduleFile(context: Context, eventCode: String): File {
        val dir = File(context.filesDir, SCHEDULE_DIR)
        return File(dir, "${eventCode}_schedule.json")
    }

    /**
     * Save currently selected event.
     */
    private fun saveCurrentEvent(context: Context, eventCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CURRENT_EVENT, eventCode).apply()
    }

    /**
     * Get last selected event code.
     */
    fun getLastEventCode(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CURRENT_EVENT, null)
    }

    /**
     * Check if schedule is cached for an event.
     */
    fun isScheduleCached(context: Context, eventCode: String): Boolean {
        return getScheduleFile(context, eventCode).exists()
    }

    /**
     * Clear current schedule from memory.
     */
    fun clearCurrentSchedule() {
        currentSchedule = null
    }

    /**
     * Delete cached schedule for an event.
     */
    fun deleteCachedSchedule(context: Context, eventCode: String) {
        getScheduleFile(context, eventCode).delete()
        if (currentSchedule?.eventCode == eventCode) {
            currentSchedule = null
        }
    }
}

/**
 * Result of a schedule load operation.
 */
sealed class LoadResult {
    data class Success(val source: ScheduleSource, val matchCount: Int) : LoadResult()
    object Failed : LoadResult()
}

/**
 * Source of loaded schedule.
 */
enum class ScheduleSource {
    CACHE,
    API,
    FILE
}

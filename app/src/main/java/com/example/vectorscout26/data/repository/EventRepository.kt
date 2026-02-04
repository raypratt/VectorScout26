package com.example.vectorscout26.data.repository

import android.content.Context
import com.example.vectorscout26.data.model.Event
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object EventRepository {
    private var cachedEvents: List<Event>? = null

    fun loadEvents(context: Context): List<Event> {
        // Return cached events if already loaded
        cachedEvents?.let { return it }

        // Load from assets
        val json = context.assets.open("michigan_events_2026.json").bufferedReader().use { it.readText() }

        // Parse JSON using Gson
        val gson = Gson()
        val eventListType = object : TypeToken<List<Event>>() {}.type
        val events: List<Event> = gson.fromJson(json, eventListType)

        // Sort by date ascending, then by eventName
        cachedEvents = events.sortedWith(compareBy({ it.date }, { it.eventName }))

        return cachedEvents!!
    }

    fun clearCache() {
        cachedEvents = null
    }
}

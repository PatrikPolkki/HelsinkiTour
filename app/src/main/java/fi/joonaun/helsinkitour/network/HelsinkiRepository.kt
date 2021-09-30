package fi.joonaun.helsinkitour.network

import fi.joonaun.helsinkitour.utils.parseHtml
import java.util.*

object HelsinkiRepository {
    private val call = HelsinkiApi.service
    private lateinit var activities: Activities
    private lateinit var events: Events
    private lateinit var places: Places
    private val language: String

    init {
        language = if (Locale.getDefault().language == "fi") "fi" else "en"
    }

    suspend fun getAllPlaces(): Places {
        if (!this::places.isInitialized) {
            places = call.getAllPlaces(language)
            places.data.forEach {
                it.description.body = parseHtml(it.description.body)
            }
        }

        return places
    }

    suspend fun getAllEvents(): Events {
        if (!this::events.isInitialized) {
            events = call.getAllEvents(language)
            events.data.forEach {
                it.description.body = parseHtml(it.description.body)
            }
        }

        return events
    }

    suspend fun getAllActivities(): Activities {
        if (!this::activities.isInitialized) {
            activities = call.getAllActivities(language)
            activities.data.forEach {
                it.description.body = parseHtml(it.description.body)
            }
        }

        return activities
    }
}
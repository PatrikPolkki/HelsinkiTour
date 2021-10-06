package fi.joonaun.helsinkitour.network

import android.util.Log
import fi.joonaun.helsinkitour.utils.parseHtml
import java.util.*

object HelsinkiRepository {
    private val call = HelsinkiApi.service
    private var activities: Activities? = null
    private var events: Events? = null
    private var places: Places? = null
    private val language: String

    init {
        language = if (Locale.getDefault().language == "fi") "fi" else "en"
    }

    suspend fun getAllPlaces(): Places {
        if (places == null) {
            try {
                places = call.getAllPlaces(language)
                places?.data?.forEach {
                    it.description.body = parseHtml(it.description.body)
                }
            } catch (e: Exception) {
                Log.e("PLACES", e.toString())
            }
        }

        return places ?: Places(Meta(0, null), emptyList())
    }

    suspend fun getAllEvents(): Events {
        if (events == null) {
            try {
                events = call.getAllEvents(language)
                events?.data?.forEach {
                    it.description.body = parseHtml(it.description.body)
                }
            } catch (e: Exception) {
                Log.e("EVENTS", e.toString())
            }
        }

        return events ?: Events(Meta(0, null), emptyList())
    }

    suspend fun getAllActivities(): Activities {
        if (activities == null) {
            try {
                activities = call.getAllActivities(language)
                activities?.data?.forEach {
                    it.description.body = parseHtml(it.description.body)
                }
            } catch (e: Exception) {
                Log.e("ACTIVITIES", e.toString())
            }
        }

        return activities ?: Activities(Meta(0, null), emptyList())
    }
}
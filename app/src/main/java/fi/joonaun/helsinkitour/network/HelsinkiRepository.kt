package fi.joonaun.helsinkitour.network

class HelsinkiRepository {
    private val call = HelsinkiApi.service

    suspend fun getAllPlaces(language: String) = call.getAllPlaces(language)
    suspend fun getAllEvents(language: String) = call.getAllEvents(language)
    suspend fun getAllActivities(language: String) = call.getAllActivities(language)
}
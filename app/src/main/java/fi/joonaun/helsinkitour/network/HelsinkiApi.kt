package fi.joonaun.helsinkitour.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object HelsinkiApi {
    private const val BASE_URL = "https://open-api.myhelsinki.fi/v1/"

    interface Service {
        @GET("places/")
        suspend fun getAllPlaces(
            @Query("language_filter") language: String
        ): Places

        @GET("events/")
        suspend fun getAllEvents(
            @Query("language_filter") language: String
        ): Events

        @GET("activities/")
        suspend fun getAllActivities(
            @Query("language_filter") language: String
        ): Activities
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: Service = retrofit.create(Service::class.java)
}
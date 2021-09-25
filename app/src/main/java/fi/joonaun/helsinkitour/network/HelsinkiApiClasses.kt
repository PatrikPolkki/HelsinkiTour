package fi.joonaun.helsinkitour.network

import com.google.gson.annotations.SerializedName

//region Generic data classes

data class Meta(
    val count: Int,
    val next: String?
)

data class Name(
    val fi: String?,
    val en: String?
)

data class Address(
    @SerializedName("street_address") val streetAddress: String,
    @SerializedName("postal_code") val postalCode: String,
    val locality: String
)

data class Location(
    val lat: Double,
    val lon: Double,
    val address: Address
)

data class LicenseType(
    val id: Int,
    val name: String
)

data class Images(
    val url: String,
    @SerializedName("copyright_holder") val copyrightHolder: String,
    @SerializedName("license_type") val licenseType: LicenseType
)

data class Description(
    val intro: String?,
    val body: String,
    val images: List<Images>
)

data class Tags(
    val id: String,
    val name: String
)

//endregion

//region Events

data class Events(
    val meta: Meta,
    val data: List<Event>
)

data class Event(
    val id: String,
    val name: Name,
    @SerializedName("info_url") val infoUrl: String,
    @SerializedName("modified_at") val modifiedAt: String,
    val location: Location,
    val description: Description,
    val tags: List<Tags>,
    @SerializedName("event_dates") val eventDates: EventDates
)

data class EventDates(
    @SerializedName("starting_day") val startingDay: String,
    @SerializedName("ending_day") val endingDay: String,
    @SerializedName("additional_description") val additionalDescription: String
)

//endregion

//region Activities

data class Activities(
    val meta: Meta,
    val data: List<Activity>
)

data class Activity(
    val id: String,
    val name: Name,
    @SerializedName("info_url") val infoUrl: String,
    @SerializedName("modified_at") val modifiedAt: String,
    val location: Location,
    val description: Description,
    val tags: List<Tags>,
    @SerializedName("where_when_duration") val whereWhenDuration: WhereWhenDuration
)

data class WhereWhenDuration(
    @SerializedName("where_and_when") val whereAndWhen: String,
    val duration: String
)

//endregion

//region Places

data class Places(
    val meta: Meta,
    val data: List<Place>
)

data class Place(
    val id: String,
    val name: Name,
    @SerializedName("info_url") val infoUrl: String,
    @SerializedName("modified_at") val modifiedAt: String,
    val location: Location,
    val description: Description,
    val tags: List<Tags>,
    @SerializedName("opening_hours") val openingHours: OpeningHours
)

data class OpeningHours(
    val hours: List<Hours>,
    @SerializedName("openinghours_exception") val openingHoursException: String
)

data class Hours(
    @SerializedName("weekday_id") val weekdayId: Int,
    val opens: String?,
    val closes: String?,
    val open24h: Boolean
)

//endregion

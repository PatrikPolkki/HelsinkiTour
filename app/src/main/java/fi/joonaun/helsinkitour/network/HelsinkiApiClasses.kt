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

data class Image(
    val url: String,
    @SerializedName("copyright_holder") val copyrightHolder: String,
    @SerializedName("license_type") val licenseType: LicenseType
)

data class Description(
    val intro: String?,
    var body: String,
    val images: List<Image>?
)

data class Tags(
    val id: String,
    val name: String
)

data class SourceType(
    val id: Int,
    val name: String
)

//endregion

interface Helsinki {
    val id: String
    val name: Name
    val infoUrl: String
    val modifiedAt: String
    val location: Location
    val description: Description
    val tags: List<Tags>
    val sourceType: SourceType
}

//region Events

data class Events(
    val meta: Meta,
    val data: List<Event>
)

data class Event(
    override val id: String,
    override val name: Name,
    @SerializedName("info_url") override val infoUrl: String,
    @SerializedName("modified_at") override val modifiedAt: String,
    override val location: Location,
    override val description: Description,
    override val tags: List<Tags>,
    @SerializedName("source_type") override val sourceType: SourceType,
    @SerializedName("event_dates") val eventDates: EventDates
) : Helsinki

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
    override val id: String,
    override val name: Name,
    @SerializedName("info_url") override val infoUrl: String,
    @SerializedName("modified_at") override val modifiedAt: String,
    override val location: Location,
    override val description: Description,
    override val tags: List<Tags>,
    @SerializedName("source_type") override val sourceType: SourceType,
    @SerializedName("where_when_duration") val whereWhenDuration: WhereWhenDuration
) : Helsinki

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
    override val id: String,
    override val name: Name,
    @SerializedName("info_url") override val infoUrl: String,
    @SerializedName("modified_at") override val modifiedAt: String,
    override val location: Location,
    override val description: Description,
    override val tags: List<Tags>,
    @SerializedName("source_type") override val sourceType: SourceType,
    @SerializedName("opening_hours") val openingHours: OpeningHours
) : Helsinki

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

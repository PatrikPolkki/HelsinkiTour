package fi.joonaun.helsinkitour.utils

import android.content.Context
import android.text.util.Linkify
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.google.android.material.button.MaterialButton
import com.squareup.picasso.Picasso
import fi.joonaun.helsinkitour.R
import fi.joonaun.helsinkitour.network.*
import fi.joonaun.helsinkitour.ui.search.bottomsheet.ImageRecyclerViewAdapter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Hides [view] if [images] is null or empty.
 * If [view] is [RecyclerView] sets it adapter to [ImageRecyclerViewAdapter]
 * and add [images] to that.
 */
@BindingAdapter("recyclerViewImages")
fun bindShowImages(view: View, images: List<Image>?) {
    if (images == null || images.isEmpty()) {
        view.visibility = View.GONE
        return
    }

    if (view is RecyclerView) {
        view.adapter = ImageRecyclerViewAdapter()
        val adapter = (view.adapter as ImageRecyclerViewAdapter)
        adapter.addImages(images)
    }
}

/**
 * If [image] is used then load it to [view].
 * If [imageList] is used then loads first image from that list to [view].
 * Else sets placeholder image to [view].
 */
@BindingAdapter(value = ["image", "imageList"], requireAll = false)
fun bindShowImageOrPlaceholder(view: ImageView, image: String?, imageList: List<Image>?) {
    when {
        image != null -> loadSmallImage(view, image)
        imageList != null && imageList.isNotEmpty() -> {
            val img = imageList.first()
            loadSmallImage(view, img.url)
        }
        else -> view.setImageDrawable(
            ContextCompat.getDrawable(
                view.context,
                R.drawable.ic_baseline_image_not_supported_24
            )
        )
    }
}

/**
 * Load [url] to [view]
 */
@BindingAdapter("imageUrl")
fun bindShowImage(view: ImageView, url: String) {
    loadSmallImage(view, url)
}

/**
 * If [item] is [Event] then show event dates on [view]
 */
@BindingAdapter("eventDates")
fun bindEventDates(view: TextView, item: Helsinki?) {
    if (item is Event) {
        val startDate = stringToDate(item.eventDates.startingDay) ?: return
        val endDate = stringToDate(item.eventDates.endingDay)

        val text: String = when {
            endDate == null -> dateTimeToString(startDate)
            startDate.toLocalDate() == endDate.toLocalDate() -> view.context.getString(
                R.string.event_date_time,
                dateToString(startDate),
                startDate.toLocalTime().toString(),
                endDate.toLocalTime().toString()
            )
            else -> view.context.getString(
                R.string.event_dates,
                dateToString(startDate),
                dateToString(endDate)
            )
        }

        view.text = text
    }
}

/**
 * If [item] is [Activity] then show duration on [view]
 */
@BindingAdapter("whereAndWhen")
fun bindWhereAndWhen(view: TextView, item: Helsinki?) {
    if (item is Activity) {
        view.text = view.context.getString(
            R.string.duration_time,
            item.whereWhenDuration.duration.toString()
        )
    }
}

/**
 * If [item] is [Event] then [view] is visible, otherwise view is gone
 */
@BindingAdapter("showIfEvent")
fun bindShowIfEvent(view: View, item: Helsinki?) {
    view.visibility = if (item is Event) View.VISIBLE else View.GONE
}

/**
 * If [item] is [Activity] then [view] is visible, otherwise view is gone
 */
@BindingAdapter("showIfActivity")
fun bindShowIfActivity(view: View, item: Helsinki?) {
    view.visibility = if (item is Activity) View.VISIBLE else View.GONE
}

/**
 * If [item] is [Place] then [view] is visible, otherwise view is gone
 */
@BindingAdapter("showIfPlace")
fun bindShowIfPlace(view: View, item: Helsinki?) {
    view.visibility = if (item is Place) View.VISIBLE else View.GONE
}

/**
 * If [item] is [Activity] then [view] is visible, otherwise view is gone
 */
@BindingAdapter("showIfHasDuration")
fun bindShowIfHasDuration(view: View, item: Helsinki?) {
    if (item is Activity) {
        view.visibility = if (item.whereWhenDuration.duration == null) View.GONE else View.VISIBLE
    }
}

/**
 * Add clickable link to [view]
 */
@BindingAdapter("url")
fun bindUrl(view: TextView, url: String?) {
    view.apply {
        text = url
        autoLinkMask = Linkify.WEB_URLS
        linksClickable = true
    }
    Linkify.addLinks(view, Linkify.WEB_URLS)
}

/**
 * Sets [view] text to be [address]
 */
@BindingAdapter("address")
fun bindAddress(view: TextView, address: Address?) {
    address ?: return
    view.text = view.context.getString(
        R.string.address_street_city,
        address.streetAddress,
        address.postalCode,
        address.locality
    )
}

@BindingAdapter("marker_address")
fun bindMarkerAddress(view: TextView, address: Address?) {
    address ?: return
    view.text = view.context.getString(
        R.string.marker_address_street_city,
        address.streetAddress,
        address.postalCode,
        address.locality
    )
}

/**
 * If [item] is [Place] then shows opening hours
 */
@BindingAdapter("hours")
fun bindHours(view: TextView, item: Helsinki?) {
    if (item !is Place) return

    val context = view.context
    val hours = item.openingHours.hours
    var opens: String?
    var closes: String?
    var text = ""

    hours.forEach {
        opens = it.opens?.dropLast(3)
        closes = it.closes?.dropLast(3)

        text += "•    " + getWeekday(it.weekdayId, context) + ": "
        text += when {
            // Closed
            opens == null && closes == null && !it.open24h ->
                context.getString(R.string.closed)
            // Open 24/7
            it.open24h -> context.getString(R.string.open_24)
            else -> context.getString(R.string.opening_hours, opens, closes)
        }
        if (it.weekdayId != 7)
            text += "\n"

        opens = null
        closes = null
    }

    view.text = text
}

/**
 * If [favorite] is true then show checkMark, otherwise show star
 */
@BindingAdapter("favorite")
fun bindFavorite(view: MaterialButton, favorite: Boolean) {
    if (favorite) {
        view.setIconResource(R.drawable.ic_baseline_check_24)
    } else {
        view.setIconResource(R.drawable.ic_baseline_star_24)
    }
}

/**
 * Show [steps] on [view]
 */
@BindingAdapter("totalSteps")
fun bindTotalSteps(view: TextView, steps: Int?) {
    val text = view.context.getString(R.string.step_count, steps ?: 0)
    view.text = text
}

@BindingAdapter("imageSliderImages")
fun bindImageSliderImages(view: ImageSlider, images: List<Image>?) {
    val imageList: MutableList<SlideModel> = mutableListOf()

    images?.forEach {
        imageList.add(SlideModel(it.url, "", ScaleTypes.CENTER_CROP))
    }
    if (imageList.isEmpty())
        view.visibility = View.GONE
    else
        view.setImageList(imageList)
}

/**
 * @return weekdays name based on [id]
 */
private fun getWeekday(id: Int, context: Context): String {
    return when (id) {
        1 -> context.getString(R.string.monday)
        2 -> context.getString(R.string.tuesday)
        3 -> context.getString(R.string.wednesday)
        4 -> context.getString(R.string.thursday)
        5 -> context.getString(R.string.friday)
        6 -> context.getString(R.string.saturday)
        7 -> context.getString(R.string.sunday)
        else -> ""
    }
}

/**
 * Load [url] to [view] using [Picasso]
 */
private fun loadSmallImage(view: ImageView, url: String) {
    Picasso.get().load(url)
        .resize(300, 300)
        .centerCrop()
        .placeholder(R.drawable.ic_baseline_downloading_24)
        .error(R.drawable.ic_baseline_error_outline_24)
        .into(view)
}

/**
 * Converts [input] to [LocalDateTime]
 */
private fun stringToDate(input: String): LocalDateTime? {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    return try {
        LocalDateTime.parse(input, formatter)
    } catch (e: Exception) {
        null
    }
}

/**
 * Converts [input] to date [String]
 */
private fun dateToString(input: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("d.M.yyyy")
    return try {
        input.format(formatter)
    } catch (e: Exception) {
        "null"
    }
}

/**
 * Converts [input] to dateTime [String]
 */
private fun dateTimeToString(input: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("d.M.yyyy H:mm")
    return try {
        input.format(formatter)
    } catch (e: Exception) {
        "null"
    }
}
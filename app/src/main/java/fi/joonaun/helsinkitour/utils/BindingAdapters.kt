package fi.joonaun.helsinkitour.utils

import android.content.Context
import android.text.util.Linkify
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import fi.joonaun.helsinkitour.R
import fi.joonaun.helsinkitour.network.*
import fi.joonaun.helsinkitour.ui.search.bottomsheet.ImageRecyclerViewAdapter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

@BindingAdapter("imageIfExist")
fun bindShowImageOrPlaceholder(view: ImageView, images: List<Image>?) {
    if (images == null || images.isEmpty()) {
        view.setImageDrawable(
            ContextCompat.getDrawable(
                view.context,
                R.drawable.ic_baseline_image_not_supported_24
            )
        )
    } else {
        val img = images.first()
        loadSmallImage(view, img.url)
    }
}

@BindingAdapter("imageUrl")
fun bindShowImage(view: ImageView, url: String) {
    loadSmallImage(view, url)
}

@BindingAdapter("eventDates")
fun bindEventDates(view: TextView, item: Helsinki) {
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

@BindingAdapter("whereAndWhen")
fun bindWhereAndWhen(view: TextView, item: Helsinki) {
    if (item is Activity) {
        view.text = item.whereWhenDuration.duration
    }
}

@BindingAdapter("showIfEvent")
fun bindShowIfEvent(view: View, item: Helsinki) {
    view.visibility = if (item is Event) View.VISIBLE else View.GONE
}

@BindingAdapter("showIfActivity")
fun bindShowIfActivity(view: View, item: Helsinki) {
    view.visibility = if (item is Activity) View.VISIBLE else View.GONE
}

@BindingAdapter("showIfPlace")
fun bindShowIfPlace(view: View, item: Helsinki) {
    view.visibility = if (item is Place) View.VISIBLE else View.GONE
}

@BindingAdapter("showIfHasDuration")
fun bindShowIfHasDuration(view: View, item: Helsinki) {
    if (item is Activity) {
        view.visibility = if (item.whereWhenDuration.duration == null) View.GONE else View.VISIBLE
    }
}

@BindingAdapter("url")
fun bindUrl(view: TextView, url: String?) {
    view.apply {
        text = url
        autoLinkMask = Linkify.WEB_URLS
        linksClickable = true
    }
    Linkify.addLinks(view, Linkify.WEB_URLS)
}

@BindingAdapter("address")
fun bindAddress(view: TextView, address: Address) {
    view.text = view.context.getString(
        R.string.address_street_city,
        address.streetAddress,
        address.postalCode,
        address.locality
    )
}

@BindingAdapter("hours")
fun bindHours(view: TextView, item: Helsinki) {
    if (item !is Place) return

    val context = view.context
    val hours = item.openingHours.hours
    var opens: String?
    var closes: String?
    var text = ""

    hours.forEach {
        opens = it.opens?.dropLast(3)
        closes = it.closes?.dropLast(3)

        text += getWeekday(it.weekdayId, context) + ": "
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

private fun loadSmallImage(view: ImageView, url: String) {
    Picasso.get().load(url)
        .resize(300, 300)
        .centerCrop()
        .placeholder(R.drawable.ic_baseline_downloading_24)
        .error(R.drawable.ic_baseline_error_outline_24)
        .into(view)
}

private fun stringToDate(input: String): LocalDateTime? {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    return try {
        LocalDateTime.parse(input, formatter)
    } catch (e: Exception) {
        null
    }
}

private fun dateToString(input: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("d.M.yyyy")
    return try {
        input.format(formatter)
    } catch (e: Exception) {
        "null"
    }
}

private fun dateTimeToString(input: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("d.M.yyyy H:mm")
    return try {
        input.format(formatter)
    } catch (e: Exception) {
        "null"
    }
}
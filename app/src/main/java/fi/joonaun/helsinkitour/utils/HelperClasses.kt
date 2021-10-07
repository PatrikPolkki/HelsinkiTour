package fi.joonaun.helsinkitour.utils

import androidx.core.text.HtmlCompat
import fi.joonaun.helsinkitour.database.Favorite
import fi.joonaun.helsinkitour.network.Activity
import fi.joonaun.helsinkitour.network.Event
import fi.joonaun.helsinkitour.network.Helsinki
import fi.joonaun.helsinkitour.network.Place

fun parseHtml(text: String): String {
    var result = HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
    result = result.replace("\n", "\n\n")
    result.trim()
    return result
}

fun makeFavoriteItem(item: Helsinki): Favorite? {
    return Favorite(
        item.id,
        when (item) {
            is Event -> HelsinkiType.EVENT
            is Place -> HelsinkiType.PLACE
            is Activity -> HelsinkiType.ACTIVITY
            else -> return null
        },
        item.getLocaleName() ?: return null,
        item.description.images?.firstOrNull()?.url ?: return null,
        item.description.intro ?: item.description.body
    )
}
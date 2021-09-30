package fi.joonaun.helsinkitour.utils

import androidx.core.text.HtmlCompat

fun parseHtml(text: String): String {
    var result = HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
    result = result.replace("\n", "\n\n")
    result.trim()
    return result
}
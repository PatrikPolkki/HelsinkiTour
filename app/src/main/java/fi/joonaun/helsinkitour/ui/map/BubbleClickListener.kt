package fi.joonaun.helsinkitour.ui.map

import fi.joonaun.helsinkitour.network.Helsinki

interface BubbleClickListener {
    fun onBubbleClickListener(helsinkiItem: Helsinki, fav: Boolean)
}
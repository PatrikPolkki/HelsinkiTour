package fi.joonaun.helsinkitour.ui.map

import fi.joonaun.helsinkitour.network.Helsinki

interface MarkerClickListener {
    fun onMarkerClickListener(helsinkiItem: Helsinki, fav: Boolean)
}
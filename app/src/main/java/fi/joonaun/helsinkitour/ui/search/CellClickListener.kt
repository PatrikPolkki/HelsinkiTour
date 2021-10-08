package fi.joonaun.helsinkitour.ui.search

import android.location.Location

interface CellClickListener {
    fun onCellClickListener(id: String, typeId: Int)
}
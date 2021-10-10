package fi.joonaun.helsinkitour.ui.search

interface CellClickListener {
    /**
     * ClickListener for single recyclerView item
     */
    fun onCellClickListener(id: String, typeId: Int)
}
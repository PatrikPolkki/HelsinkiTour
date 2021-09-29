package fi.joonaun.helsinkitour.ui.map

import android.widget.Button
import fi.joonaun.helsinkitour.R
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.infowindow.InfoWindow

class MarkerWindow(mapView: MapView): InfoWindow(R.layout.info_window, mapView) {
    override fun onOpen(item: Any?) {
        // Following command
        closeAllInfoWindowsOn(mapView)

        // Here we are settings onclick listeners for the buttons in the layouts.

        val moveButton = mView.findViewById<Button>(R.id.move_button)
        val deleteButton = mView.findViewById<Button>(R.id.delete_button)

        moveButton.setOnClickListener {
            // How to create a moveMarkerMapListener is not covered here.
            // Use the Map Listeners guide for this instead
            // mapView.addMapListener(MoveMarkerMapListener)
        }
        deleteButton.setOnClickListener {
            // Do Something

            // In order to delete the marker,
            // You would probably have to pass the "map class"
            // where the map was created,
            // along with an ID to reference the marker.

            // Using a HashMap to store markers would be useful here
            // so that the markers can be referenced by ID.

            // Once you get the marker,
            // you would do map.overlays.remove(marker)
        }

        // You can set an onClickListener on the InfoWindow itself.
        // This is so that you can close the InfoWindow once it has been tapped.

        // Instead, you could also close the InfoWindows when the map is pressed.
        // This is covered in the Map Listeners guide.

        mView.setOnClickListener {
            close()
        }
    }

    override fun onClose() {
        TODO("Not yet implemented")
    }

}
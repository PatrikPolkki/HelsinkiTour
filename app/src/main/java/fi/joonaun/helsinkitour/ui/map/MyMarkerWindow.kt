package fi.joonaun.helsinkitour.ui.map

import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import fi.joonaun.helsinkitour.R
import fi.joonaun.helsinkitour.network.Helsinki
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.bonuspack.location.POI




class MyMarkerWindow(mapView: MapView):  InfoWindow(R.layout.info_window, mapView) {

    private val mapListener = MapEventsOverlay(object  : MapEventsReceiver {
        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
            close()
            return true
        }

        override fun longPressHelper(p: GeoPoint?): Boolean {
            Log.d("LONGPRESS", true.toString())
            return true
        }
    })

    override fun onOpen(item: Any?) {
        // Following command
        closeAllInfoWindowsOn(mapView)
        mMapView.overlays.add(mapListener)

        val markerInfo = item as Marker
        val selectedMarker = markerInfo.relatedObject as Helsinki


        val bubbleTitle = mView.findViewById<TextView>(R.id.bubble_title)
        val bubbleDes = mView.findViewById<TextView>(R.id.bubble_des)
        val bubbleAddress = mView.findViewById<TextView>(R.id.bubble_address)
        val bubbleUrl = mView.findViewById<TextView>(R.id.bubble_url)



        bubbleTitle.text = selectedMarker.name.fi
        bubbleDes.text = selectedMarker.description.intro ?: selectedMarker.description.body
        bubbleAddress.text = "${selectedMarker.location.address.streetAddress} ${selectedMarker.location.address.locality}, ${selectedMarker.location.address.postalCode}"
        bubbleUrl.text = selectedMarker.infoUrl
        // You can set an onClickListener on the InfoWindow itself.
        // This is so that you can close the InfoWindow once it has been tapped.

        // Instead, you could also close the InfoWindows when the map is pressed.
        // This is covered in the Map Listeners guide.

        mView.setOnClickListener {
            close()
        }
    }

    override fun onClose() {
        Log.d("ONCLOSE", "TOIMII")
        mMapView.overlays.remove(mapListener)
    }

}
package fi.joonaun.helsinkitour.ui.map

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.findNavController
import fi.joonaun.helsinkitour.BR
import fi.joonaun.helsinkitour.R
import fi.joonaun.helsinkitour.databinding.FragmentMapBinding
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


class MyMarkerWindow(mapView: MapView) : InfoWindow(R.layout.info_window, mapView) {

    private val mapListener = MapEventsOverlay(object : MapEventsReceiver {
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

        val viewRoot = LayoutInflater.from(mView.context).inflate(R.layout.info_window, null)
        val binding: ViewDataBinding? = DataBindingUtil.bind(viewRoot)

        mView = binding?.root

        binding?.setVariable(BR.helsinkiItem, selectedMarker)

        val bubbleReadMore = mView.findViewById<Button>(R.id.readMoreButton)

        bubbleReadMore.setOnClickListener {
           // view.findNavController().navigate()
        }

        mView.setOnClickListener {
            close()
        }
    }

    override fun onClose() {
        Log.d("ONCLOSE", "TOIMII")
        mMapView.overlays.remove(mapListener)
    }

}
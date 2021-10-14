package fi.joonaun.helsinkitour.ui.map

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.CheckBox
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import fi.joonaun.helsinkitour.BR
import fi.joonaun.helsinkitour.R
import fi.joonaun.helsinkitour.database.AppDatabase
import fi.joonaun.helsinkitour.network.Activity
import fi.joonaun.helsinkitour.network.Event
import fi.joonaun.helsinkitour.network.Place
import fi.joonaun.helsinkitour.utils.HelsinkiType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.infowindow.InfoWindow
import kotlin.math.roundToInt

class MyMarkerWindow(mapView: MapView, private val onMarkerClickListener: MarkerClickListener) :
    InfoWindow(R.layout.info_window, mapView), MapListener {
    private lateinit var roadOverlay: Polyline
    private lateinit var selectedMarker: RelatedObj

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

    private val mapLi = object : MapListener {
        override fun onScroll(event: ScrollEvent?): Boolean {
            Log.d("MapLi", "TOIMIIII")
            return true
        }

        override fun onZoom(event: ZoomEvent?): Boolean {
            Log.d("MapLi", "TOIMIIII")
            return true
        }
    }

    override fun onOpen(item: Any?) {
        closeAllInfoWindowsOn(mMapView)
        mMapView.overlays.add(mapListener)

        mMapView.addMapListener(mapLi)

        val markerInfo = item as Marker
        selectedMarker = markerInfo.relatedObject as RelatedObj

        val viewRoot = LayoutInflater.from(mView.context).inflate(R.layout.info_window, null)
        val binding: ViewDataBinding? = DataBindingUtil.bind(viewRoot)
        mView = binding?.root
        binding?.setVariable(BR.helsinkiItem, selectedMarker.helsinki)

        favourites()
        addPolyline()

        mMapView.setMapCenterOffset(0, (mMapView.height / 2.5).roundToInt())

        val bubbleReadMore = mView.findViewById<Button>(R.id.readMoreButton)
        bubbleReadMore.setOnClickListener {
            readMore()
        }
    }

    override fun onClose() {
        Log.d("ONCLOSE", "TOIMII")
        mMapView.overlays.remove(mapListener)
        if (this::roadOverlay.isInitialized)
            mMapView.overlays.remove(roadOverlay)
    }

    private fun readMore() {
        val type = when (selectedMarker.helsinki) {
            is Event -> HelsinkiType.EVENT
            is Activity -> HelsinkiType.ACTIVITY
            is Place -> HelsinkiType.PLACE
            else -> return
        }
        val action =
            MapFragmentDirections.actionNavMapToInfoFragment(type, selectedMarker.helsinki.id)
        mView.findNavController().navigate(action)
    }

    private fun addPolyline() {
        val roadManager: RoadManager = OSRMRoadManager(mView.context, "MY_USER_AGENT")
        val wayPoints: ArrayList<GeoPoint> = ArrayList()
        val startPoint = selectedMarker.location.value?.let {
            GeoPoint(it.latitude, it.longitude)
        }
        val endPoint =
            GeoPoint(selectedMarker.helsinki.location.lat, selectedMarker.helsinki.location.lon)
        wayPoints.apply {
            if (startPoint != null) {
                add(startPoint)
            }
            add(endPoint)
        }
        selectedMarker.owner.lifecycleScope.launch(Dispatchers.IO) {
            val road: Road = roadManager.getRoad(wayPoints)
            roadOverlay = RoadManager.buildRoadOverlay(road)
            roadOverlay.outlinePaint.strokeWidth = 10f
            roadOverlay.outlinePaint.color = Color.parseColor("#669df6")
            mMapView.overlays.add(roadOverlay)
            mMapView.invalidate()
        }
    }

    private fun favourites() {

        val db = AppDatabase.get(mView.context).favoriteDao()
        val favourite = db.get(selectedMarker.helsinki.id)

        val checkBox = mView.findViewById<CheckBox>(R.id.materialCheckBox)

        favourite.observe(selectedMarker.owner) { fav ->
            checkBox.apply {
                isChecked = fav != null
                setOnClickListener {
                    onMarkerClickListener.onMarkerClickListener(
                        selectedMarker.helsinki,
                        fav != null
                    )
                }
            }
        }
    }

    override fun onScroll(event: ScrollEvent?): Boolean {
        return true
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
        Log.d("onZoom", event.toString())
        return true
    }

}
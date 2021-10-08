package fi.joonaun.helsinkitour.ui.map

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.navigation.findNavController
import fi.joonaun.helsinkitour.BR
import fi.joonaun.helsinkitour.R
import fi.joonaun.helsinkitour.database.AppDatabase
import fi.joonaun.helsinkitour.databinding.FragmentMapBinding
import fi.joonaun.helsinkitour.network.Activity
import fi.joonaun.helsinkitour.network.Event
import fi.joonaun.helsinkitour.network.Helsinki
import fi.joonaun.helsinkitour.network.Place
import fi.joonaun.helsinkitour.ui.search.CellClickListener
import fi.joonaun.helsinkitour.utils.HelsinkiType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.supervisorScope
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.bonuspack.location.POI
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.views.overlay.Polyline
import kotlin.coroutines.coroutineContext


class MyMarkerWindow(mapView: MapView, private val onBubbleClickListener: BubbleClickListener) :
    InfoWindow(R.layout.info_window, mapView) {

    lateinit var roadOverlay: Polyline

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
        closeAllInfoWindowsOn(mMapView)
        mMapView.overlays.add(mapListener)

        val markerInfo = item as Marker
        val selectedMarker = markerInfo.relatedObject as RelatedObj

        val viewRoot = LayoutInflater.from(mView.context).inflate(R.layout.info_window, null)
        val binding: ViewDataBinding? = DataBindingUtil.bind(viewRoot)

        mView = binding?.root

        binding?.setVariable(BR.helsinkiItem, selectedMarker.helsinki)

        val db = AppDatabase.get(mView.context).favoriteDao()
        val favourite = db.get(selectedMarker.helsinki.id)

        val checkBox = mView.findViewById<CheckBox>(R.id.materialCheckBox)

        Log.d("lifecycleowner", favourite.value?.toString() ?: "null")

        favourite.observe(selectedMarker.owner) { fav ->
            checkBox.apply {
                isChecked = fav != null
                setOnClickListener {
                    onBubbleClickListener.onBubbleClickListener(selectedMarker.helsinki, fav != null)
                }
            }
        }

        val roadManager: RoadManager = OSRMRoadManager(mView.context, "MY_USER_AGENT")
        val wayPoints: ArrayList<GeoPoint> = ArrayList()
        val startPoint = GeoPoint(
            selectedMarker.location.value!!.latitude,
            selectedMarker.location.value!!.longitude
        )
        val endPoint =
            GeoPoint(selectedMarker.helsinki.location.lat, selectedMarker.helsinki.location.lon)
        wayPoints.apply {
            add(startPoint)
            add(endPoint)
        }
        val road: Road = roadManager.getRoad(wayPoints)
        roadOverlay = RoadManager.buildRoadOverlay(road)
        mMapView.overlays.add(roadOverlay)
        mMapView.invalidate()

        val bubbleReadMore = mView.findViewById<Button>(R.id.readMoreButton)

        bubbleReadMore.setOnClickListener {
            val type = when (selectedMarker.helsinki) {
                is Event -> HelsinkiType.EVENT
                is Activity -> HelsinkiType.ACTIVITY
                is Place -> HelsinkiType.PLACE
                else -> return@setOnClickListener
            }

            val action =
                MapFragmentDirections.actionNavMapToInfoActivity(selectedMarker.helsinki.id, type)
            mView.findNavController().navigate(action)
        }

        mView.setOnClickListener {
            close()
        }
    }

    override fun onClose() {
        Log.d("ONCLOSE", "TOIMII")
        mMapView.overlays.remove(mapListener)
        mMapView.overlays.remove(roadOverlay)
    }

}
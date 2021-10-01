package fi.joonaun.helsinkitour.ui.map

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fi.joonaun.helsinkitour.MainViewModel
import fi.joonaun.helsinkitour.R
import fi.joonaun.helsinkitour.databinding.FragmentMapBinding
import fi.joonaun.helsinkitour.network.Helsinki
import fi.joonaun.helsinkitour.ui.map.filtersheet.FilterSheet
import fi.joonaun.helsinkitour.ui.search.SearchRecyclerViewAdapter
import fi.joonaun.helsinkitour.ui.search.bottomsheet.InfoBottomSheet
import org.osmdroid.api.IGeoPoint
import org.osmdroid.bonuspack.location.POI
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme
import java.lang.Exception

class MapFragment : Fragment(R.layout.fragment_map), LocationListener, ChipGroup.OnCheckedChangeListener {
    private lateinit var binding: FragmentMapBinding
    lateinit var locationManager: LocationManager
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var marker: Marker

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Configuration.getInstance().load(
            context,
            PreferenceManager.getDefaultSharedPreferences(context)
        )
        binding = FragmentMapBinding.inflate(layoutInflater)

        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0
        )
        val policy: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        binding.MapChipGroup.setOnCheckedChangeListener(this)

        setMap()
        initFirstObserver()
        requestLocation()

        binding.filterButton.setOnClickListener(fabListener)

        return binding.root
    }

    private fun requestLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1f, this)
        }
    }

    override fun onLocationChanged(p0: Location) {
        // Log.d("GEOLOCATION", "new latitude: ${p0.latitude} and longitude : ${p0.longitude}")
    }

    private fun setMap() {
        binding.map.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(14.0)
            controller.setCenter(GeoPoint(60.17, 24.95))
        }
    }

    private fun initFirstObserver() {
        mainViewModel.activities.observe(viewLifecycleOwner, helsinkiObserver)
    }

    private fun addMarker(pointInfo: List<Helsinki>) {

        pointInfo.forEach { point ->
            try {
                marker = Marker(binding.map)
                marker.icon = AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_baseline_place_24
                )
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.position = GeoPoint(point.location.lat, point.location.lon)


                val indoWindow = MarkerWindow(binding.map)
                marker.infoWindow = indoWindow
                marker.title = point.name.fi
                marker.closeInfoWindow()
                binding.map.apply {
                    overlays.add(marker)
                    //displays the marker as soon as it has been added.
                    invalidate()
                }
            } catch (e: Exception) {
                Log.e("ERROR", "location: ${point.location} error: $e")
            }

        }
    }

    private val helsinkiObserver = Observer<List<Helsinki>> {
        Log.d("Observer", "${it.size}")
        addMarker(it)
    }

    override fun onCheckedChanged(group: ChipGroup?, checkedId: Int) {
        mainViewModel.activities.removeObserver(helsinkiObserver)
        mainViewModel.events.removeObserver(helsinkiObserver)
        mainViewModel.places.removeObserver(helsinkiObserver)

        binding.map.overlays.clear()
        when(checkedId) {
            R.id.chip1 -> {
                    Log.d("CHECKED", binding.chip1.isChecked.toString())
                    mainViewModel.activities.observe(viewLifecycleOwner, helsinkiObserver)
            }
            R.id.chip2 -> {
                    Log.d("CHECKED", binding.chip2.isChecked.toString())
                    mainViewModel.events.observe(viewLifecycleOwner, helsinkiObserver)
            }
            R.id.chip3 -> {
                    Log.d("CHECKED", binding.chip3.isChecked.toString())
                    mainViewModel.places.observe(viewLifecycleOwner, helsinkiObserver)

            }
        }
    }

    private val fabListener = View.OnClickListener {
        when(it) {
            binding.filterButton -> {
                val filterSheet = FilterSheet()

                filterSheet.show(parentFragmentManager, filterSheet.tag)
            }
        }
    }


}
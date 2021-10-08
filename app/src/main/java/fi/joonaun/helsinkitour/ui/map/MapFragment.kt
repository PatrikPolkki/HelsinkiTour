package fi.joonaun.helsinkitour.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.material.chip.ChipGroup
import fi.joonaun.helsinkitour.MainViewModel
import fi.joonaun.helsinkitour.R
import fi.joonaun.helsinkitour.database.Favorite
import fi.joonaun.helsinkitour.databinding.FragmentMapBinding
import fi.joonaun.helsinkitour.network.Helsinki
import fi.joonaun.helsinkitour.network.HelsinkiRepository
import fi.joonaun.helsinkitour.ui.map.filtersheet.FilterSheet
import fi.joonaun.helsinkitour.ui.stats.StatsViewModel
import fi.joonaun.helsinkitour.ui.stats.StatsViewModelFactory
import fi.joonaun.helsinkitour.utils.getTodayDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.lang.Exception
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.events.ZoomEvent

import org.osmdroid.events.ScrollEvent

import org.osmdroid.events.MapListener
import org.osmdroid.views.overlay.Polyline
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


data class RelatedObj(
    val helsinki: Helsinki,
    val location: LiveData<Location>,
    val owner: LifecycleOwner
)

class MapFragment : Fragment(R.layout.fragment_map), LocationListener,
    ChipGroup.OnCheckedChangeListener, BubbleClickListener {
    private lateinit var binding: FragmentMapBinding
    private lateinit var locationManager: LocationManager
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var userMarker: Marker
    private var locDistance: Location? = null

    private val viewModel: MapViewModel by viewModels {
        MapViewModelFactory(requireContext())
    }

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

        val policy: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager


        locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        binding.MapChipGroup.setOnCheckedChangeListener(this)

        userMarker = Marker(binding.map)
        userMarker.apply {
            icon = AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.ic_baseline_person_pin_circle_24
            )?.also {
                it.setTint(Color.DKGRAY)
            }
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        binding.map.apply {
            overlays.add(userMarker)
            invalidate()
        }

        getPref()
        setMap()
        initFirstObserver()
        requestLocation()

        binding.filterButton.setOnClickListener(fabListener)
        binding.fabLocation.setOnClickListener(fabListener)

        viewModel.userLocation.observe(viewLifecycleOwner, userMarkerObserver)
        viewModel.userLocation.observe(viewLifecycleOwner, distanceObserver)

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        savePref()
    }

    private fun requestLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1f, this)
        }
    }

    override fun onLocationChanged(p0: Location) {
        Log.d("LOCATION", "${p0.latitude}, ${p0.longitude}")
        viewModel.setUserLocation(p0)
    }

    private val distanceObserver = Observer<Location> { vmLocation ->
        locDistance?.let {
            val distance = vmLocation.distanceTo(it)
            Log.d("DISTANCE", distance.toString())
            viewModel.insertDistance(distance.roundToInt(), getTodayDate())
        }
        locDistance = vmLocation
    }

    private val userMarkerObserver = Observer<Location> {
        if (it != null) {
            userMarker.position = GeoPoint(it.latitude, it.longitude)
        } else {
            userMarker.position = GeoPoint(60.17, 24.95)
        }
    }

    private fun savePref() {
        val preferences =
            requireActivity().getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE)
        val editor = preferences.edit()

        viewModel.getUserLocation().value?.let {
            editor.putString("LOCATION_LAT", it.latitude.toString())
            editor.putString("LOCATION_LON", it.longitude.toString())
            editor.putString("LOCATION_PROVIDER", it.provider)
        }
        editor.apply()
    }

    private fun getPref() {
        val preferences =
            requireActivity().getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE)
        val lat: String? = preferences.getString("LOCATION_LAT", null)
        val lon: String? = preferences.getString("LOCATION_LON", null)
        var location: Location? = null
        if (lat != null && lon != null) {
            val provider: String? = preferences.getString("LOCATION_PROVIDER", null)
            location = Location(provider)
            location.latitude = lat.toDouble()
            location.longitude = lon.toDouble()
        }
        if (location != null) {
            viewModel.initUserLocation(location)
        }
    }

    private fun setMap() {
        val userLoc = viewModel.getUserLocation()
        binding.map.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(14.0)
            if (userLoc.value != null) {
                controller.setCenter(GeoPoint(userLoc.value!!.latitude, userLoc.value!!.longitude))
                userMarker.position = GeoPoint(userLoc.value!!.latitude, userLoc.value!!.longitude)
            } else {
                controller.setCenter(GeoPoint(60.17, 24.95))
                userMarker.position = GeoPoint(60.17, 24.95)
            }

            addMapListener(object : MapListener {
                override fun onScroll(event: ScrollEvent): Boolean {
                    return true
                }

                override fun onZoom(event: ZoomEvent): Boolean {
                    //do something
                    return false
                }
            })
        }
    }

    private fun initFirstObserver() {
        viewModel.helsinkiList.observe(viewLifecycleOwner, helsinkiObserver)
        lifecycleScope.launch {
            viewModel.setHelsinkiList(HelsinkiRepository.getAllActivities().data)
        }
    }


    private fun addMarker(pointInfo: List<Helsinki>) {
        lifecycleScope.launch(Dispatchers.IO) {
            val allMarkers = RadiusMarkerClusterer(requireContext())
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_star_24)
            drawable?.setTint(Color.BLACK)
            allMarkers.setIcon(drawable?.toBitmap(200, 200))
            val myInfoWindow = MyMarkerWindow(binding.map, this@MapFragment)
            val userLocation: LiveData<Location> = viewModel.getUserLocation()
            pointInfo.forEach { point ->
                try {
                    val marker = Marker(binding.map)
                    marker.apply {

                        icon = AppCompatResources.getDrawable(
                            requireContext(),
                            R.drawable.ic_baseline_place_24
                        )?.also {
                            it.setTint(Color.DKGRAY)
                        }
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        position = GeoPoint(point.location.lat, point.location.lon)
                        infoWindow = myInfoWindow
                        relatedObject = RelatedObj(point, userLocation, viewLifecycleOwner)
                        closeInfoWindow()
                    }
                    allMarkers.add(marker)
                } catch (e: Exception) {
                    Log.e("ERROR", "location: ${point.location} error: $e")
                }
            }
            binding.map.apply {
                overlays.add(allMarkers)
                // displays the marker as soon as it has been added.
                invalidate()
            }

        }
    }

    private val helsinkiObserver = Observer<List<Helsinki>> {
        Log.d("Observer", "${it.size}")
        binding.map.overlays.clear()
        addMarker(it)
    }


    override fun onCheckedChanged(group: ChipGroup?, checkedId: Int) {
        when (checkedId) {
            R.id.chip1 -> {
                Log.d("CHECKED", binding.chip1.isChecked.toString())
                if (binding.chip1.isChecked)
                    lifecycleScope.launch {
                        viewModel.setHelsinkiList(HelsinkiRepository.getAllActivities().data)
                    }
            }
            R.id.chip2 -> {
                Log.d("CHECKED", binding.chip2.isChecked.toString())
                if (binding.chip2.isChecked)
                lifecycleScope.launch {
                    viewModel.setHelsinkiList(HelsinkiRepository.getAllEvents().data)
                }
            }
            R.id.chip3 -> {
                Log.d("CHECKED", binding.chip3.isChecked.toString())
                if (binding.chip3.isChecked)
                lifecycleScope.launch {
                    viewModel.setHelsinkiList(HelsinkiRepository.getAllPlaces().data)
                }
            }
        }
    }

    private fun getUserLoc() {
        val userLoc: LiveData<Location> = viewModel.getUserLocation()
        binding.map.controller.setCenter(
            GeoPoint(
                userLoc.value!!.latitude,
                userLoc.value!!.longitude
            )
        )
    }

    private val fabListener = View.OnClickListener {
        when (it) {
            binding.filterButton -> {
                val filterSheet = FilterSheet()

                filterSheet.show(parentFragmentManager, filterSheet.tag)
            }
            binding.fabLocation -> {
                Log.d("FAB", "WORKS")
                getUserLoc()
            }
        }
    }

    override fun onBubbleClickListener(helsinkiItem: Helsinki, fav: Boolean) {
        Log.d("favButton", "TOIMI")
        if (fav) {
            viewModel.deleteFavorite(helsinkiItem)
        } else {
            viewModel.addFavourite(helsinkiItem)
        }
    }
}
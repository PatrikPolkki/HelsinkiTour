package fi.joonaun.helsinkitour.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.GnssStatus
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
import fi.joonaun.helsinkitour.databinding.FragmentMapBinding
import fi.joonaun.helsinkitour.network.Helsinki
import fi.joonaun.helsinkitour.network.HelsinkiRepository
import fi.joonaun.helsinkitour.utils.getTodayDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import java.util.*
import kotlin.math.roundToInt


data class RelatedObj(
    val helsinki: Helsinki,
    val location: LiveData<Location?>,
    val owner: LifecycleOwner
)

data class MapLocation(
    val latitude: Double,
    val longitude: Double,
    val zoomLevel: Double
)

class MapFragment : Fragment(R.layout.fragment_map), LocationListener,
    ChipGroup.OnCheckedChangeListener, MarkerClickListener {
    companion object {
        private var addMarkerJob = Job()
    }
    private lateinit var binding: FragmentMapBinding
    private lateinit var locationManager: LocationManager
    private lateinit var userMarker: Marker

    private var locDistance: Location? = null

    private val viewModel: MapViewModel by viewModels {
        MapViewModelFactory(requireContext())
    }

    private val mainViewModel: MainViewModel by activityViewModels()

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

        locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

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

        addGpsListener()

        binding.MapChipGroup.setOnCheckedChangeListener(this)
        binding.fabLocation.setOnClickListener(fabListener)

        viewModel.userLocation.observe(viewLifecycleOwner, userMarkerObserver)
        viewModel.userLocation.observe(viewLifecycleOwner, distanceObserver)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showSearchItems()
    }

    override fun onDestroy() {
        super.onDestroy()
        savePref()
        mainViewModel.mapLocation = MapLocation(
            binding.map.mapCenter.latitude,
            binding.map.mapCenter.longitude,
            binding.map.zoomLevelDouble
        )
    }

    private val gps = object : GnssStatus.Callback() {
        override fun onStarted() {
            super.onStarted()
            requestLocation()
        }

        override fun onStopped() {
            super.onStopped()
            locationManager.removeUpdates(this@MapFragment)
        }
    }

    private fun addGpsListener() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.registerGnssStatusCallback(gps, null)
        }

    }

    private fun requestLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        ) {
            val location: Unit =
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10f, this)
            Log.d("UNIT", location.toString())
        }
    }

    override fun onLocationChanged(p0: Location) {
        Log.d("LOCATION", "${p0.latitude}, ${p0.longitude}")
        viewModel.setUserLocation(p0)
    }

    private val distanceObserver = Observer<Location?> { vmLocation ->
        locDistance?.let {
            val distance = vmLocation.distanceTo(it)
            // Log.d("DISTANCE", distance.toString())
            viewModel.insertDistance(distance.roundToInt(), getTodayDate())
        }
        locDistance = vmLocation
    }

    private val userMarkerObserver = Observer<Location?> {
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

        viewModel.userLocation.value?.let {
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
        val userLocation: LiveData<Location?> = viewModel.userLocation

        binding.map.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            maxZoomLevel = 20.0
            minZoomLevel = 5.0
            if (mainViewModel.mapLocation == null) {
                controller.setZoom(18.0)
                controller.setCenter(
                    GeoPoint(
                        userLocation.value?.latitude ?: 60.17,
                        userLocation.value?.longitude ?: 24.95
                    )
                )
            } else {
                controller.setZoom(mainViewModel.mapLocation?.zoomLevel ?: 10.0)
                controller.setCenter(
                    GeoPoint(
                        mainViewModel.mapLocation?.latitude ?: 60.17,
                        mainViewModel.mapLocation?.longitude ?: 24.95
                    )
                )
            }

            userMarker.position = GeoPoint(
                userLocation.value?.latitude ?: 60.17,
                userLocation.value?.longitude ?: 24.95
            )
        }
    }

    private fun initFirstObserver() {
        viewModel.helsinkiList.observe(viewLifecycleOwner, helsinkiObserver)
        lifecycleScope.launch {
            viewModel.setHelsinkiList(HelsinkiRepository.getAllActivities().data)
        }
    }


    private fun addMarker(pointInfo: List<Helsinki>) {
        addMarkerJob.cancel()
        addMarkerJob = Job()
        lifecycleScope.launch(Dispatchers.Default + addMarkerJob) {
            binding.map.apply {
                overlays.add(userMarker)
                invalidate()
            }
            val allMarkers = RadiusMarkerClusterer(requireContext())
            val drawable =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_star_24)
            drawable?.setTint(Color.BLACK)
            allMarkers.setIcon(drawable?.toBitmap(200, 200))

            val myInfoWindow = MyMarkerWindow(binding.map, this@MapFragment)
            val userLocation: LiveData<Location?> = viewModel.userLocation

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
        val userLocation: LiveData<Location?> = viewModel.userLocation
        if (userLocation.value != null) {
            binding.map.controller.setCenter(
                GeoPoint(
                    userLocation.value?.latitude ?: 60.17,
                    userLocation.value?.longitude ?: 24.95
                )
            )
        }

    }

    private val fabListener = View.OnClickListener {
        when (it) {
            binding.fabLocation -> {
                Log.d("FAB", "WORKS")
                getUserLoc()
            }
        }
    }

    override fun onMarkerClickListener(helsinkiItem: Helsinki, fav: Boolean) {
        Log.d("favButton", "TOIMI")
        if (fav) {
            viewModel.deleteFavorite(helsinkiItem)
        } else {
            viewModel.addFavourite(helsinkiItem)
        }
    }

    /**
     * Checks if there is argument called "helsinkiList". Then casts it to [List<Helsinki>]
     * and set it to be value of "viewModel.helsinkiList"
     */
    private fun showSearchItems() {
        val argList = arguments?.get("helsinkiList") as? List<*>
        val list = argList?.filterIsInstance<Helsinki>()
        if (list == null || list.isEmpty()) {
            Log.d("MAP ARGS", "No arguments or is empty")
            return
        }

        viewModel.setHelsinkiList(list)
        binding.MapChipGroup.clearCheck()

        val argBounds = arguments?.get("bounds") as? BoundingBox ?: return
        argBounds.apply {
            binding.map.controller.zoomToSpan(latNorth - latSouth, lonWest - lonEast)
            binding.map.controller.setCenter(GeoPoint(
                (latNorth + latSouth) / 2.0,
                (lonWest + lonEast) / 2.0
            ))
        }
    }
}
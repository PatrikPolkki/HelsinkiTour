package fi.joonaun.helsinkitour

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import fi.joonaun.helsinkitour.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), SensorEventListener {

    private var sm: SensorManager? = null
    private var sStepCounter: Sensor? = null

    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initNavigation()

        mainViewModel.apply {
            getAll()
            addStatsIfNotExist()
        }

        askAllPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        sm?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        if (event.sensor == sStepCounter) {
            Log.d("SENSOR", "Steps: ${event.values[0]}")
            if (mainViewModel.stepsBegin == null) mainViewModel.stepsBegin = event.values[0].toInt()

            mainViewModel.stepsBegin?.let {
                mainViewModel.updateSteps(event.values[0].toInt() - it)
            }
            mainViewModel.stepsBegin = event.values[0].toInt()
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        Log.d("SENSOR", "$p0 accuracy changed to $p1")
    }

    /**
     * Asks permissions if they are not already given
     */
    private fun askAllPermissions() {
        val permissionArray = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION
        ) else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
                map.forEach {
                    when (it.key) {
                        Manifest.permission.ACTIVITY_RECOGNITION -> if (it.value) initSensor()
                    }
                }
            }

        when {
            permissionArray.all {
                ContextCompat.checkSelfPermission(
                    this,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            } -> initSensor()
            else -> requestPermissionLauncher.launch(permissionArray)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) initSensor()
    }

    /**
     * Registers stepCounter sensor
     */
    private fun registerSensor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        sStepCounter?.let {
            Log.d("SENSOR", "Registering ${it.name}")
            sm?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    /**
     * Initializes bottom navigation and appBar
     */
    private fun initNavigation() {
        val navHost =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHost.navController
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.navMap, R.id.navSearch, R.id.navStats
//            )
//        )
//
//        setupActionBarWithNavController(navController, appBarConfiguration)

        val navView = binding.bottomNavigationView
        navView.setupWithNavController(navController)
    }

    /**
     * Initializes stepCounter sensor
     */
    private fun initSensor() {
        sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sStepCounter = sm?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        registerSensor()
    }
}
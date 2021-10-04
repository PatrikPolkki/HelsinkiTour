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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import fi.joonaun.helsinkitour.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sm: SensorManager
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
        initSensor()

        mainViewModel.apply {
            getAll()
            addStatsIfNotExist()
        }
    }

    override fun onStart() {
        super.onStart()
        requestActivityRecognitionPermission()
    }

    override fun onStop() {
        super.onStop()
        sm.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        if(event.sensor == sStepCounter) {
            if(mainViewModel.stepsBegin == null) mainViewModel.stepsBegin = event.values[0].toInt()

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
     * Requesting permission [Manifest.permission.ACTIVITY_RECOGNITION].
     * This permission is needed on Android 10 and newer.
     * Doesn't ask, if older version.
     */
    private fun requestActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("PERMISSION", "Asking permission")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    requestPermissions(
                        arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                        0
                    )
                }
            } else {
                Log.d("PERMISSION", "Already have permission")
                registerSensor()
            }
        } else {
            registerSensor()
        }
    }

    private fun registerSensor() {
        sStepCounter?.let {
            Log.d("SENSOR", "Registering ${it.name}")
            sm.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun initNavigation() {
        val navHost =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHost.navController
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navMap, R.id.navSearch, R.id.navStats
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        val navView = binding.bottomNavigationView
        navView.setupWithNavController(navController)
    }

    private fun initSensor() {
        sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sStepCounter = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }
}
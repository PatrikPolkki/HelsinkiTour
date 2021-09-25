package fi.joonaun.helsinkitour

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import fi.joonaun.helsinkitour.databinding.ActivityMainBinding
import fi.joonaun.helsinkitour.network.Activities
import fi.joonaun.helsinkitour.network.Events
import fi.joonaun.helsinkitour.network.Places

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initNavigation()

        mainViewModel.getAll()
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
}
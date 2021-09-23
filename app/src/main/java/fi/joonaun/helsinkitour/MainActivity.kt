package fi.joonaun.helsinkitour

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import fi.joonaun.helsinkitour.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initNavigation()
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
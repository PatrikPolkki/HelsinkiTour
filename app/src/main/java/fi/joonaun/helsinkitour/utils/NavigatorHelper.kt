package fi.joonaun.helsinkitour.utils

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import fi.joonaun.helsinkitour.R
import fi.joonaun.helsinkitour.network.Helsinki
import org.osmdroid.util.BoundingBox

class NavigatorHelper(private val context: Context, private val navController: NavController) {
    private fun showOnMap(item: Helsinki) {
        val north = item.location.lat
        val south = item.location.lat
        val west = item.location.lon
        val east = item.location.lon

        val list = listOf(item)
        val bounds = try {
            BoundingBox(north, east, south, west)
        } catch (e: Exception) {
            Log.e("BOUNDING BOX", "Error: ${e.localizedMessage}")
            BoundingBox(60.17, 24.95, 60.17, 24.95)
        }
        val bundle = bundleOf("helsinkiList" to list, "bounds" to bounds)
        navController.navigate(R.id.navMap, bundle)
    }

    private fun navigate(item: Helsinki) {
        try {
            val uri = Uri.parse("https://www.google.com/maps/dir/?api=1" +
                    "&destination=${item.location.lat},${item.location.lon}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("NAVIGATE", "Couldn't open Google Maps")
        }
    }

    fun showMapDialog(item: Helsinki) {
        AlertDialog.Builder(context).apply {
            setTitle(R.string.what_map)
            setPositiveButton(R.string.navigate) { _, _ ->
                navigate(item)
            }
            setNegativeButton(R.string.showOnMap) { _, _ ->
                showOnMap(item)
            }
            create()
            show()
        }
    }
}
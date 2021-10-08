package fi.joonaun.helsinkitour.ui.map

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.*
import fi.joonaun.helsinkitour.database.AppDatabase
import fi.joonaun.helsinkitour.database.Stat
import fi.joonaun.helsinkitour.network.Helsinki
import fi.joonaun.helsinkitour.ui.stats.StatsViewModel
import fi.joonaun.helsinkitour.utils.addFavouriteToDatabase
import fi.joonaun.helsinkitour.utils.deleteFavoriteFromDatabase
import fi.joonaun.helsinkitour.utils.makeFavoriteItem
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class MapViewModel(context: Context): ViewModel() {
    private val database = AppDatabase.get(context)
    // val favorite = database.favoriteDao().get(id)

    val userLocation: MutableLiveData<Location> by lazy {
        MutableLiveData<Location>().also {
            it.value = null
        }
    }

    fun getUserLocation(): LiveData<Location> {
        return userLocation
    }

    fun initUserLocation(prefLocation: Location) {
        userLocation.value = prefLocation
    }

    fun setUserLocation(userLoc: Location) {
        userLocation.postValue(userLoc)
    }

    fun insertDistance(distance: Int, date: String) {
        viewModelScope.launch {
            database.statDao().updateDistanceTravelled(distance, date)
        }
    }

    fun addFavourite(helsinkiItem: Helsinki?) {
        viewModelScope.launch {
            addFavouriteToDatabase(helsinkiItem, database.favoriteDao())
        }
    }

    fun deleteFavorite(helsinkiItem: Helsinki?) {
        viewModelScope.launch {
            deleteFavoriteFromDatabase(helsinkiItem, database.favoriteDao())
        }
    }
}

class MapViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
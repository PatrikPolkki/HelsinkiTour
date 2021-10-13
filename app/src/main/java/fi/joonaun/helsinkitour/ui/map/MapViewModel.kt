package fi.joonaun.helsinkitour.ui.map

import android.content.Context
import android.location.Location
import androidx.lifecycle.*
import fi.joonaun.helsinkitour.database.AppDatabase
import fi.joonaun.helsinkitour.network.Helsinki
import fi.joonaun.helsinkitour.utils.addFavouriteToDatabase
import fi.joonaun.helsinkitour.utils.deleteFavoriteFromDatabase
import kotlinx.coroutines.launch

class MapViewModel(context: Context) : ViewModel() {
    private val database = AppDatabase.get(context)

    private val mUserLocation: MutableLiveData<Location> by lazy {
        MutableLiveData<Location>().also {
            it.value = null
        }
    }

    val userLocation: LiveData<Location?>
        get() = mUserLocation

    private val mHelsinkiList: MutableLiveData<List<Helsinki>> by lazy {
        MutableLiveData<List<Helsinki>>().also { it.value = listOf() }
    }
    val helsinkiList: LiveData<List<Helsinki>>
        get() = mHelsinkiList

    fun setHelsinkiList(list: List<Helsinki>) {
        mHelsinkiList.value = list
    }

    fun initUserLocation(prefLocation: Location) {
        mUserLocation.value = prefLocation
    }

    fun setUserLocation(userLoc: Location) {
        mUserLocation.postValue(userLoc)
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
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
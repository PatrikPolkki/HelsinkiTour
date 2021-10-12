package fi.joonaun.helsinkitour

import android.content.Context
import androidx.lifecycle.*
import fi.joonaun.helsinkitour.database.AppDatabase
import fi.joonaun.helsinkitour.database.Stat
import fi.joonaun.helsinkitour.network.Activity
import fi.joonaun.helsinkitour.network.Event
import fi.joonaun.helsinkitour.network.HelsinkiRepository
import fi.joonaun.helsinkitour.network.Place
import fi.joonaun.helsinkitour.ui.map.MapLocation
import fi.joonaun.helsinkitour.utils.getTodayDate
import kotlinx.coroutines.launch

class MainViewModel(context: Context) : ViewModel() {
    private val repository: HelsinkiRepository = HelsinkiRepository
    private val database = AppDatabase.get(context)

    var mapLocation: MapLocation? = null

    private val mPlaces: MutableLiveData<List<Place>> by lazy {
        MutableLiveData<List<Place>>()
    }
    val places: LiveData<List<Place>>
        get() = mPlaces

    private val mEvents: MutableLiveData<List<Event>> by lazy {
        MutableLiveData<List<Event>>()
    }
    val events: LiveData<List<Event>>
        get() = mEvents

    private val mActivities: MutableLiveData<List<Activity>> by lazy {
        MutableLiveData<List<Activity>>()
    }
    val activities: LiveData<List<Activity>>
        get() = mActivities

    private var stats: Stat? = null

    var stepsBegin: Int? = null

    fun getAll() {
        getActivities()
        getEvents()
        getPlaces()
    }

    private fun getPlaces() {
        viewModelScope.launch {
            val result = repository.getAllPlaces()
            mPlaces.postValue(result.data)
        }
    }

    private fun getEvents() {
        viewModelScope.launch {
            val result = repository.getAllEvents()
            mEvents.postValue(result.data)
        }
    }

    private fun getActivities() {
        viewModelScope.launch {
            val result = repository.getAllActivities()
            mActivities.postValue(result.data)
        }
    }

    fun addStatsIfNotExist() {
        viewModelScope.launch {
            val date = getTodayDate()
            val newStat = Stat(date)
            val id = database.statDao().insert(newStat)
            stats = if (id == -1L) database.statDao().get(date) else newStat
        }
    }

    fun updateSteps(amount: Int) {
        viewModelScope.launch {
            stats?.let {
                database.statDao().updateSteps(amount, it.date)
            }
        }
    }
}

class MainViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
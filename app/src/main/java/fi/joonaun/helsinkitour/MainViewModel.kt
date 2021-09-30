package fi.joonaun.helsinkitour

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fi.joonaun.helsinkitour.network.Activity
import fi.joonaun.helsinkitour.network.Event
import fi.joonaun.helsinkitour.network.HelsinkiRepository
import fi.joonaun.helsinkitour.network.Place
import fi.joonaun.helsinkitour.utils.parseHtml
import kotlinx.coroutines.launch

class MainViewModel() : ViewModel() {
    private val repository: HelsinkiRepository = HelsinkiRepository

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

    fun getAll() {
        getActivities()
        getEvents()
        getPlaces()
    }

    private fun getPlaces() {
        viewModelScope.launch {
            val result = repository.getAllPlaces()
            result.data.forEach {
                it.description.body = parseHtml(it.description.body)
            }
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
}
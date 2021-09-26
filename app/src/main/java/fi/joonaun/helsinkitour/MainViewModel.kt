package fi.joonaun.helsinkitour

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fi.joonaun.helsinkitour.network.*
import kotlinx.coroutines.launch

class MainViewModel() : ViewModel() {
    private val repository: HelsinkiRepository = HelsinkiRepository()

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

    fun getAll(language: String = "en") {
        getActivities(language)
        getEvents(language)
        getPlaces(language)
    }

    private fun getPlaces(language: String = "en") {
        viewModelScope.launch {
            val result = repository.getAllPlaces(language)
            mPlaces.postValue(result.data)
        }
    }

    private fun getEvents(language: String = "en") {
        viewModelScope.launch {
            val result = repository.getAllEvents(language)
            mEvents.postValue(result.data)
        }
    }

    private fun getActivities(language: String = "en") {
        viewModelScope.launch {
            val result = repository.getAllActivities(language)
            mActivities.postValue(result.data)
        }
    }
}
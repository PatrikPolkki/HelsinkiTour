package fi.joonaun.helsinkitour

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fi.joonaun.helsinkitour.network.Activities
import fi.joonaun.helsinkitour.network.Events
import fi.joonaun.helsinkitour.network.HelsinkiRepository
import fi.joonaun.helsinkitour.network.Places
import kotlinx.coroutines.launch

class MainViewModel() : ViewModel() {
    private val repository: HelsinkiRepository = HelsinkiRepository()

    private val mPlaces: MutableLiveData<Places> by lazy {
        MutableLiveData<Places>()
    }
    val places: LiveData<Places>
        get() = mPlaces

    private val mEvents: MutableLiveData<Events> by lazy {
        MutableLiveData<Events>()
    }
    val events: LiveData<Events>
        get() = mEvents

    private val mActivities: MutableLiveData<Activities> by lazy {
        MutableLiveData<Activities>()
    }
    val activities: LiveData<Activities>
        get() = mActivities

    fun getAll(language: String = "en") {
        getActivities(language)
        getEvents(language)
        getPlaces(language)
    }

    private fun getPlaces(language: String = "en") {
        viewModelScope.launch {
            val result = repository.getAllPlaces(language)
            mPlaces.postValue(result)
        }
    }

    private fun getEvents(language: String = "en") {
        viewModelScope.launch {
            val result = repository.getAllEvents(language)
            mEvents.postValue(result)
        }
    }

    private fun getActivities(language: String = "en") {
        viewModelScope.launch {
            val result = repository.getAllActivities(language)
            mActivities.postValue(result)
        }
    }
}
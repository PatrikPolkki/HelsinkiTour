package fi.joonaun.helsinkitour

import androidx.core.text.HtmlCompat
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
            result.data.forEach {
                it.description.body = parseHtml(it.description.body)
            }
            mPlaces.postValue(result.data)
        }
    }

    private fun getEvents(language: String = "en") {
        viewModelScope.launch {
            val result = repository.getAllEvents(language)
            result.data.forEach {
                it.description.body = parseHtml(it.description.body)
            }
            mEvents.postValue(result.data)
        }
    }

    private fun getActivities(language: String = "en") {
        viewModelScope.launch {
            val result = repository.getAllActivities(language)
            result.data.forEach {
                it.description.body = parseHtml(it.description.body)
            }
            mActivities.postValue(result.data)
        }
    }

    private fun parseHtml(text: String): String {
        var result = HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
        result = result.replace("\n", "\n\n")
        result.trim()
        return result
    }
}
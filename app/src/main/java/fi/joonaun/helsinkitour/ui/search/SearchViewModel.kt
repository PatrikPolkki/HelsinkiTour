package fi.joonaun.helsinkitour.ui.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fi.joonaun.helsinkitour.network.Helsinki
import fi.joonaun.helsinkitour.network.HelsinkiRepository
import fi.joonaun.helsinkitour.utils.HelsinkiType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SearchViewModel() : ViewModel() {
    companion object {
        private var searchJob = Job()
    }

    private val repository = HelsinkiRepository

    private val mSearchResults: MutableLiveData<List<Helsinki>> by lazy {
        MutableLiveData<List<Helsinki>>().also { it.value = listOf() }
    }
    val searchResults: MutableLiveData<List<Helsinki>>
        get() = mSearchResults

    private val mSelectedType: MutableLiveData<HelsinkiType> by lazy {
        MutableLiveData<HelsinkiType>().also { it.value = HelsinkiType.ACTIVITY }
    }
    val selectedType: LiveData<HelsinkiType>
        get() = mSelectedType

    fun setSelectedType(type: HelsinkiType) {
        mSelectedType.value = type
    }

    private var searchText = ""

    fun search(s: CharSequence, start: Int, before: Int, count: Int) {
        Log.d("SEARCH TEXT", s.toString())
        searchText = s.trim().toString().lowercase()
        searchJob.cancel()
        searchJob = Job()
        doSearch()
    }

    fun doSearch() {
        mSearchResults.value = listOf()
        viewModelScope.launch(Dispatchers.Default + searchJob) {
            val result = when (selectedType.value) {
                HelsinkiType.ACTIVITY -> repository.getAllActivities().data
                    .filter { stringMatch(it.getLocaleName(), searchText) }
                HelsinkiType.EVENT -> repository.getAllEvents().data
                    .filter { stringMatch(it.getLocaleName(), searchText) }
                HelsinkiType.PLACE -> repository.getAllPlaces().data
                    .filter { stringMatch(it.getLocaleName(), searchText) }
                else -> listOf()
            }
            mSearchResults.postValue(result)
        }
    }

    private fun stringMatch(str: String?, input: String): Boolean {
        return str?.lowercase()?.contains(input) ?: false
    }
}
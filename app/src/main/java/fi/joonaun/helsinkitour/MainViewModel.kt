package fi.joonaun.helsinkitour

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fi.joonaun.helsinkitour.network.HelsinkiRepository
import kotlinx.coroutines.launch

class MainViewModel(context: Context) : ViewModel() {
    private val repository: HelsinkiRepository = HelsinkiRepository()

    fun getAllPlaces() {
        viewModelScope.launch {
            val result = repository.getAllPlaces("en")
            Log.d("AllPlaces", "$result")
        }
    }

    fun getAllEvents() {
        viewModelScope.launch {
            val result = repository.getAllEvents("en")
            Log.d("AllEvents", "$result")
        }
    }

    fun getAllActivities() {
        viewModelScope.launch {
            val result = repository.getAllActivities("en")
            Log.d("AllActivities", "$result")
        }
    }
}

class MainViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            // Sorry Patrick, but this is how Google do this
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
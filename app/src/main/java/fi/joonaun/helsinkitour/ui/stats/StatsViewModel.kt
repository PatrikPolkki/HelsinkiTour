package fi.joonaun.helsinkitour.ui.stats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import fi.joonaun.helsinkitour.database.AppDatabase

class StatsViewModel(context: Context) : ViewModel() {
    private val database = AppDatabase.get(context)
    val totalSteps: LiveData<Int?> = database.statDao().getTotalSteps()
}

class StatsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(StatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatsViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
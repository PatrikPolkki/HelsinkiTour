package fi.joonaun.helsinkitour.ui.stats

import android.content.Context
import androidx.lifecycle.*
import fi.joonaun.helsinkitour.database.AppDatabase
import fi.joonaun.helsinkitour.database.Favorite
import fi.joonaun.helsinkitour.network.Helsinki
import fi.joonaun.helsinkitour.ui.search.SearchViewModel
import fi.joonaun.helsinkitour.utils.HelsinkiType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class StatsViewModel(context: Context) : ViewModel() {
    private val database = AppDatabase.get(context)
    val totalSteps: LiveData<Int?> = database.statDao().getTotalSteps()

    val aFavorite = database.favoriteDao().getType(HelsinkiType.ACTIVITY)
    val pFavorite = database.favoriteDao().getType(HelsinkiType.PLACE)
    val eFavorite = database.favoriteDao().getType(HelsinkiType.EVENT)
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
package fi.joonaun.helsinkitour.ui.stats

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import fi.joonaun.helsinkitour.database.AppDatabase
import fi.joonaun.helsinkitour.utils.HelsinkiType

class StatsViewModel(context: Context) : ViewModel() {
    private val database = AppDatabase.get(context)
    val totalSteps: LiveData<Int?> = database.statDao().getTotalSteps()

    private val mUsername: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            it.value = ""
        }
    }

    val username: LiveData<String>
        get() = mUsername

    fun initUsername(prefName: String) {
        mUsername.value = prefName
    }

    fun setUsername(username: String) {
        mUsername.postValue(username)
    }

    val aFavorite = database.favoriteDao().getType(HelsinkiType.ACTIVITY)
    val pFavorite = database.favoriteDao().getType(HelsinkiType.PLACE)
    val eFavorite = database.favoriteDao().getType(HelsinkiType.EVENT)
}

/**
 * ViewModelFactory for [StatsViewModel]
 * @param context Context for database
 */
class StatsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatsViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
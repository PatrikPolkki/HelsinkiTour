package fi.joonaun.helsinkitour.ui.search.bottomsheet

import android.content.Context
import androidx.lifecycle.*
import fi.joonaun.helsinkitour.database.AppDatabase
import fi.joonaun.helsinkitour.network.Helsinki
import fi.joonaun.helsinkitour.utils.addFavouriteToDatabase
import fi.joonaun.helsinkitour.utils.deleteFavoriteFromDatabase
import kotlinx.coroutines.launch

class InfoBottomSheetViewModel(context: Context, id: String) : ViewModel() {
    private val database = AppDatabase.get(context)
    private val favoriteDao = database.favoriteDao()

    /**
     * Gets favorite from database.
     * Is null if no values with id found
     */
    val favorite = favoriteDao.get(id)

    /**
     * MutableLiveData for view [Helsinki] item
     */
    private val mHelsinkiItem: MutableLiveData<Helsinki> by lazy {
        MutableLiveData<Helsinki>()
    }
    val helsinkiItem: LiveData<Helsinki>
        get() = mHelsinkiItem

    /**
     * Sets [mHelsinkiItem] value to [item]
     */
    fun setHelsinkiItem(item: Helsinki) {
        mHelsinkiItem.value = item
    }

    /**
     * Adds [helsinkiItem] to favorite database
     */
    fun addFavourite() {
        viewModelScope.launch {
            addFavouriteToDatabase(helsinkiItem.value, favoriteDao)
        }
    }

    /**
     * Deletes [helsinkiItem] from favorite database
     */
    fun deleteFavorite() {
        viewModelScope.launch {
            deleteFavoriteFromDatabase(helsinkiItem.value, favoriteDao)
        }
    }
}

/**
 * ViewModelFactory for [InfoBottomSheetViewModel]
 * @param context Context for database
 * @param id HelsinkiItems id
 */
class InfoBottomSheetViewModelFactory(private val context: Context, private val id: String) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InfoBottomSheetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InfoBottomSheetViewModel(context, id) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package fi.joonaun.helsinkitour.ui.search.bottomsheet

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import fi.joonaun.helsinkitour.database.AppDatabase
import fi.joonaun.helsinkitour.database.Favorite
import fi.joonaun.helsinkitour.network.Activity
import fi.joonaun.helsinkitour.network.Event
import fi.joonaun.helsinkitour.network.Helsinki
import fi.joonaun.helsinkitour.network.Place
import fi.joonaun.helsinkitour.utils.HelsinkiType
import kotlinx.coroutines.launch

class InfoBottomSheetViewModel(context: Context, id: String) : ViewModel() {
    private val database = AppDatabase.get(context)
    private val favoriteDao = database.favoriteDao()

    val favorite = favoriteDao.get(id)

    private val mHelsinkiItem: MutableLiveData<Helsinki> by lazy {
        MutableLiveData<Helsinki>()
    }
    val helsinkiItem: LiveData<Helsinki>
        get() = mHelsinkiItem

    fun setHelsinkiItem(item: Helsinki) {
        mHelsinkiItem.value = item
    }

    fun addFavourite() {
        viewModelScope.launch {
            helsinkiItem.value?.let {
                val item = makeFavoriteItem(it) ?: return@launch
                val id = favoriteDao.insert(item)
                Log.d("FAVORITE", "Inserted wit id: $id")
            }
        }
    }

    fun deleteFavorite() {
        viewModelScope.launch {
            helsinkiItem.value?.let {
                val item = makeFavoriteItem(it) ?: return@launch
                favoriteDao.delete(item)
                Log.d("FAVORITE", "Deleted")
            }
        }
    }

    private fun makeFavoriteItem(item: Helsinki): Favorite? {
        return Favorite(
            item.id,
            when (item) {
                is Event -> HelsinkiType.EVENT
                is Place -> HelsinkiType.PLACE
                is Activity -> HelsinkiType.ACTIVITY
                else -> return null
            },
            item.getLocaleName() ?: return null,
            item.description.images?.firstOrNull()?.url ?: return null
        )
    }
}

class InfoBottomSheetViewModelFactory(private val context: Context, private val id: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InfoBottomSheetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InfoBottomSheetViewModel(context, id) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}

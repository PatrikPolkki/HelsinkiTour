package fi.joonaun.helsinkitour.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Favorite): Long

    @Delete
    suspend fun delete(item: Favorite)

    @Update
    suspend fun update(item: Favorite)

    @Query("SELECT * FROM Favorite WHERE id =:id")
    fun get(id: String): LiveData<Favorite?>
}
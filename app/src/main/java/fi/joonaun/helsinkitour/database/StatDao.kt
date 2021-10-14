package fi.joonaun.helsinkitour.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StatDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(stat: Stat): Long

    @Query("SELECT * FROM Stat WHERE date = :date")
    suspend fun get(date: String): Stat?

    @Query("UPDATE stat SET steps = steps + :amount WHERE date = :date")
    suspend fun updateSteps(amount: Int, date: String)

    @Query("UPDATE stat SET distance_travelled = distance_travelled + :amount WHERE date = :date")
    suspend fun updateDistanceTravelled(amount: Int, date: String)

    @Query("SELECT SUM(steps) FROM stat")
    fun getTotalSteps(): LiveData<Int?>

    @Query("SELECT SUM(distance_travelled) FROM stat")
    fun getTotalDistance(): LiveData<Int?>
}
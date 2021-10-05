package fi.joonaun.helsinkitour.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Favorite::class, Stat::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun statDao(): StatDao

    companion object {
        @Volatile
        private var mInstance: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return mInstance ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                mInstance = instance
                instance
            }
        }
    }
}
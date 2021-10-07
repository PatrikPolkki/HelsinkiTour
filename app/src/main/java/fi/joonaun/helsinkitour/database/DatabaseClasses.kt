package fi.joonaun.helsinkitour.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import fi.joonaun.helsinkitour.utils.HelsinkiType

@Entity
data class Favorite(
    @PrimaryKey val id: String,
    val type: HelsinkiType,
    val name: String,
    val imageUrl: String?,
    val shortDescription: String?
)

@Entity
data class Stat(
    @PrimaryKey val date: String,
    val steps: Int = 0,
    @ColumnInfo(name = "distance_travelled") val distanceTravelled: Int = 0
)
package fi.joonaun.helsinkitour.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import fi.joonaun.helsinkitour.utils.HelsinkiType

@Entity
data class Favorite(
    @PrimaryKey val id: String,
    val type: HelsinkiType,
    val name: String,
    val imageUrl: String?
)
package io.mityukov.geo.tracking.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "track_point")
data class TrackPointEntity(
    @PrimaryKey  val id: String,
    @ColumnInfo(name = "track_id") val trackId: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Float,
    val time: Long
)

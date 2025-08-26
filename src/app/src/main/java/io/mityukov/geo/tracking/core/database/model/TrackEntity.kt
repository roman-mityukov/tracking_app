package io.mityukov.geo.tracking.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "track")
data class TrackEntity(
    @PrimaryKey val id: String,
    val name: String,
    val start: Long,
    val end: Long,
    val duration: Long,
    val distance: Float,
    @ColumnInfo(name = "altitude_up")
    val altitudeUp: Float,
    @ColumnInfo(name = "altitude_down")
    val altitudeDown: Float,
    @ColumnInfo(name = "average_speed")
    val averageSpeed: Float,
    @ColumnInfo(name = "min_speed")
    val minSpeed: Float,
    @ColumnInfo(name = "max_speed")
    val maxSpeed: Float,
    @ColumnInfo(name = "file_path")
    val filePath: String,
)

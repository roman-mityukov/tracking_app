package io.mityukov.geo.tracking.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
@Entity(tableName = "track")
data class TrackEntity(
    @PrimaryKey val id: String,
    val name: String,
    val start: Long,
    val end: Long,
    val duration: Long,
    val distance: Int,
    @ColumnInfo(name = "altitude_up")
    val altitudeUp: Int,
    @ColumnInfo(name = "altitude_down")
    val altitudeDown: Int,
    @ColumnInfo(name = "average_speed")
    val averageSpeed: Float,
    @ColumnInfo(name = "min_speed")
    val minSpeed: Float,
    @ColumnInfo(name = "max_speed")
    val maxSpeed: Float,
    @ColumnInfo(name = "file_path")
    val filePath: String,
)

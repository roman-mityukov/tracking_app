package io.mityukov.geo.tracking.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track_action")
data class TrackActionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "track_id") val trackId: String,
    val timestamp: String,
    val action: String,
)

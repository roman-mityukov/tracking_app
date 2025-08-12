package io.mityukov.geo.tracking.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "track")
data class TrackEntity(
    @PrimaryKey val id: String,
    val name: String,
    val start: String,
    val end: String,
)

package io.mityukov.geo.tracking.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track")
data class TrackEntity(@PrimaryKey  val id: String, val name: String, val duration: Long)

package io.mityukov.geo.tracking.core.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class TrackWithPoints(
    @Embedded
    val track: TrackEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "track_id"
    )
    val points: List<TrackPointEntity>
)
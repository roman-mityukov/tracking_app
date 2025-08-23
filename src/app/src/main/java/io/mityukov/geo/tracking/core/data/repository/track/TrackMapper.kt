package io.mityukov.geo.tracking.core.data.repository.track

import io.mityukov.geo.tracking.core.database.model.TrackEntity
import io.mityukov.geo.tracking.core.model.track.Track
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class TrackMapper @Inject constructor() {
    fun trackEntityToDomain(entity: TrackEntity): Track {
        return Track(
            id = entity.id,
            name = entity.name,
            start = entity.start,
            end = entity.end,
            distance = entity.distance,
            altitudeUp = entity.altitudeUp,
            altitudeDown = entity.altitudeDown,
            duration = entity.duration.seconds,
            averageSpeed = entity.averageSpeed,
            minSpeed = entity.minSpeed,
            maxSpeed = entity.maxSpeed,
            filePath = entity.filePath,
        )
    }
}

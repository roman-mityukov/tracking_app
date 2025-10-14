package io.mityukov.geo.tracking.core.data.repository.track

import io.mityukov.geo.tracking.core.database.model.TrackEntity
import io.mityukov.geo.tracking.core.model.track.Track
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

internal class TrackMapper @Inject constructor() {
    fun trackDomainToEntity(track: Track): TrackEntity {
        return TrackEntity(
            id = track.id,
            description = track.description,
            name = track.name,
            start = track.start,
            end = track.end,
            distance = track.distance,
            altitudeUp = track.altitudeUp,
            altitudeDown = track.altitudeDown,
            duration = track.duration.inWholeSeconds,
            sumSpeed = track.sumSpeed,
            minSpeed = track.minSpeed,
            maxSpeed = track.maxSpeed,
            geolocationCount = track.geolocationCount,
            filePath = track.filePath,
        )
    }

    fun trackEntityToDomain(entity: TrackEntity): Track {
        return Track(
            id = entity.id,
            description = entity.description ?: "",
            name = entity.name,
            start = entity.start,
            end = entity.end,
            distance = entity.distance,
            altitudeUp = entity.altitudeUp,
            altitudeDown = entity.altitudeDown,
            duration = entity.duration.seconds,
            sumSpeed = entity.sumSpeed,
            minSpeed = entity.minSpeed,
            maxSpeed = entity.maxSpeed,
            geolocationCount = entity.geolocationCount,
            filePath = entity.filePath,
        )
    }
}

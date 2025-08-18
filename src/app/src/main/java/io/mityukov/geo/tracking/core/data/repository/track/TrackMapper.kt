package io.mityukov.geo.tracking.core.data.repository.track

import io.mityukov.geo.tracking.core.database.model.TrackActionEntity
import io.mityukov.geo.tracking.core.database.model.TrackPointEntity
import io.mityukov.geo.tracking.core.database.model.TrackWithPoints
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.core.model.track.Track
import io.mityukov.geo.tracking.core.model.track.TrackAction
import io.mityukov.geo.tracking.core.model.track.TrackActionType
import io.mityukov.geo.tracking.core.model.track.TrackPoint
import io.mityukov.geo.tracking.utils.geolocation.distanceTo
import javax.inject.Inject

class TrackMapper @Inject constructor() {
    fun trackWithPointsEntityToDomain(entity: TrackWithPoints): Track {
        return Track(
            id = entity.track.id,
            name = entity.track.name,
            start = entity.track.start,
            end = entity.track.end,
            distance = if (entity.points.size > 1) {
                var result = 0
                entity.points.forEachIndexed { index, point ->
                    if (index > 0) {
                        val firstPoint = entity.points[index - 1]
                        val secondPoint = entity.points[index]
                        result += firstPoint.distanceTo(secondPoint)
                    }
                }
                result
            } else {
                0
            },
            altitudeUp = if (entity.points.size > 1) {
                var result = 0.0
                entity.points.forEachIndexed { index, point ->
                    if (index > 0) {
                        val firstPoint = entity.points[index - 1]
                        val secondPoint = entity.points[index]

                        if (secondPoint.altitude > firstPoint.altitude) {
                            result += secondPoint.altitude - firstPoint.altitude
                        }
                    }
                }
                result.toInt()
            } else {
                0
            },
            altitudeDown = if (entity.points.size > 1) {
                var result = 0.0
                entity.points.forEachIndexed { index, point ->
                    if (index > 0) {
                        val firstPoint = entity.points[index - 1]
                        val secondPoint = entity.points[index]

                        if (secondPoint.altitude < firstPoint.altitude) {
                            result += firstPoint.altitude - secondPoint.altitude
                        }
                    }
                }
                result.toInt()
            } else {
                0
            },
            points = entity.points.map(::trackPointEntityToDomain),
            actions = entity.actions.map(::trackActionEntityToDomain)
        )
    }

    fun trackPointEntityToDomain(entity: TrackPointEntity): TrackPoint {
        return TrackPoint(
            id = entity.id,
            geolocation = Geolocation(
                latitude = entity.latitude,
                longitude = entity.longitude,
                altitude = entity.altitude,
                time = entity.time
            )
        )
    }

    fun trackActionEntityToDomain(entity: TrackActionEntity): TrackAction {
        return TrackAction(
            id = entity.id,
            trackId = entity.trackId,
            type = TrackActionType.valueOf(entity.action),
            timestamp = entity.timestamp,
        )
    }
}

private fun TrackPointEntity.distanceTo(other: TrackPointEntity): Int {
    return distanceTo(
        this.latitude,
        this.longitude,
        this.altitude,
        other.latitude,
        other.longitude,
        other.altitude
    )
}

package io.mityukov.geo.tracking.core.data.repository.track

import android.location.Location
import io.mityukov.geo.tracking.core.database.model.TrackWithPoints
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.core.model.track.Track
import io.mityukov.geo.tracking.core.model.track.TrackPoint
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class TrackMapper @Inject constructor() {
    fun trackWithPointsEntityToDomain(entity: TrackWithPoints): Track {
        return Track(
            id = entity.track.id,
            duration = if (entity.points.size > 1) {
                (entity.points.last().time - entity.points.first().time).milliseconds
            } else {
                Duration.ZERO
            },
            distance = if (entity.points.size > 1) {
                var result = 0.0f
                val floatArray = FloatArray(1)
                entity.points.forEachIndexed { index, point ->
                    if (index > 0) {
                        val firstPoint = entity.points[index - 1]
                        val secondPoint = entity.points[index]
                        Location.distanceBetween(
                            firstPoint.latitude,
                            firstPoint.longitude,
                            secondPoint.latitude,
                            secondPoint.longitude,
                            floatArray
                        )
                        result += floatArray[0]
                    }
                }
                result.toInt()
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
            points = entity.points.map {
                TrackPoint(
                    id = it.id,
                    geolocation = Geolocation(
                        latitude = it.latitude,
                        longitude = it.longitude,
                        altitude = it.altitude,
                        time = it.time
                    )
                )
            }
        )
    }
}

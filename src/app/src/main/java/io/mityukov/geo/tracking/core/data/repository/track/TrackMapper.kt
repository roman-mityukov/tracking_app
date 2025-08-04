package io.mityukov.geo.tracking.core.data.repository.track

import io.mityukov.geo.tracking.core.database.model.TrackPointEntity
import io.mityukov.geo.tracking.core.database.model.TrackWithPoints
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.core.model.track.Track
import io.mityukov.geo.tracking.core.model.track.TrackPoint
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.milliseconds

class TrackMapper @Inject constructor() {
    fun trackWithPointsEntityToDomain(entity: TrackWithPoints): Track {
        return Track(
            id = entity.track.id,
            duration = entity.track.duration.milliseconds,
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
            points = entity.points.map(::trackPointEntityToDomain)
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
}

private fun TrackPointEntity.distanceTo(other: TrackPointEntity): Int {
    val earthRadius = 6_371_008

    val lat1Rad = this.latitude.toRadians()
    val lon1Rad = this.longitude.toRadians()
    val lat2Rad = other.latitude.toRadians()
    val lon2Rad = other.longitude.toRadians()

    val deltaLat = lat2Rad - lat1Rad
    val deltaLon = lon2Rad - lon1Rad

    // Формула гаверсинусов (2D расстояние по поверхности)
    val a = sin(deltaLat / 2).pow(2) +
            cos(lat1Rad) * cos(lat2Rad) * sin(deltaLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    val horizontalDistance = earthRadius * c

    val deltaAlt = other.altitude - this.altitude

    // Полное расстояние (3D)
    return sqrt(horizontalDistance.pow(2) + deltaAlt.pow(2)).toInt()
}

private fun Double.toRadians() = this * PI / 180

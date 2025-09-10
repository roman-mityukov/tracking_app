package io.mityukov.geo.tracking.core.model.track

import io.mityukov.geo.tracking.core.model.geo.Geolocation
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private fun Double.toRadians() = this * PI / 180
fun distanceTo(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double,
): Int {
    val earthRadius = 6_371_008

    val lat1Rad = lat1.toRadians()
    val lon1Rad = lon1.toRadians()
    val lat2Rad = lat2.toRadians()
    val lon2Rad = lon2.toRadians()

    val deltaLat = lat2Rad - lat1Rad
    val deltaLon = lon2Rad - lon1Rad

    // Формула гаверсинусов (2D расстояние по поверхности)
    val a = sin(deltaLat / 2).pow(2) +
            cos(lat1Rad) * cos(lat2Rad) * sin(deltaLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    val horizontalDistance = earthRadius * c

    return horizontalDistance.roundToInt().absoluteValue
}

data class DetailedTrack(
    val track: Track,
    val geolocations: List<Geolocation>
) {
    val altitudeByDistance: List<Pair<Int, Int>> by lazy {
        val result = mutableListOf<Pair<Int, Int>>()
        var currentDistance = 0
        geolocations.forEachIndexed { index, point ->
            if (index > 0) {
                val firstPoint = geolocations[index - 1]
                val secondPoint = geolocations[index]
                currentDistance += distanceTo(
                    firstPoint.latitude,
                    firstPoint.longitude,
                    secondPoint.latitude,
                    secondPoint.longitude,
                )
                result.add(Pair(secondPoint.altitude.toInt(), currentDistance))
            } else {
                result.add(Pair(geolocations[index].altitude.toInt(), 0))
            }
        }
        result.toList()
    }

    val speedByDistance: List<Pair<Double, Int>> by lazy {
        val result = mutableListOf<Pair<Double, Int>>()
        var currentDistance = 0
        geolocations.forEachIndexed { index, point ->
            if (index > 0) {
                val firstPoint = geolocations[index - 1]
                val secondPoint = geolocations[index]
                currentDistance += distanceTo(
                    firstPoint.latitude,
                    firstPoint.longitude,
                    secondPoint.latitude,
                    secondPoint.longitude,
                )
                result.add(Pair(secondPoint.speed.toDouble(), currentDistance))
            } else {
                result.add(Pair(geolocations[index].speed.toDouble(), 0))
            }
        }
        result.toList()
    }
}

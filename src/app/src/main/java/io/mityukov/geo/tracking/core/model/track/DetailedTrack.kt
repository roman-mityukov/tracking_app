package io.mityukov.geo.tracking.core.model.track

import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.utils.geolocation.distanceTo

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

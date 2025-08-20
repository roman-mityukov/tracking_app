package io.mityukov.geo.tracking.utils.geolocation

import io.mityukov.geo.tracking.utils.ext.toRadians
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

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

    return horizontalDistance.roundToInt()
}

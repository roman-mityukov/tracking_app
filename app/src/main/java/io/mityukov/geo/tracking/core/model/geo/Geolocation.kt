package io.mityukov.geo.tracking.core.model.geo

data class Geolocation(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val time: Long,
)
package io.mityukov.geo.tracking.core.model.geo

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

data class Geolocation(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Float,
    val time: Long,
) {
    val localDateTime: LocalDateTime
        get() = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(time),
            ZoneId.systemDefault()
        )
}

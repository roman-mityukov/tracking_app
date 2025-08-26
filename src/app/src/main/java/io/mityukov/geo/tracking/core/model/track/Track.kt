package io.mityukov.geo.tracking.core.model.track

import kotlin.time.Duration

data class Track(
    val id: String,
    val name: String,
    val start: Long,
    val end: Long,
    val duration: Duration,
    val distance: Float,
    val altitudeUp: Float,
    val altitudeDown: Float,
    val sumSpeed: Float,
    val minSpeed: Float,
    val maxSpeed: Float,
    val geolocationCount: Int,
    val filePath: String,
) {
    val averageSpeed = if (geolocationCount > 0) sumSpeed / geolocationCount else 0f
}

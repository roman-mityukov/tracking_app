package io.mityukov.geo.tracking.core.model.track

import kotlin.time.Duration

data class Track(
    val id: String,
    val duration: Duration,
    val distance: Int,
    val altitudeUp: Int,
    val altitudeDown: Int,
    val points: List<TrackPoint>
)
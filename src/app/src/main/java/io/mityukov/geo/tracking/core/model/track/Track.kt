package io.mityukov.geo.tracking.core.model.track

data class Track(
    val id: String,
    val start: String,
    val end: String,
    val distance: Int,
    val altitudeUp: Int,
    val altitudeDown: Int,
    val points: List<TrackPoint>
) {
    val isCompleted: Boolean = end.isNotEmpty()
}

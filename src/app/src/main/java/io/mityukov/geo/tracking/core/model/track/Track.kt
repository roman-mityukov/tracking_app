package io.mityukov.geo.tracking.core.model.track

data class Track(
    val id: String,
    val name: String,
    val start: String,
    val end: String,
    val distance: Int,
    val altitudeUp: Int,
    val altitudeDown: Int,
    val points: List<TrackPoint>,
    val actions: List<TrackAction>,
) {
    val isCompleted: Boolean = end.isNotBlank()
}

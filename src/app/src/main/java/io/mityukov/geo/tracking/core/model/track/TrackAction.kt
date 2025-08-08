package io.mityukov.geo.tracking.core.model.track

enum class TrackActionType {
    Start, Pause, Resume, Stop
}

data class TrackAction(
    val id: String,
    val trackId: String,
    val type: TrackActionType,
    val timestamp: String,
)

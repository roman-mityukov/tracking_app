package io.mityukov.geo.tracking.core.data.repository.track

import io.mityukov.geo.tracking.core.model.track.Track
import kotlinx.coroutines.flow.Flow

sealed interface TrackCaptureStatus {
    data class Run(val track: Track, val paused: Boolean) : TrackCaptureStatus

    data object Error : TrackCaptureStatus
    data object Idle : TrackCaptureStatus
}

interface TrackCaptureController {
    val status: Flow<TrackCaptureStatus>
    suspend fun start()
    suspend fun resume()
    suspend fun pause()
    suspend fun stop()
    suspend fun bind()
}

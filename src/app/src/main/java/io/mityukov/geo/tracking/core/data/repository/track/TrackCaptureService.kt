package io.mityukov.geo.tracking.core.data.repository.track

import io.mityukov.geo.tracking.core.model.track.Track
import kotlinx.coroutines.flow.Flow

sealed interface TrackCaptureStatus {
    data class Running(val track: Track) : TrackCaptureStatus
    data object Error : TrackCaptureStatus
    data object Idle : TrackCaptureStatus
}

interface TrackCaptureService {
    val status: Flow<TrackCaptureStatus>
    suspend fun switch()
    suspend fun start()
    suspend fun stop()
    suspend fun bind()
}

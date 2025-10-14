package io.mityukov.geo.tracking.core.data.repository.track.capture

import kotlinx.coroutines.flow.Flow

interface TrackCaptureStatusProvider {
    val status: Flow<TrackCaptureStatus>
}

interface TrackCaptureStatusRepository : TrackCaptureStatusProvider {
    suspend fun update(status: TrackCaptureStatus)
}

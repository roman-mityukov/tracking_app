package io.mityukov.geo.tracking.core.data.repository.track.capture

import kotlinx.coroutines.flow.Flow

sealed interface LocalTrackCaptureStatus {
    data class Enabled(
        val trackInProgress: TrackInProgress = TrackInProgress.empty()
    ) : LocalTrackCaptureStatus

    data object Disabled : LocalTrackCaptureStatus
}

interface TrackCaptureStatusProvider {
    val status: Flow<LocalTrackCaptureStatus>
}

interface TrackCaptureStatusRepository : TrackCaptureStatusProvider {
    suspend fun update(localTrackCaptureStatus: LocalTrackCaptureStatus)
}

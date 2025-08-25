package io.mityukov.geo.tracking.core.data.repository.track.capture

import android.location.Location
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

data class TrackInProgress(
    val start: Long,
    val duration: Duration,
    val distance: Int,
    val altitudeUp: Int,
    val altitudeDown: Int,
    val averageSpeed: Float,
    val minSpeed: Float,
    val maxSpeed: Float,
    val lastLocation: Location?,
    val geolocationCount: Int,
    val paused: Boolean
) {
    companion object {
        fun empty(): TrackInProgress {
            return TrackInProgress(
                start = System.currentTimeMillis(),
                duration = Duration.ZERO,
                distance = 0,
                altitudeUp = 0,
                altitudeDown = 0,
                averageSpeed = 0f,
                minSpeed = 0f,
                maxSpeed = 0f,
                lastLocation = null,
                geolocationCount = 0,
                paused = false,
            )
        }
    }
}

sealed interface TrackCaptureStatus {
    data class Run(val trackInProgress: TrackInProgress) : TrackCaptureStatus
    data object Error : TrackCaptureStatus
    data object Idle : TrackCaptureStatus
}

interface TrackCapturerController {
    val status: Flow<TrackCaptureStatus>
    suspend fun start()
    suspend fun resume()
    suspend fun pause()
    suspend fun stop()
    suspend fun bind()
}

package io.mityukov.geo.tracking.core.data.repository.track

interface TrackCaptureRepository {
    suspend fun start()
    suspend fun stop()
}

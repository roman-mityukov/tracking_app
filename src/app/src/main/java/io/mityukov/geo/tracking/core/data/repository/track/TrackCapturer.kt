package io.mityukov.geo.tracking.core.data.repository.track

interface TrackCapturer {
    @androidx.annotation.RequiresPermission(
        allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION]
    )
    suspend fun start()
    suspend fun stop()
}

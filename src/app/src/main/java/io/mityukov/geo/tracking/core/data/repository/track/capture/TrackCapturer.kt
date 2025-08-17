package io.mityukov.geo.tracking.core.data.repository.track.capture

import android.Manifest
import androidx.annotation.RequiresPermission

interface TrackCapturer {
    @RequiresPermission(
        allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION]
    )
    suspend fun start()
    suspend fun stop()
}

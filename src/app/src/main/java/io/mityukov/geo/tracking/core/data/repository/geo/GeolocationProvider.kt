package io.mityukov.geo.tracking.core.data.repository.geo

import android.location.Location
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

data class PlatformLocationUpdateResult(
    val location: Location?,
    val error: GeolocationUpdateException?,
)

interface GeolocationProvider {
    @androidx.annotation.RequiresPermission(
        allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION]
    )
    suspend fun getLastKnownLocation(): PlatformLocationUpdateResult
    @androidx.annotation.RequiresPermission(
        allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION]
    )
    fun locationUpdates(interval: Duration, minDistance: Float = 0f): Flow<PlatformLocationUpdateResult>
}

package io.mityukov.geo.tracking.core.data.repository.geo

import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

interface GeolocationProvider {
    @androidx.annotation.RequiresPermission(
        allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION]
    )
    suspend fun getLastKnownLocation(): GeolocationUpdateResult
    @androidx.annotation.RequiresPermission(
        allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION]
    )
    fun locationUpdates(interval: Duration): Flow<GeolocationUpdateResult>
}

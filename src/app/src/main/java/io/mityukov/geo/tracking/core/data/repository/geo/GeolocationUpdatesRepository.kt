package io.mityukov.geo.tracking.core.data.repository.geo

import io.mityukov.geo.tracking.core.model.geo.Geolocation
import kotlinx.coroutines.flow.Flow

sealed interface GeolocationUpdateException {
    data object Initialization : GeolocationUpdateException
    data object LocationDisabled : GeolocationUpdateException
    data object LocationIsNull : GeolocationUpdateException
    data object PermissionsNotGranted : GeolocationUpdateException
}

data class GeolocationUpdateResult(
    val geolocation: Geolocation?,
    val error: GeolocationUpdateException?,
)

interface GeolocationUpdatesRepository {
    val currentLocation: Flow<GeolocationUpdateResult>

    @androidx.annotation.RequiresPermission(
        allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION]
    )
    suspend fun start()
    suspend fun stop()
}

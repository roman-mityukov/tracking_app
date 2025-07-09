package io.mityukov.geo.tracking.core.data.repository.geo

import io.mityukov.geo.tracking.core.model.geo.Geolocation
import kotlinx.coroutines.flow.Flow

sealed interface GeolocationUpdateException {
    data object LocationDisabled : GeolocationUpdateException
    data object LocationIsNull : GeolocationUpdateException
    data object PermissionsNotGranted : GeolocationUpdateException
}

data class GeolocationUpdateResult(
    val geolocation: Geolocation?,
    val error: GeolocationUpdateException?,
)

interface GeolocationUpdatesRepository {
    fun getGeolocationUpdates(): Flow<GeolocationUpdateResult>
}
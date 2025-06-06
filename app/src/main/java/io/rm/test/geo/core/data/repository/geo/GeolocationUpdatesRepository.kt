package io.rm.test.geo.core.data.repository.geo

import io.rm.test.geo.core.model.geo.Geolocation
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
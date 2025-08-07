package io.mityukov.geo.tracking.core.data.repository.geo

import io.mityukov.geo.tracking.core.model.geo.Geolocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration

class FakeGeolocationProviderImpl(
    private val emitNullGeolocation: Boolean = false,
    private val mockedGeolocation: Geolocation,
) : GeolocationProvider {
    override suspend fun getLastKnownLocation(): GeolocationUpdateResult {
        return if (emitNullGeolocation) {
            GeolocationUpdateResult(null, GeolocationUpdateException.LocationIsNull)
        } else {
            GeolocationUpdateResult(mockedGeolocation, null)
        }
    }

    override fun locationUpdates(interval: Duration): Flow<GeolocationUpdateResult> = flow {
        emit(getLastKnownLocation())
    }
}

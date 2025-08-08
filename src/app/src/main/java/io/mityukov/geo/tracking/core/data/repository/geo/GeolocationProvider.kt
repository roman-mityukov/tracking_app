package io.mityukov.geo.tracking.core.data.repository.geo

import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

interface GeolocationProvider {
    suspend fun getLastKnownLocation(): GeolocationUpdateResult
    fun locationUpdates(interval: Duration): Flow<GeolocationUpdateResult>
}

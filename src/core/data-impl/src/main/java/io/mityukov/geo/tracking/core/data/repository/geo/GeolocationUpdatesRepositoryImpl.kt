package io.mityukov.geo.tracking.core.data.repository.geo

import android.annotation.SuppressLint
import io.mityukov.geo.tracking.core.common.di.DispatcherIO
import io.mityukov.geo.tracking.core.data.permission.PermissionChecker
import io.mityukov.geo.tracking.core.data.repository.settings.app.LocationSettingsRepository
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

internal class GeolocationUpdatesRepositoryImpl @Inject constructor(
    private val locationSettingsRepository: LocationSettingsRepository,
    private val geolocationProvider: GeolocationProvider,
    @param:DispatcherIO private val coroutineDispatcher: CoroutineDispatcher,
    private val permissionChecker: PermissionChecker,
) : GeolocationUpdatesRepository {
    @SuppressLint("MissingPermission")
    override val currentLocation: Flow<GeolocationUpdateResult> = flow {
        if (locationSettingsRepository.locationEnabled.not()) {
            emit(
                GeolocationUpdateResult(
                    geolocation = null,
                    error = GeolocationUpdateException.LocationDisabled
                )
            )
        } else if (permissionChecker.locationGranted.not()) {
            emit(
                GeolocationUpdateResult(
                    geolocation = null,
                    error = GeolocationUpdateException.PermissionsNotGranted
                )
            )
        } else {
            val lastKnownLocation = geolocationProvider.getLastKnownLocation()
            emit(lastKnownLocation.toGeolocationUpdateResult())

            geolocationProvider.locationUpdates(10000.milliseconds)
                .flowOn(coroutineDispatcher)
                .collect {
                    emit(it.toGeolocationUpdateResult())
                }
        }
    }
}

private fun PlatformLocationUpdateResult.toGeolocationUpdateResult(): GeolocationUpdateResult {
    return GeolocationUpdateResult(
        geolocation = if (location != null) {
            Geolocation(
                latitude = location.latitude,
                longitude = location.longitude,
                altitude = location.altitude,
                speed = location.speed,
                time = location.time,
            )
        } else {
            null
        },
        error = error,
    )
}

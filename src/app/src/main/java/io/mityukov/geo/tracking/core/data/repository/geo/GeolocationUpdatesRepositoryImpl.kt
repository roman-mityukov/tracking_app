package io.mityukov.geo.tracking.core.data.repository.geo

import io.mityukov.geo.tracking.core.data.repository.settings.app.LocationSettingsRepository
import io.mityukov.geo.tracking.di.DispatcherIO
import io.mityukov.geo.tracking.utils.permission.PermissionChecker
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class GeolocationUpdatesRepositoryImpl @Inject constructor(
    private val locationSettingsRepository: LocationSettingsRepository,
    private val geolocationProvider: GeolocationProvider,
    @param:DispatcherIO private val coroutineDispatcher: CoroutineDispatcher,
    private val permissionChecker: PermissionChecker,
) : GeolocationUpdatesRepository {
    private val mutableStateFlow = MutableStateFlow(
        GeolocationUpdateResult(
            null,
            GeolocationUpdateException.Initialization
        )
    )

    override val currentLocation = mutableStateFlow.asStateFlow()

    private var geolocationSubscription: Job? = null
    private val started: Boolean
        get() = geolocationSubscription?.isActive ?: false
    private val mutex = Mutex()

    @androidx.annotation.RequiresPermission(
        allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION]
    )
    override suspend fun start() = withContext(coroutineDispatcher) {
        mutex.withLock {
            if (started.not()) {
                if (locationSettingsRepository.locationEnabled.not()) {
                    mutableStateFlow.update {
                        GeolocationUpdateResult(
                            geolocation = null,
                            error = GeolocationUpdateException.LocationDisabled
                        )
                    }
                } else if (permissionChecker.locationGranted.not()) {
                    mutableStateFlow.update {
                        GeolocationUpdateResult(
                            geolocation = null,
                            error = GeolocationUpdateException.PermissionsNotGranted
                        )
                    }
                } else {
                    launch {
                        val lastKnownLocation = geolocationProvider.getLastKnownLocation()
                        mutableStateFlow.update {
                            lastKnownLocation
                        }
                    }

                    geolocationSubscription = launch {
                        geolocationProvider.locationUpdates(5000.milliseconds).collect { result ->
                            mutableStateFlow.update {
                                result
                            }
                        }
                    }
                }
            }
        }
    }

    override suspend fun stop() = withContext(coroutineDispatcher) {
        mutex.withLock {
            geolocationSubscription?.cancel()
            geolocationSubscription = null
        }
    }
}

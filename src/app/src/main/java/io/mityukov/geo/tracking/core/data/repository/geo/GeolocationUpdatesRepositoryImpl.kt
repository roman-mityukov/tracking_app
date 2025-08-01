package io.mityukov.geo.tracking.core.data.repository.geo

import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LastLocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.di.DispatcherIO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GeolocationUpdatesRepositoryImpl @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    @DispatcherIO private val coroutineDispatcher: CoroutineDispatcher,
) : GeolocationUpdatesRepository {
    private val mutableStateFlow = MutableStateFlow(
        GeolocationUpdateResult(
            null,
            GeolocationUpdateException.LocationIsNull
        )
    )

    override val currentLocation = mutableStateFlow.asStateFlow()

    private val locationManager =
        applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            mutableStateFlow.update {
                GeolocationUpdateResult(
                    geolocation = if (locationResult.lastLocation != null) {
                        Geolocation(
                            latitude = locationResult.lastLocation!!.latitude,
                            longitude = locationResult.lastLocation!!.longitude,
                            altitude = locationResult.lastLocation!!.altitude,
                            time = locationResult.lastLocation!!.time
                        )
                    } else {
                        null
                    },
                    error = if (locationResult.lastLocation == null) {
                        GeolocationUpdateException.LocationIsNull
                    } else {
                        null
                    }
                )
            }
        }
    }

    private var isStarted: Boolean = false
    private val mutex = Mutex()

    override suspend fun start() = withContext(coroutineDispatcher) {
        mutex.withLock {
            if (isStarted.not()) {
                val accessFineLocationGranted = applicationContext.checkSelfPermission(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                val accessCoarseLocationGranted = applicationContext.checkSelfPermission(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (accessCoarseLocationGranted.not()
                    && accessFineLocationGranted.not()
                ) {
                    mutableStateFlow.update {
                        GeolocationUpdateResult(
                            geolocation = null,
                            error = GeolocationUpdateException.PermissionsNotGranted
                        )
                    }
                } else if (locationManager.isLocationEnabled.not()) {
                    mutableStateFlow.update {
                        GeolocationUpdateResult(
                            geolocation = null,
                            error = GeolocationUpdateException.LocationDisabled
                        )
                    }
                } else {
                    fusedLocationProviderClient.getLastLocation(
                        LastLocationRequest.Builder()
                            .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL).build()
                    )
                        .addOnSuccessListener { lastKnownLocation ->
                            if (lastKnownLocation != null) {
                                mutableStateFlow.update {
                                    GeolocationUpdateResult(
                                        geolocation = Geolocation(
                                            latitude = lastKnownLocation.latitude,
                                            longitude = lastKnownLocation.longitude,
                                            altitude = lastKnownLocation.altitude,
                                            time = lastKnownLocation.time,
                                        ), error = null
                                    )
                                }
                            }
                        }

                    val locationRequest = LocationRequest
                        .Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                        .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                        .build()

                    fusedLocationProviderClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper(),
                    )
                    isStarted = true
                }
            }
        }
    }

    override suspend fun stop() = withContext(coroutineDispatcher) {
        mutex.withLock {
            if (isStarted) {
                fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                isStarted = false
            }
        }
    }
}

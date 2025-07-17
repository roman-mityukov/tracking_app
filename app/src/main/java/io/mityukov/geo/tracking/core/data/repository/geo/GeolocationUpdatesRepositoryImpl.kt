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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class GeolocationUpdatesRepositoryImpl @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val fusedLocationProviderClient: FusedLocationProviderClient,
) : GeolocationUpdatesRepository {
    override fun getGeolocationUpdates(): Flow<GeolocationUpdateResult> = callbackFlow {
        val accessFineLocationGranted = applicationContext.checkSelfPermission(
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val accessCoarseLocationGranted = applicationContext.checkSelfPermission(
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val locationManager =
            applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                trySendBlocking(
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
                )
            }
        }

        delay(100)

        if (accessCoarseLocationGranted.not()
            && accessFineLocationGranted.not()
        ) {
            trySend(
                GeolocationUpdateResult(
                    geolocation = null,
                    error = GeolocationUpdateException.PermissionsNotGranted
                )
            )
        } else if (locationManager.isLocationEnabled.not()) {
            trySend(
                GeolocationUpdateResult(
                    geolocation = null,
                    error = GeolocationUpdateException.LocationDisabled
                )
            )
        } else {
            fusedLocationProviderClient.getLastLocation(
                LastLocationRequest.Builder()
                    .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL).build()
            )
                .addOnSuccessListener { lastKnownLocation ->
                    if (lastKnownLocation != null) {
                        trySendBlocking(
                            GeolocationUpdateResult(
                                geolocation = Geolocation(
                                    latitude = lastKnownLocation.latitude,
                                    longitude = lastKnownLocation.longitude,
                                    altitude = lastKnownLocation.altitude,
                                    time = lastKnownLocation.time,
                                ), error = null
                            )
                        )
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
        }
        awaitClose {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }
}

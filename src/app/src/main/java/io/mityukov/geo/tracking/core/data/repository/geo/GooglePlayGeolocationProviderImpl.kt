package io.mityukov.geo.tracking.core.data.repository.geo

import android.annotation.SuppressLint
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LastLocationRequest
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.utils.log.logd
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration

class GooglePlayGeolocationProviderImpl @Inject constructor(
    private val fusedLocationProviderClient: FusedLocationProviderClient,
) : GeolocationProvider {
    @androidx.annotation.RequiresPermission(
        allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION]
    )
    override suspend fun getLastKnownLocation(): GeolocationUpdateResult =
        suspendCoroutine { continuation ->
            fusedLocationProviderClient.getLastLocation(
                LastLocationRequest.Builder()
                    .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL).build()
            )
                .addOnSuccessListener { lastKnownLocation ->
                    if (lastKnownLocation != null) {
                        continuation.resume(
                            GeolocationUpdateResult(
                                geolocation = Geolocation(
                                    latitude = lastKnownLocation.latitude,
                                    longitude = lastKnownLocation.longitude,
                                    altitude = lastKnownLocation.altitude,
                                    time = lastKnownLocation.time,
                                ),
                                error = null,
                            )
                        )
                    } else {
                        continuation.resume(
                            GeolocationUpdateResult(
                                geolocation = null,
                                error = GeolocationUpdateException.LocationIsNull
                            )
                        )
                    }
                }.addOnFailureListener { e ->
                    logd("GooglePlayGeolocationProviderImpl getLastLocation addOnFailureListener $e")
                    continuation.resume(
                        GeolocationUpdateResult(
                            geolocation = null,
                            error = null // TODO надо использовать e
                        )
                    )
                }
        }

    @SuppressLint("MissingPermission")
    @androidx.annotation.RequiresPermission(
        allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION]
    )
    override fun locationUpdates(interval: Duration): Flow<GeolocationUpdateResult> = callbackFlow {
        val locationRequest = LocationRequest
            .Builder(Priority.PRIORITY_HIGH_ACCURACY, interval.inWholeMilliseconds)
            .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation = locationResult.lastLocation
                val result = GeolocationUpdateResult(
                    geolocation = if (lastLocation != null) {
                        Geolocation(
                            latitude = lastLocation.latitude,
                            longitude = lastLocation.longitude,
                            altitude = lastLocation.altitude,
                            time = lastLocation.time
                        )
                    } else {
                        null
                    },
                    error = if (lastLocation == null) {
                        GeolocationUpdateException.LocationIsNull
                    } else {
                        null
                    }
                )
                logd("GooglePlayGeolocationProviderImpl requestLocationUpdates onLocationResult $result")
                trySendBlocking(result)
            }

            override fun onLocationAvailability(p0: LocationAvailability) {
                super.onLocationAvailability(p0)
                logd("GooglePlayGeolocationProviderImpl requestLocationUpdates onLocationAvailability $p0")
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper(),
        )
        awaitClose {
            fusedLocationProviderClient.removeLocationUpdates(callback)
        }
    }
}

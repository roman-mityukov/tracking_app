package io.mityukov.geo.tracking.core.data.repository.geo

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import io.mityukov.geo.tracking.utils.log.logd
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.random.Random
import kotlin.time.Duration

class HardwareGeolocationProvider @Inject constructor(@param:ApplicationContext private val context: Context) :
    GeolocationProvider {
    private val locationManager = context.getSystemService<LocationManager>() as LocationManager
    private val random = Random.nextLong()

    init {
        logd("Init HardwareGeolocationProvider id $random")
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override suspend fun getLastKnownLocation(): PlatformLocationUpdateResult =
        suspendCoroutine { continuation ->
            val lastKnownLocation =
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

            if (lastKnownLocation != null) {
                continuation.resume(
                    PlatformLocationUpdateResult(
                        location = lastKnownLocation,
                        error = null,
                    )
                )
            } else {
                continuation.resume(
                    PlatformLocationUpdateResult(
                        location = null,
                        error = GeolocationUpdateException.LocationIsNull
                    )
                )
            }
        }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun locationUpdates(
        interval: Duration,
        minDistance: Float
    ): Flow<PlatformLocationUpdateResult> =
        callbackFlow {
            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    logd("HardwareGeolocationProvider $random emit location $location")
                    trySendBlocking(
                        PlatformLocationUpdateResult(
                            location = location,
                            error = null
                        )
                    )
                }

                override fun onProviderDisabled(provider: String) {
                    super.onProviderDisabled(provider)

                    if (provider == LocationManager.GPS_PROVIDER) {
                        trySendBlocking(
                            PlatformLocationUpdateResult(
                                location = null,
                                error = GeolocationUpdateException.LocationDisabled
                            )
                        )
                    }
                }
            }

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                interval.inWholeMilliseconds,
                minDistance,
                locationListener,
                Looper.getMainLooper()
            )
            awaitClose {
                logd("Deinit HardwareGeolocationProvider id $random")
                locationManager.removeUpdates(locationListener)
            }
        }
}

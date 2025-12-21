package io.mityukov.geo.tracking.core.data.repository.geo

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.os.PowerManager
import androidx.annotation.RequiresPermission
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import io.mityukov.geo.tracking.log.logd
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration

enum class LocationPowerSaveMode(val value: Int) {
    NoChange(value = 0),
    GpsDisabledWhenScreenOff(value = 1),
    AllDisabledWhenScreenOff(value = 2),
    ForegroundOnly(value = 3),
    ThrottleRequestsWhenScreenOff(value = 4),
}

data class PowerSettings(
    val locationPowerSaveMode: LocationPowerSaveMode,
    val isLowPowerStandbyEnabled: Boolean,
    val isPowerSaveMode: Boolean,
    val isIgnoringBatteryOptimization: Boolean,
    val isExemptFromLowPowerStandby: Boolean,
)

internal class HardwareGeolocationProviderImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) :
    GeolocationProvider {
    private val locationManager = context.getSystemService<LocationManager>() as LocationManager
    private val powerManager = context.getSystemService<PowerManager>() as PowerManager

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
                this@HardwareGeolocationProviderImpl.logd("lastKnownLocation is null")
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
                    this@HardwareGeolocationProviderImpl.logd("emit location $location")
                    trySendBlocking(
                        PlatformLocationUpdateResult(
                            location = location,
                            error = null
                        )
                    )
                }

                override fun onProviderDisabled(provider: String) {
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

            val powerSettings = PowerSettings(
                locationPowerSaveMode = when (powerManager.locationPowerSaveMode) {
                    PowerManager.LOCATION_MODE_NO_CHANGE -> LocationPowerSaveMode.NoChange
                    PowerManager.LOCATION_MODE_GPS_DISABLED_WHEN_SCREEN_OFF ->
                        LocationPowerSaveMode.GpsDisabledWhenScreenOff

                    PowerManager.LOCATION_MODE_ALL_DISABLED_WHEN_SCREEN_OFF ->
                        LocationPowerSaveMode.AllDisabledWhenScreenOff

                    PowerManager.LOCATION_MODE_FOREGROUND_ONLY -> LocationPowerSaveMode.ForegroundOnly
                    PowerManager.LOCATION_MODE_THROTTLE_REQUESTS_WHEN_SCREEN_OFF ->
                        LocationPowerSaveMode.ThrottleRequestsWhenScreenOff

                    else -> throw IllegalArgumentException("Unknown PowerManager locationPowerSaveMode")
                },
                isPowerSaveMode = powerManager.isPowerSaveMode,
                isIgnoringBatteryOptimization = powerManager.isIgnoringBatteryOptimizations("io.mityukov.geo.tracking"),
                isLowPowerStandbyEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    powerManager.isLowPowerStandbyEnabled
                } else false,
                isExemptFromLowPowerStandby = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    powerManager.isExemptFromLowPowerStandby
                } else true,
            )

            this@HardwareGeolocationProviderImpl.logd(
                "powerManager check result\n${powerSettings}"
            )

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                interval.inWholeMilliseconds,
                minDistance,
                locationListener,
                Looper.getMainLooper()
            )
            awaitClose {
                locationManager.removeUpdates(locationListener)
            }
        }
}

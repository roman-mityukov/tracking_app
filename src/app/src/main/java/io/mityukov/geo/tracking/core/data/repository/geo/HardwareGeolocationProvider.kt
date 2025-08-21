package io.mityukov.geo.tracking.core.data.repository.geo

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.OnNmeaMessageListener
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.utils.log.logd
import io.mityukov.geo.tracking.utils.nmea.NmeaData
import io.mityukov.geo.tracking.utils.nmea.NmeaParser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration

class HardwareGeolocationProvider @Inject constructor(@param:ApplicationContext private val context: Context) :
    GeolocationProvider {
    private val locationManager = context.getSystemService<LocationManager>() as LocationManager

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override suspend fun getLastKnownLocation(): GeolocationUpdateResult =
        suspendCoroutine { continuation ->
            val lastKnownLocation =
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

            if (lastKnownLocation != null) {
                continuation.resume(
                    GeolocationUpdateResult(
                        geolocation = Geolocation(
                            latitude = lastKnownLocation.latitude,
                            longitude = lastKnownLocation.longitude,
                            altitude = lastKnownLocation.altitude,
                            speed = lastKnownLocation.speed,
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
        }

    val nmeaGgaBuffer = mutableListOf<NmeaData.GGA>()
    val nmeaRmcBuffer = mutableListOf<NmeaData.RMC>()
    val nmeaHandler = Handler(Looper.getMainLooper())
    val nmeaListener = object : OnNmeaMessageListener {
        val parser = NmeaParser
        override fun onNmeaMessage(line: String, timestamp: Long) {
            if (line.contains("GGA") || line.contains("RMC")) {
                val nmeaData = parser.parseNmeaMessage(line)
                if (nmeaData != null) {
                    if (nmeaData is NmeaData.GGA) {
                        nmeaGgaBuffer.add(nmeaData)
                    } else if (nmeaData is NmeaData.RMC) {
                        nmeaRmcBuffer.add(nmeaData)
                    }
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun locationUpdates(interval: Duration): Flow<GeolocationUpdateResult> = callbackFlow {
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                logd("HardwareGeolocationProviderImpl $location")
                val nmea = mutableListOf<NmeaData>()
                nmea.addAll(nmeaGgaBuffer)
                nmea.addAll(nmeaRmcBuffer)
                nmeaGgaBuffer.clear()
                nmeaRmcBuffer.clear()

                trySendBlocking(
                    GeolocationUpdateResult(
                        geolocation = Geolocation(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            speed = if (location.hasSpeed()) location.speed else 0f,
                            altitude = if (location.hasAltitude()) location.altitude else 0.0,
                            time = location.time
                        ),
                        error = null,
                        nmea = nmea
                    )
                )
            }

            override fun onProviderDisabled(provider: String) {
                super.onProviderDisabled(provider)

                if (provider == LocationManager.GPS_PROVIDER) {
                    trySendBlocking(
                        GeolocationUpdateResult(
                            geolocation = null,
                            error = GeolocationUpdateException.LocationDisabled
                        )
                    )
                }
            }
        }

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            interval.inWholeMilliseconds,      // 10 seconds
            0f,         // 10 meters
            locationListener,
            Looper.getMainLooper()
        )
        locationManager.addNmeaListener(nmeaListener, nmeaHandler)
        awaitClose {
            locationManager.removeUpdates(locationListener)
            locationManager.removeNmeaListener(nmeaListener)
        }
    }
}

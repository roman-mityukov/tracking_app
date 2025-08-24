package io.mityukov.geo.tracking.core.data.repository.track.capture

import android.Manifest
import androidx.annotation.RequiresPermission
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationProvider
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationUpdateResult
import io.mityukov.geo.tracking.core.data.repository.settings.app.AppSettingsRepository
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.di.DispatcherIO
import io.mityukov.geo.tracking.utils.geolocation.distanceTo
import io.mityukov.geo.tracking.utils.log.logd
import io.mityukov.geo.tracking.utils.log.logw
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi

class TrackCapturerImpl @Inject constructor(
    private val trackCaptureStatusRepository: TrackCaptureStatusRepository,
    private val tracksRepository: TracksRepository,
    private val geolocationProvider: GeolocationProvider,
    private val appSettingsRepository: AppSettingsRepository,
    @param:DispatcherIO private val coroutineDispatcher: CoroutineDispatcher,
) : TrackCapturer {
    private val mutex = Mutex()
    private var geolocationSubscription: Job? = null
    private val inProgress: Boolean
        get() = geolocationSubscription?.isActive ?: false

    @RequiresPermission(
        allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION]
    )
    override suspend fun start() = withContext(coroutineDispatcher) {
        mutex.withLock {
            if (inProgress) return@withContext
            logd("TrackCaptureRepositoryImpl start")

            val geolocationUpdatesInterval =
                appSettingsRepository.appSettings.first().geolocationUpdatesInterval

            geolocationSubscription = launch {
                geolocationProvider.locationUpdates(geolocationUpdatesInterval)
                    .collect { result ->
                        logd(
                            "TrackCaptureRepositoryImpl locationCallback geolocation" +
                                    " ${result.geolocation} error ${result.error} nmea size ${result.nmea.size}"
                        )
                        val captureStatus = trackCaptureStatusRepository.status.first()

                        if (captureStatus is LocalTrackCaptureStatus.Enabled) {
                            handleGeolocationUpdate(captureStatus.trackInProgress, result)
                        }
                    }
            }
        }
    }

    override suspend fun stop() {
        mutex.withLock {
            logd("TrackCaptureRepositoryImpl stop")
            geolocationSubscription?.cancel()
            geolocationSubscription = null
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun handleGeolocationUpdate(
        trackInProgress: TrackInProgress,
        geolocationUpdateResult: GeolocationUpdateResult
    ) {
        if (trackInProgress.paused) return
        if (geolocationUpdateResult.isInvalidData()) return

        geolocationUpdateResult.geolocation?.let { geolocation ->
            val isAcceptableDistance =
                acceptableDistance(trackInProgress.lastLocation, geolocation, 2f)

            if (isAcceptableDistance) {
                tracksRepository.insertTrackPoint(geolocation = geolocation)

                val lastLocation = trackInProgress.lastLocation
                val newTrackInProgress = if (lastLocation != null) {
                    trackInProgress.copy(
                        distance = trackInProgress.distance + distanceTo(
                            lat1 = lastLocation.latitude,
                            lon1 = lastLocation.longitude,
                            lat2 = geolocation.latitude,
                            lon2 = geolocation.longitude,
                        ),
                        altitudeUp = if (lastLocation.altitude > geolocation.altitude) {
                            (lastLocation.altitude - geolocation.altitude).toInt()
                        } else {
                            trackInProgress.altitudeUp
                        },
                        altitudeDown = if (geolocation.altitude > lastLocation.altitude) {
                            (geolocation.altitude - lastLocation.altitude).toInt()
                        } else {
                            trackInProgress.altitudeDown
                        },
                        averageSpeed = (trackInProgress.averageSpeed + geolocation.speed)
                                / (trackInProgress.geolocationCount + 1),
                        maxSpeed = if (geolocation.speed > trackInProgress.maxSpeed) {
                            geolocation.speed
                        } else {
                            trackInProgress.maxSpeed
                        },
                        minSpeed = if (geolocation.speed < trackInProgress.minSpeed) {
                            geolocation.speed
                        } else {
                            trackInProgress.minSpeed
                        },
                        lastLocation = geolocation,
                        geolocationCount = trackInProgress.geolocationCount + 1,
                    )
                } else {
                    trackInProgress.copy(
                        lastLocation = geolocation
                    )
                }

                trackCaptureStatusRepository.update(
                    LocalTrackCaptureStatus.Enabled(
                        newTrackInProgress
                    )
                )
            } else {
                logw("not acceptable distance")
            }
        }
    }

    fun acceptableDistance(
        previous: Geolocation?,
        next: Geolocation,
        speedThreshold: Float
    ): Boolean {
        if (previous == null) return true

        val distance: Float = distanceTo(
            lat1 = next.latitude,
            lon1 = next.longitude,
            lat2 = previous.latitude,
            lon2 = previous.longitude,
        ).toFloat() // meters
        val time: Long = (next.time - previous.time) / 1000 // seconds
        val maxDistance = time * speedThreshold

        logd("distance $distance time $time maxDistance $maxDistance")

        return distance > 1 && distance < maxDistance
    }
}

private fun GeolocationUpdateResult.isInvalidData(): Boolean {
    val hasNoData = geolocation == null
    val hasStaleData = (System.currentTimeMillis() - (geolocation?.time ?: 0)) > 60 * 1000
    return hasNoData || hasStaleData
}

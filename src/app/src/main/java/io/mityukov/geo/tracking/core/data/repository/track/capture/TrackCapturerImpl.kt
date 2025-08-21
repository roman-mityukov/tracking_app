package io.mityukov.geo.tracking.core.data.repository.track.capture

import android.Manifest
import androidx.annotation.RequiresPermission
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationProvider
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationUpdateResult
import io.mityukov.geo.tracking.core.data.repository.settings.app.AppSettingsRepository
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.core.model.track.TrackPoint
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
import kotlin.uuid.ExperimentalUuidApi

class TrackCapturerImpl @Inject constructor(
    private val trackCaptureStatusProvider: TrackCaptureStatusProvider,
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
                        logd("TrackCaptureRepositoryImpl locationCallback geolocation ${result.geolocation} error ${result.error} nmea size ${result.nmea.size}")
                        val captureStatus = trackCaptureStatusProvider.status.first()

                        if (captureStatus is LocalTrackCaptureStatus.Enabled) {
                            handleGeolocationUpdate(captureStatus, result)
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
        captureStatus: LocalTrackCaptureStatus.Enabled,
        geolocationUpdateResult: GeolocationUpdateResult
    ) {
        if (captureStatus.paused) return
        if (geolocationUpdateResult.isInvalidData()) return

        geolocationUpdateResult.geolocation?.let { geolocation ->
            val currentTrack = tracksRepository.getTrack(captureStatus.trackId).first()
            val points = currentTrack.points

            val isAcceptableDistance = acceptableDistance(points, geolocation, 4f)

            if (isAcceptableDistance) {
                tracksRepository.insertTrackPoint(
                    trackId = currentTrack.id,
                    geolocation = geolocation
                )
            } else {
                logw("not acceptable distance")
            }
        }
    }

    fun acceptableDistance(
        points: List<TrackPoint>,
        next: Geolocation,
        speedThreshold: Float
    ): Boolean {
        if (points.isEmpty()) return true

        val previous = points.last().geolocation
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

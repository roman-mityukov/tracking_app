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
                        logd("TrackCaptureRepositoryImpl locationCallback $result")
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
        val currentTrackId = captureStatus.trackId
        val paused = captureStatus.paused
        val geolocation = geolocationUpdateResult.geolocation

        if (geolocation != null && !paused) {
            val diff = System.currentTimeMillis() - geolocation.time

            if (diff < 60 * 1000) {
                val currentTrack = tracksRepository.getTrack(currentTrackId).first()
                val points = currentTrack.points

                val canBeAdded = if (points.isNotEmpty()) {
                    val latestPoint = points.last()
                    val distance = geolocation.distanceTo(
                        Geolocation(
                            latestPoint.geolocation.latitude,
                            latestPoint.geolocation.longitude,
                            latestPoint.geolocation.altitude,
                            0L
                        )
                    )
                    logd("Can be added - distance $distance")
                    distance > 1
                } else {
                    true
                }

                if (canBeAdded) {
                    tracksRepository.insertTrackPoint(
                        trackId = currentTrackId,
                        geolocation = geolocation
                    )
                }
            }
        }
    }
}

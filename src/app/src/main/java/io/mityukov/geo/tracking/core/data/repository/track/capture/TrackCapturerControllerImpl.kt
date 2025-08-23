package io.mityukov.geo.tracking.core.data.repository.track.capture

import android.content.Context
import android.content.Intent
import android.location.Location
import dagger.hilt.android.qualifiers.ApplicationContext
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationProvider
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationUpdateResult
import io.mityukov.geo.tracking.core.data.repository.settings.app.AppSettingsRepository
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.di.DispatcherIO
import io.mityukov.geo.tracking.utils.PausableTimer
import io.mityukov.geo.tracking.utils.geolocation.distanceTo
import io.mityukov.geo.tracking.utils.log.logd
import io.mityukov.geo.tracking.utils.log.logw
import io.mityukov.geo.tracking.utils.permission.PermissionChecker
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi

class TrackCapturerControllerImpl @Inject constructor(
    @param:ApplicationContext private val applicationContext: Context,
    @param:DispatcherIO private val coroutineContext: CoroutineDispatcher,
    private val trackCaptureStatusRepository: TrackCaptureStatusRepository,
    private val tracksRepository: TracksRepository,
    private val geolocationProvider: GeolocationProvider,
    private val appSettingsRepository: AppSettingsRepository,
    private val permissionChecker: PermissionChecker,
) : TrackCapturerController {
    override val status: Flow<TrackCaptureStatus> = trackCaptureStatusRepository.status

    private val coroutineScope = CoroutineScope(coroutineContext)
    private var timerSubscriptionJob: Job? = null
    private var geolocationSubscription: Job? = null
    private val mutex = Mutex()
    private val timer = PausableTimer(coroutineScope = coroutineScope)

    @androidx.annotation.RequiresPermission(
        allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION]
    )
    override suspend fun bind() = withContext(coroutineContext) {
        mutex.withLock {
            val captureStatus = trackCaptureStatusRepository.status.first()
            if (captureStatus is TrackCaptureStatus.Run && geolocationSubscription == null) {
                launchTrackCapture()
            }
        }
    }

    @androidx.annotation.RequiresPermission(
        allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION]
    )
    @OptIn(ExperimentalUuidApi::class)
    override suspend fun start() = withContext(coroutineContext) {
        mutex.withLock {
            val captureStatus = trackCaptureStatusRepository.status.first()

            when (captureStatus) {
                is TrackCaptureStatus.Run, TrackCaptureStatus.Error -> {
                    throwInvalidCaptureStatusException()
                }

                TrackCaptureStatus.Idle -> {
                    launchTrackCapture()
                }
            }
        }
    }

    override suspend fun resume() = withContext(coroutineContext) {
        mutex.withLock {
            val captureStatus = trackCaptureStatusRepository.status.first()
            when (captureStatus) {
                TrackCaptureStatus.Error, TrackCaptureStatus.Idle -> {
                    throwInvalidCaptureStatusException()
                }

                is TrackCaptureStatus.Run -> {
                    timer.resume()
                    trackCaptureStatusRepository.update(
                        TrackCaptureStatus.Run(
                            captureStatus.trackInProgress.copy(
                                paused = false
                            )
                        )
                    )
                }
            }
        }
    }

    override suspend fun pause() = withContext(coroutineContext) {
        mutex.withLock {
            val captureStatus = trackCaptureStatusRepository.status.first()
            when (captureStatus) {
                TrackCaptureStatus.Error, TrackCaptureStatus.Idle -> {
                    throwInvalidCaptureStatusException()
                }

                is TrackCaptureStatus.Run -> {
                    timer.resume()
                    trackCaptureStatusRepository.update(
                        TrackCaptureStatus.Run(
                            captureStatus.trackInProgress.copy(
                                paused = true
                            )
                        )
                    )
                }
            }
        }
    }

    override suspend fun stop() = withContext(coroutineContext) {
        mutex.withLock {
            val captureStatus = trackCaptureStatusRepository.status.first()
            when (captureStatus) {
                TrackCaptureStatus.Idle, TrackCaptureStatus.Error -> {
                    throwInvalidCaptureStatusException()
                }

                is TrackCaptureStatus.Run -> {
                    timer.stop()
                    geolocationSubscription?.cancel()
                    geolocationSubscription = null
                    timerSubscriptionJob?.cancel()
                    timerSubscriptionJob = null
                    stopForegroundService()
                    tracksRepository.insertTrack(captureStatus.trackInProgress)
                    trackCaptureStatusRepository.update(TrackCaptureStatus.Idle)
                }
            }
        }
    }

    @androidx.annotation.RequiresPermission(
        allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION]
    )
    private suspend fun launchTrackCapture() {
        if (permissionChecker.locationGranted) {
            startForegroundService()
            timerSubscriptionJob = coroutineScope.launch {
                timer.events.collect {
                    val currentStatus = trackCaptureStatusRepository.status.first()
                    if (currentStatus is TrackCaptureStatus.Run) {
                        trackCaptureStatusRepository.update(
                            TrackCaptureStatus.Run(
                                trackInProgress = currentStatus.trackInProgress.copy(
                                    duration = (currentStatus.trackInProgress.duration.inWholeSeconds + 1).seconds
                                )
                            )
                        )
                    }
                }
            }

            val geolocationUpdatesInterval =
                appSettingsRepository.appSettings.first().geolocationUpdatesInterval

            geolocationSubscription = coroutineScope.launch {
                geolocationProvider.locationUpdates(geolocationUpdatesInterval)
                    .collect { result ->
                        logd(
                            "TrackCapturerControllerImpl locationCallback geolocation" +
                                    " ${result.geolocation} error ${result.error} nmea size ${result.nmea.size}"
                        )
                        val captureStatus = trackCaptureStatusRepository.status.first()

                        if (captureStatus is TrackCaptureStatus.Run) {
                            handleGeolocationUpdate(captureStatus.trackInProgress, result)
                        }
                    }
            }

            timer.start()

            trackCaptureStatusRepository.update(TrackCaptureStatus.Run(TrackInProgress.empty()))
        } else {
            trackCaptureStatusRepository.update(TrackCaptureStatus.Error)
        }
    }

    private fun startForegroundService() {
        val intent = Intent(applicationContext, ForegroundTrackCaptureService::class.java)
        applicationContext.startService(intent)
    }

    private fun stopForegroundService() {
        applicationContext.stopService(
            Intent(
                applicationContext,
                ForegroundTrackCaptureService::class.java
            )
        )
    }

    private fun throwInvalidCaptureStatusException(): Nothing {
        error("Invalid capture status")
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
                acceptableDistance(trackInProgress.lastLocation, geolocation)

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
                        altitudeUp = if (geolocation.altitude > lastLocation.altitude) {
                            trackInProgress.altitudeUp + (geolocation.altitude - lastLocation.altitude).toInt()
                        } else {
                            trackInProgress.altitudeUp
                        },
                        altitudeDown = if (geolocation.altitude < lastLocation.altitude) {
                            trackInProgress.altitudeDown + (lastLocation.altitude - geolocation.altitude).toInt()
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

                trackCaptureStatusRepository.update(TrackCaptureStatus.Run(newTrackInProgress))
            } else {
                logw("not acceptable distance")
            }
        }
    }

    fun acceptableDistance(
        previous: Geolocation?,
        next: Geolocation,
    ): Boolean {
        if (previous == null) return true

        val distance: Float = distanceTo(
            lat1 = next.latitude,
            lon1 = next.longitude,
            lat2 = previous.latitude,
            lon2 = previous.longitude,
        ).toFloat() // meters

        return distance > 10 && distance < 500
    }
}

private fun GeolocationUpdateResult.isInvalidData(): Boolean {
    val hasNoData = geolocation == null
    val hasStaleData = (System.currentTimeMillis() - (geolocation?.time ?: 0)) > 60 * 1000
    return hasNoData || hasStaleData
}

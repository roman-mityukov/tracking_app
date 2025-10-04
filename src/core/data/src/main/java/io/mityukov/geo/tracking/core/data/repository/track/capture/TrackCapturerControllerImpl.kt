@file:Suppress("CyclomaticComplexMethod")

package io.mityukov.geo.tracking.core.data.repository.track.capture

import android.content.Context
import android.content.Intent
import android.location.Location
import dagger.hilt.android.qualifiers.ApplicationContext
import io.mityukov.geo.tracking.core.common.di.DispatcherIO
import io.mityukov.geo.tracking.core.data.permission.PermissionChecker
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationProvider
import io.mityukov.geo.tracking.core.data.repository.geo.PlatformLocationUpdateResult
import io.mityukov.geo.tracking.core.data.repository.settings.app.AppSettingsRepository
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.core.model.track.distanceTo
import io.mityukov.geo.tracking.log.logd
import io.mityukov.geo.tracking.log.logw
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

internal class TrackCapturerControllerImpl @Inject constructor(
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
    private val timer = TrackTimer()

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
                    timer.pause()
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
                timer.events.collect { timerDuration ->
                    val currentStatus = trackCaptureStatusRepository.status.first()
                    if (currentStatus is TrackCaptureStatus.Run) {
                        trackCaptureStatusRepository.update(
                            TrackCaptureStatus.Run(
                                trackInProgress = currentStatus.trackInProgress.copy(
                                    duration = timerDuration.seconds
                                )
                            )
                        )
                    }
                }
            }

            val geolocationUpdatesInterval =
                appSettingsRepository.appSettings.first().geolocationUpdatesInterval
            val geolocationUpdatesMinDistance = 10f

            geolocationSubscription = coroutineScope.launch {
                geolocationProvider.locationUpdates(
                    geolocationUpdatesInterval,
                    geolocationUpdatesMinDistance,
                )
                    .collect { result ->
                        logd(
                            "TrackCapturerControllerImpl locationCallback geolocation" +
                                    " ${result.location} error ${result.error}"
                        )
                        val captureStatus = trackCaptureStatusRepository.status.first()

                        if (captureStatus is TrackCaptureStatus.Run) {
                            handleLocationUpdate(captureStatus.trackInProgress, result)
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
    private suspend fun handleLocationUpdate(
        trackInProgress: TrackInProgress,
        update: PlatformLocationUpdateResult
    ) {
        if (trackInProgress.paused) return

        update.location?.let { currentLocation ->
            val lastLocation = trackInProgress.lastLocation
            val isFirstPoint = lastLocation == null
            val distance =
                if (trackInProgress.lastLocation != null) trackInProgress.lastLocation.distanceTo(
                    currentLocation
                ) else 0f
            val isAcceptableDistance =
                if (lastLocation != null) distance < ((currentLocation.time - lastLocation.time) / 1000) * 15 else true
            val isAcceptableAccuracy =
                currentLocation.hasAccuracy() && currentLocation.accuracy < 50
            val isAcceptableTime = (System.currentTimeMillis() - currentLocation.time) < 60 * 1000
            val isAcceptable = isAcceptableDistance && isAcceptableAccuracy && isAcceptableTime

            if (isFirstPoint || isAcceptable) {
                tracksRepository.insertTrackPoint(currentLocation.toDomainGeolocation())
                val newTrackInProgress = if (lastLocation != null) {
                    trackInProgress.copy(
                        distance = trackInProgress.distance + distanceTo(
                            lat1 = lastLocation.latitude,
                            lon1 = lastLocation.longitude,
                            lat2 = currentLocation.latitude,
                            lon2 = currentLocation.longitude,
                        ),
                        altitudeUp = if (currentLocation.altitude > lastLocation.altitude) {
                            trackInProgress.altitudeUp + (currentLocation.altitude - lastLocation.altitude).toInt()
                        } else {
                            trackInProgress.altitudeUp
                        },
                        altitudeDown = if (currentLocation.altitude < lastLocation.altitude) {
                            trackInProgress.altitudeDown + (lastLocation.altitude - currentLocation.altitude).toInt()
                        } else {
                            trackInProgress.altitudeDown
                        },
                        sumSpeed = trackInProgress.sumSpeed + currentLocation.speed,
                        maxSpeed = if (currentLocation.speed > trackInProgress.maxSpeed) {
                            currentLocation.speed
                        } else {
                            trackInProgress.maxSpeed
                        },
                        minSpeed = if (currentLocation.speed < trackInProgress.minSpeed) {
                            currentLocation.speed
                        } else {
                            trackInProgress.minSpeed
                        },
                        lastLocation = currentLocation,
                        geolocationCount = trackInProgress.geolocationCount + 1,
                    )
                } else {
                    trackInProgress.copy(
                        sumSpeed = currentLocation.speed,
                        maxSpeed = currentLocation.speed,
                        minSpeed = currentLocation.speed,
                        lastLocation = currentLocation,
                        geolocationCount = 1,
                    )
                }
                trackCaptureStatusRepository.update(TrackCaptureStatus.Run(newTrackInProgress))
                logd("TrackCapturerControllerImpl accept location $newTrackInProgress")
            } else {
                logw(
                    "TrackCapturerControllerImpl don\'t accept location $currentLocation " +
                            " isAcceptableAccuracy $isAcceptableAccuracy ${currentLocation.accuracy}" +
                            " isAcceptableDistance $isAcceptableDistance $distance" +
                            " isAcceptableTime $isAcceptableTime system ${System.currentTimeMillis()} " +
                            "location ${currentLocation.time}"
                )
            }
        }
    }
}

private fun Location.toDomainGeolocation(): Geolocation {
    return Geolocation(
        latitude = latitude,
        longitude = longitude,
        altitude = altitude,
        speed = speed,
        time = time,
    )
}

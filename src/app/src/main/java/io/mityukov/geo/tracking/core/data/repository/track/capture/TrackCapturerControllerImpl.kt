package io.mityukov.geo.tracking.core.data.repository.track.capture

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.core.model.track.TrackActionType
import io.mityukov.geo.tracking.di.DispatcherIO
import io.mityukov.geo.tracking.utils.PausableTimer
import io.mityukov.geo.tracking.utils.log.logd
import io.mityukov.geo.tracking.utils.permission.PermissionChecker
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi

class TrackCapturerControllerImpl @Inject constructor(
    private val tracksRepository: TracksRepository,
    private val trackCaptureStatusRepository: TrackCaptureStatusRepository,
    @param:ApplicationContext private val applicationContext: Context,
    @param:DispatcherIO private val coroutineContext: CoroutineDispatcher,
    private val permissionChecker: PermissionChecker,
) : TrackCapturerController {
    private val mutableStateFlow = MutableStateFlow<TrackCaptureStatus>(TrackCaptureStatus.Idle)
    override val status: Flow<TrackCaptureStatus> = mutableStateFlow

    private val coroutineScope = CoroutineScope(coroutineContext)
    private var statusSubscriptionJob: Job? = null
    private var timerSubscriptionJob: Job? = null
    private val mutex = Mutex()
    private val timer = PausableTimer(coroutineScope = coroutineScope)

    override suspend fun bind() = withContext(coroutineContext) {
        mutex.withLock {
            val captureStatus = trackCaptureStatusRepository.status.first()
            if (captureStatus is LocalTrackCaptureStatus.Enabled && statusSubscriptionJob == null) {
                launchTrackCapture()
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun start() = withContext(coroutineContext) {
        mutex.withLock {
            val captureStatus = trackCaptureStatusRepository.status.first()

            when (captureStatus) {
                is LocalTrackCaptureStatus.Enabled -> {
                    throwInvalidCaptureStatusException()
                }

                LocalTrackCaptureStatus.Disabled -> {
                    tracksRepository.insertTrackAction(TrackActionType.Start)
                    trackCaptureStatusRepository.update(LocalTrackCaptureStatus.Enabled())
                    launchTrackCapture()
                }
            }
        }
    }

    override suspend fun resume() = withContext(coroutineContext) {
        mutex.withLock {
            val captureStatus = trackCaptureStatusRepository.status.first()

            when (captureStatus) {
                LocalTrackCaptureStatus.Disabled -> {
                    throwInvalidCaptureStatusException()
                }

                is LocalTrackCaptureStatus.Enabled -> {
                    timer.resume()
                    tracksRepository.insertTrackAction(TrackActionType.Resume)
                    trackCaptureStatusRepository.update(
                        LocalTrackCaptureStatus.Enabled(captureStatus.trackInProgress.copy(paused = false))
                    )
                }
            }
        }
    }

    override suspend fun pause() = withContext(coroutineContext) {
        mutex.withLock {
            val captureStatus = trackCaptureStatusRepository.status.first()

            when (captureStatus) {
                LocalTrackCaptureStatus.Disabled -> {
                    throwInvalidCaptureStatusException()
                }

                is LocalTrackCaptureStatus.Enabled -> {
                    timer.pause()
                    tracksRepository.insertTrackAction(TrackActionType.Pause)
                    trackCaptureStatusRepository.update(
                        LocalTrackCaptureStatus.Enabled(captureStatus.trackInProgress.copy(paused = true))
                    )
                }
            }
        }
    }

    override suspend fun stop() = withContext(coroutineContext) {
        mutex.withLock {
            val captureStatus = trackCaptureStatusRepository.status.first()

            when (captureStatus) {
                LocalTrackCaptureStatus.Disabled -> {
                    // no op
                }

                is LocalTrackCaptureStatus.Enabled -> {
                    timer.stop()
                    statusSubscriptionJob?.cancel()
                    statusSubscriptionJob = null
                    stopForegroundService()

                    tracksRepository.insertTrackAction(TrackActionType.Stop)
                    trackCaptureStatusRepository.update(LocalTrackCaptureStatus.Disabled)
                    mutableStateFlow.update {
                        TrackCaptureStatus.Idle
                    }
                }
            }
        }
    }

    private fun launchTrackCapture() {
        if (permissionChecker.locationGranted) {
            startForegroundService()
            statusSubscriptionJob = coroutineScope.launch {
                subscribe()
            }
            timerSubscriptionJob = coroutineScope.launch {
                timer.events.collect {
                    val currentStatus = trackCaptureStatusRepository.status.first()
                    if (currentStatus is LocalTrackCaptureStatus.Enabled) {
                        trackCaptureStatusRepository.update(
                            LocalTrackCaptureStatus.Enabled(
                                trackInProgress = currentStatus.trackInProgress.copy(
                                    duration = (currentStatus.trackInProgress.duration.inWholeSeconds + 1).seconds
                                )
                            )
                        )
                    }
                }
            }
            timer.start()
        } else {
            mutableStateFlow.update {
                TrackCaptureStatus.Error
            }
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

    private suspend fun subscribe() {
        logd("TrackCaptureService subscribePointsUpdate")

        trackCaptureStatusRepository.status
            .map { localStatus: LocalTrackCaptureStatus ->
                when (localStatus) {
                    LocalTrackCaptureStatus.Disabled -> {
                        TrackCaptureStatus.Idle
                    }

                    is LocalTrackCaptureStatus.Enabled -> {
                        TrackCaptureStatus.Run(trackInProgress = localStatus.trackInProgress)
                    }
                }
            }.collect { newStatus ->
                mutableStateFlow.update {
                    newStatus
                }
            }
    }

    private fun throwInvalidCaptureStatusException(): Nothing {
        error("Invalid capture status")
    }
}

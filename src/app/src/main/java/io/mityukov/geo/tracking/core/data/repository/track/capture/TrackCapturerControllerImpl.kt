package io.mityukov.geo.tracking.core.data.repository.track.capture

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.core.model.track.Track
import io.mityukov.geo.tracking.core.model.track.TrackActionType
import io.mityukov.geo.tracking.di.DispatcherIO
import io.mityukov.geo.tracking.utils.log.logd
import io.mityukov.geo.tracking.utils.permission.PermissionChecker
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
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
    private var subscriptionJob: Job? = null
    private val mutex = Mutex()

    override suspend fun bind() = withContext(coroutineContext) {
        mutex.withLock {
            val captureStatus = trackCaptureStatusRepository.status.first()
            if (captureStatus is LocalTrackCaptureStatus.Enabled && subscriptionJob == null) {
                launchTrackCapture(captureStatus.trackId)
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
                    val trackId = tracksRepository.insertTrack()
                    tracksRepository.insertTrackAction(trackId, TrackActionType.Start)

                    trackCaptureStatusRepository.update(
                        LocalTrackCaptureStatus.Enabled(trackId = trackId, paused = false)
                    )

                    launchTrackCapture(trackId)
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
                    tracksRepository.insertTrackAction(
                        captureStatus.trackId,
                        TrackActionType.Resume
                    )
                    trackCaptureStatusRepository.update(
                        LocalTrackCaptureStatus.Enabled(
                            paused = false,
                            trackId = captureStatus.trackId
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
                LocalTrackCaptureStatus.Disabled -> {
                    throwInvalidCaptureStatusException()
                }

                is LocalTrackCaptureStatus.Enabled -> {
                    tracksRepository.insertTrackAction(captureStatus.trackId, TrackActionType.Pause)
                    trackCaptureStatusRepository.update(
                        LocalTrackCaptureStatus.Enabled(
                            paused = true,
                            trackId = captureStatus.trackId
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
                LocalTrackCaptureStatus.Disabled -> {
                    // no op
                }

                is LocalTrackCaptureStatus.Enabled -> {
                    subscriptionJob?.cancel()
                    subscriptionJob = null
                    stopForegroundService()
                    val currentTrack = tracksRepository.getTrack(captureStatus.trackId).first()

                    tracksRepository.insertTrackAction(currentTrack.id, TrackActionType.Stop)
                    trackCaptureStatusRepository.update(LocalTrackCaptureStatus.Disabled)
                    mutableStateFlow.update {
                        TrackCaptureStatus.Idle
                    }
                }
            }
        }
    }

    private fun launchTrackCapture(trackId: String) {
        if (permissionChecker.locationGranted) {
            startForegroundService()
            subscriptionJob = coroutineScope.launch {
                subscribe(trackId)
            }
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

    private suspend fun subscribe(trackId: String) {
        logd("TrackCaptureService subscribePointsUpdate")

        tracksRepository.getTrack(trackId)
            .combine(trackCaptureStatusRepository.status) { track: Track, status: LocalTrackCaptureStatus ->
                when (status) {
                    LocalTrackCaptureStatus.Disabled -> {
                        TrackCaptureStatus.Idle
                    }

                    is LocalTrackCaptureStatus.Enabled -> {
                        TrackCaptureStatus.Run(
                            track = track,
                            paused = status.paused
                        )
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

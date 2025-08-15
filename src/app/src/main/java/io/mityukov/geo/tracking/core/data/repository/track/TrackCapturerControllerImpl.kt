package io.mityukov.geo.tracking.core.data.repository.track

import android.content.Context
import android.content.Intent
import androidx.datastore.core.DataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.mityukov.geo.tracking.core.data.repository.settings.app.proto.ProtoLocalTrackCaptureStatus
import io.mityukov.geo.tracking.core.database.dao.TrackDao
import io.mityukov.geo.tracking.core.database.model.TrackActionEntity
import io.mityukov.geo.tracking.core.database.model.TrackEntity
import io.mityukov.geo.tracking.core.database.model.TrackWithPoints
import io.mityukov.geo.tracking.core.model.track.TrackActionType
import io.mityukov.geo.tracking.di.DispatcherIO
import io.mityukov.geo.tracking.di.TrackCaptureStatusDataStore
import io.mityukov.geo.tracking.utils.log.logd
import io.mityukov.geo.tracking.utils.log.logw
import io.mityukov.geo.tracking.utils.permission.PermissionChecker
import io.mityukov.geo.tracking.utils.time.TimeUtils
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
import kotlin.uuid.Uuid

class TrackCapturerControllerImpl @Inject constructor(
    private val trackDao: TrackDao,
    private val trackMapper: TrackMapper,
    @param:ApplicationContext private val applicationContext: Context,
    @param:TrackCaptureStatusDataStore private val dataStore: DataStore<ProtoLocalTrackCaptureStatus>,
    @param:DispatcherIO private val coroutineContext: CoroutineDispatcher,
    private val permissionChecker: PermissionChecker,
) : TrackCapturerController {
    private val mutableStateFlow = MutableStateFlow<TrackCaptureStatus>(TrackCaptureStatus.Idle)
    override val status: Flow<TrackCaptureStatus> = mutableStateFlow

    private var currentTrack: TrackEntity? = null
    private val coroutineScope = CoroutineScope(coroutineContext)
    private var subscriptionJob: Job? = null
    private val mutex = Mutex()

    override suspend fun bind() = withContext(coroutineContext) {
        mutex.withLock {
            val currentTrackCaptureStatus = dataStore.data.first()
            if (currentTrackCaptureStatus.trackCaptureEnabled && subscriptionJob == null) {
                currentTrack = trackDao.getTrack(currentTrackCaptureStatus.trackId)

                launchTrackCapture()
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun start() = withContext(coroutineContext) {
        mutex.withLock {
            if (subscriptionJob != null) {
                logw("Stop current track capture before start")
                return@withContext
            }
            val startTime = TimeUtils.getCurrentUtcTime()
            val trackEntity =
                TrackEntity(
                    id = Uuid.random().toString(),
                    name = startTime,
                    start = startTime,
                    end = ""
                )
            trackDao.insertTrack(trackEntity)

            val trackAction = buildTrackActionEntity(trackEntity.id, TrackActionType.Start)
            trackDao.insertTrackAction(trackAction)

            val newTrackCaptureStatus = ProtoLocalTrackCaptureStatus
                .newBuilder()
                .setTrackId(trackEntity.id)
                .setTrackCaptureEnabled(true)
                .setPaused(false)
                .build()
            dataStore.updateData {
                newTrackCaptureStatus
            }

            currentTrack = trackEntity

            launchTrackCapture()
        }
    }

    override suspend fun resume() = withContext(coroutineContext) {
        mutex.withLock {
            val trackAction = buildTrackActionEntity(currentTrack!!.id, TrackActionType.Resume)
            trackDao.insertTrackAction(trackAction)

            val newTrackCaptureStatus = ProtoLocalTrackCaptureStatus
                .newBuilder()
                .setTrackId(currentTrack!!.id)
                .setTrackCaptureEnabled(true)
                .setPaused(false)
                .build()
            dataStore.updateData {
                newTrackCaptureStatus
            }
            Unit
        }
    }

    override suspend fun pause() = withContext(coroutineContext) {
        mutex.withLock {
            val trackAction = buildTrackActionEntity(currentTrack!!.id, TrackActionType.Pause)
            trackDao.insertTrackAction(trackAction)

            val newTrackCaptureStatus = ProtoLocalTrackCaptureStatus
                .newBuilder()
                .setTrackId(currentTrack!!.id)
                .setTrackCaptureEnabled(true)
                .setPaused(true)
                .build()
            dataStore.updateData {
                newTrackCaptureStatus
            }
            Unit
        }
    }

    override suspend fun stop() = withContext(coroutineContext) {
        mutex.withLock {
            subscriptionJob?.cancel()
            subscriptionJob = null
            val currentTrackCaptureStatus = dataStore.data.first()

            if (currentTrackCaptureStatus.trackCaptureEnabled) {
                stopForegroundService()
                dataStore.updateData {
                    ProtoLocalTrackCaptureStatus
                        .newBuilder()
                        .setTrackCaptureEnabled(false)
                        .setPaused(false)
                        .build()
                }

                currentTrack?.let {
                    val trackAction = buildTrackActionEntity(it.id, TrackActionType.Stop)
                    trackDao.insertTrackAction(trackAction)

                    val track =
                        TrackEntity(
                            id = it.id,
                            name = it.name,
                            start = it.start,
                            end = TimeUtils.getCurrentUtcTime()
                        )
                    trackDao.insertTrack(track)
                }

                currentTrack = null
                mutableStateFlow.update {
                    TrackCaptureStatus.Idle
                }
            }
        }
    }

    private fun launchTrackCapture() {
        if (permissionChecker.locationGranted) {
            startForegroundService()
            subscriptionJob = coroutineScope.launch {
                subscribe()
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

    private suspend fun subscribe() {
        logd("TrackCaptureService subscribePointsUpdate")

        trackDao.getTrackWithPoints(currentTrack!!.id)
            .combine(dataStore.data) { trackWithPoints: TrackWithPoints, data: ProtoLocalTrackCaptureStatus ->
                if (currentTrack != null) {
                    TrackCaptureStatus.Run(
                        track = trackMapper.trackWithPointsEntityToDomain(trackWithPoints),
                        paused = data.paused
                    )
                } else {
                    TrackCaptureStatus.Idle
                }
            }.collect { newStatus ->
                mutableStateFlow.update {
                    newStatus
                }
            }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun buildTrackActionEntity(
        trackId: String,
        action: TrackActionType
    ): TrackActionEntity {
        return TrackActionEntity(
            id = Uuid.random().toString(),
            trackId = trackId,
            timestamp = TimeUtils.getCurrentUtcTime(),
            action = action.toString(),
        )
    }
}

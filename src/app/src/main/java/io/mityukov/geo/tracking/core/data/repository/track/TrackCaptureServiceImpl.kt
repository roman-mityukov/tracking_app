package io.mityukov.geo.tracking.core.data.repository.track

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.mityukov.geo.tracking.ForegroundGeolocationService
import io.mityukov.geo.tracking.app.DeepLinkProps
import io.mityukov.geo.tracking.app.GeoAppProps
import io.mityukov.geo.tracking.core.data.repository.settings.app.proto.ProtoLocalTrackCaptureStatus
import io.mityukov.geo.tracking.core.database.dao.TrackDao
import io.mityukov.geo.tracking.core.database.model.TrackEntity
import io.mityukov.geo.tracking.core.database.model.TrackWithPoints
import io.mityukov.geo.tracking.di.DispatcherIO
import io.mityukov.geo.tracking.di.TrackCaptureStatusDataStore
import io.mityukov.geo.tracking.utils.PausableTimer
import io.mityukov.geo.tracking.utils.log.logd
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TrackCaptureServiceImpl @Inject constructor(
    private val trackDao: TrackDao,
    private val trackMapper: TrackMapper,
    @ApplicationContext private val applicationContext: Context,
    @TrackCaptureStatusDataStore private val dataStore: DataStore<ProtoLocalTrackCaptureStatus>,
    @DispatcherIO private val coroutineContext: CoroutineDispatcher,
) : TrackCaptureService {
    private val mutableStateFlow = MutableStateFlow<TrackCaptureStatus>(TrackCaptureStatus.Idle)
    override val status: Flow<TrackCaptureStatus> = mutableStateFlow

    private var currentTrack: TrackEntity? = null
    private val coroutineScope = CoroutineScope(coroutineContext)
    private var subscriptionJob: Job? = null
    private var timer: PausableTimer? = null

    override suspend fun bind() = withContext(coroutineContext) {
        val currentTrackCaptureStatus = dataStore.data.first()
        if (currentTrackCaptureStatus.trackCaptureEnabled) {
            currentTrack = trackDao.getTrack(currentTrackCaptureStatus.trackId)
            startForegroundService(currentTrackCaptureStatus.trackId)

            timer?.stop()
            timer = PausableTimer(
                initialValue = currentTrack!!.duration,
                coroutineScope = coroutineScope
            )

            if (subscriptionJob == null) {
                subscriptionJob = coroutineScope.launch {
                    subscribe()
                }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun start() = withContext(coroutineContext) {
        subscriptionJob = coroutineScope.launch {
            val trackId = Uuid.random().toString()
            currentTrack = TrackEntity(id = trackId, name = "Random name", duration = 0)
            trackDao.insertTrack(currentTrack!!)

            val newTrackCaptureStatus = ProtoLocalTrackCaptureStatus
                .newBuilder()
                .setTrackId(currentTrack!!.id)
                .setTrackCaptureEnabled(true)
                .setPaused(false)
                .build()
            dataStore.updateData {
                newTrackCaptureStatus
            }

            startForegroundService(trackId)

//            val workRequest = PeriodicWorkRequestBuilder<BackgroundGeolocationWorker>(
//                Duration.ofMinutes(
//                    GeoAppProps.TRACK_CAPTURE_INTERVAL_MINUTES
//                )
//            ).build()
//            val workManager = WorkManager.getInstance(applicationContext)
//            workManager.enqueueUniquePeriodicWork(
//                GeoAppProps.TRACK_CAPTURE_WORK_NAME,
//                ExistingPeriodicWorkPolicy.KEEP, workRequest
//            )
            timer?.stop()
            timer = PausableTimer(
                coroutineScope = coroutineScope
            )

            subscribe()
        }
    }

    override suspend fun resume() {
        timer?.resume()
        val newTrackCaptureStatus = ProtoLocalTrackCaptureStatus
            .newBuilder()
            .setTrackId(currentTrack!!.id)
            .setTrackCaptureEnabled(true)
            .setPaused(false)
            .build()
        dataStore.updateData {
            newTrackCaptureStatus
        }
    }

    override suspend fun pause() {
        timer?.pause()
        val newTrackCaptureStatus = ProtoLocalTrackCaptureStatus
            .newBuilder()
            .setTrackId(currentTrack!!.id)
            .setTrackCaptureEnabled(true)
            .setPaused(true)
            .build()
        dataStore.updateData {
            newTrackCaptureStatus
        }
    }

    override suspend fun stop() {
        timer?.stop()
        subscriptionJob?.cancel()
        val currentTrackCaptureStatus = dataStore.data.first()

        if (currentTrackCaptureStatus.trackCaptureEnabled) {
            applicationContext.stopService(
                Intent(
                    applicationContext,
                    ForegroundGeolocationService::class.java
                )
            )
            dataStore.updateData {
                ProtoLocalTrackCaptureStatus
                    .newBuilder()
                    .setTrackCaptureEnabled(false)
                    .setPaused(false)
                    .build()
            }

            currentTrack = null
            mutableStateFlow.update {
                TrackCaptureStatus.Idle
            }

            val workManager = WorkManager.getInstance(applicationContext)
            workManager.cancelUniqueWork(GeoAppProps.TRACK_CAPTURE_WORK_NAME)
        }
    }

    private fun startForegroundService(trackId: String) {
        val intent = Intent(applicationContext, ForegroundGeolocationService::class.java)
        intent.data = DeepLinkProps.TRACK_DETAILS_URI_PATTERN.replace(
            "{${DeepLinkProps.TRACK_DETAILS_PATH}}",
            trackId
        ).toUri()
        applicationContext.startService(intent)
    }

    private suspend fun subscribe() {
        logd("TrackCaptureService subscribePointsUpdate")
        timer?.start()

        coroutineScope.launch {
            timer?.events?.collect {
                logd("timer")
                if (currentTrack != null) {
                    currentTrack = trackDao.getTrack(currentTrack!!.id)
                    val track = currentTrack
                    if (track != null) {
                        val newTrack = TrackEntity(track.id, track.name, track.duration + 1000)
                        trackDao.insertTrack(newTrack)
                    }
                }

            }
        }

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
}

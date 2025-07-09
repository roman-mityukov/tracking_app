package io.mityukov.geo.tracking.core.data.repository.track

import android.content.Context
import android.content.Intent
import androidx.datastore.core.DataStore
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.mityukov.geo.tracking.BackgroundGeolocationWorker
import io.mityukov.geo.tracking.ForegroundGeolocationService
import io.mityukov.geo.tracking.app.GeoAppProperties
import io.mityukov.geo.tracking.core.data.repository.settings.app.proto.ProtoLocalTrackCaptureStatus
import io.mityukov.geo.tracking.core.database.dao.TrackDao
import io.mityukov.geo.tracking.core.database.model.TrackEntity
import io.mityukov.geo.tracking.di.DispatcherIO
import io.mityukov.geo.tracking.di.TrackCaptureStatusDataStore
import io.mityukov.geo.tracking.utils.log.logd
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
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
    private var job: Job? = null

    override suspend fun bind() = withContext(coroutineContext) {
        val currentTrackCaptureStatus = dataStore.data.first()
        if (currentTrackCaptureStatus.trackCaptureEnabled) {
            currentTrack = trackDao.getTrack(currentTrackCaptureStatus.trackId)
            val intent = Intent(applicationContext, ForegroundGeolocationService::class.java)
            applicationContext.startService(intent)

            trackDao.getTrackPoints(currentTrack!!.id).collect { points ->
                mutableStateFlow.update {
                    TrackCaptureStatus.Running(
                        trackMapper.trackWithPointsEntityToDomain(
                            trackDao.getTrackWithPoints(
                                currentTrack!!.id
                            ).first()
                        )
                    )
                }
            }
        }
    }

    override suspend fun switch() {
        val currentStatus = status.first()
        when (currentStatus) {
            TrackCaptureStatus.Idle -> {
                start()
            }

            else -> {
                stop()
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun start() = withContext(coroutineContext) {
        job = coroutineScope.launch {
            currentTrack = TrackEntity(id = Uuid.random().toString(), name = "Random name")
            trackDao.insertTrack(currentTrack!!)

            val newTrackCaptureStatus = ProtoLocalTrackCaptureStatus
                .newBuilder()
                .setTrackId(currentTrack!!.id)
                .setTrackCaptureEnabled(true)
                .build()
            dataStore.updateData {
                newTrackCaptureStatus
            }

            val intent = Intent(applicationContext, ForegroundGeolocationService::class.java)
            applicationContext.startService(intent)

            val workRequest = PeriodicWorkRequestBuilder<BackgroundGeolocationWorker>(
                Duration.ofMinutes(
                    GeoAppProperties.TRACK_CAPTURE_INTERVAL_MINUTES
                )
            ).build()
            val workManager = WorkManager.getInstance(applicationContext)
            workManager.enqueueUniquePeriodicWork(
                GeoAppProperties.TRACK_CAPTURE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, workRequest
            )

            trackDao.getTrackPoints(currentTrack!!.id).collect { points ->
                mutableStateFlow.update {
                    logd("TrackCaptureService points update received")
                    TrackCaptureStatus.Running(
                        trackMapper.trackWithPointsEntityToDomain(
                            trackDao.getTrackWithPoints(
                                currentTrack!!.id
                            ).first()
                        )
                    )
                }
            }
        }
    }

    override suspend fun stop() {
        job?.cancel()
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
                    .build()
            }

            currentTrack = null
            mutableStateFlow.update {
                TrackCaptureStatus.Idle
            }

            val workManager = WorkManager.getInstance(applicationContext)
            workManager.cancelUniqueWork(GeoAppProperties.TRACK_CAPTURE_WORK_NAME)
        }
    }
}
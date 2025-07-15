package io.mityukov.geo.tracking.core.data.repository.track

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.mityukov.geo.tracking.BackgroundGeolocationWorker
import io.mityukov.geo.tracking.ForegroundGeolocationService
import io.mityukov.geo.tracking.app.DeepLinkProps
import io.mityukov.geo.tracking.app.GeoAppProps
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
    private var subscriptionJob: Job? = null

    override suspend fun bind() = withContext(coroutineContext) {
        val currentTrackCaptureStatus = dataStore.data.first()
        if (currentTrackCaptureStatus.trackCaptureEnabled) {
            currentTrack = trackDao.getTrack(currentTrackCaptureStatus.trackId)
            startForegroundService(currentTrackCaptureStatus.trackId)

            if (subscriptionJob == null) {
                subscriptionJob = coroutineScope.launch {
                    subscribePointsUpdate()
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
        subscriptionJob = coroutineScope.launch {
            val trackId = Uuid.random().toString()
            currentTrack = TrackEntity(id = trackId, name = "Random name")
            trackDao.insertTrack(currentTrack!!)

            val newTrackCaptureStatus = ProtoLocalTrackCaptureStatus
                .newBuilder()
                .setTrackId(currentTrack!!.id)
                .setTrackCaptureEnabled(true)
                .build()
            dataStore.updateData {
                newTrackCaptureStatus
            }

            startForegroundService(trackId)

            val workRequest = PeriodicWorkRequestBuilder<BackgroundGeolocationWorker>(
                Duration.ofMinutes(
                    GeoAppProps.TRACK_CAPTURE_INTERVAL_MINUTES
                )
            ).build()
            val workManager = WorkManager.getInstance(applicationContext)
            workManager.enqueueUniquePeriodicWork(
                GeoAppProps.TRACK_CAPTURE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, workRequest
            )

            subscribePointsUpdate()
        }
    }

    override suspend fun stop() {
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
        intent.setData(
            DeepLinkProps.TRACK_DETAILS_URI_PATTERN.replace(
                "{${DeepLinkProps.TRACK_DETAILS_PATH}}",
                trackId
            ).toUri()
        )
        applicationContext.startService(intent)
    }

    private suspend fun subscribePointsUpdate() {
        trackDao.getTrackPoints(currentTrack!!.id).collect { points ->
            if (currentTrack != null) {
                logd("TrackCaptureService subscribePointsUpdate")
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
}
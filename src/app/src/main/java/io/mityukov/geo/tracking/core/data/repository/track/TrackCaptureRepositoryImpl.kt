package io.mityukov.geo.tracking.core.data.repository.track

import androidx.datastore.core.DataStore
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationProvider
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationUpdateResult
import io.mityukov.geo.tracking.core.data.repository.settings.app.LocalAppSettingsRepository
import io.mityukov.geo.tracking.core.data.repository.settings.app.proto.ProtoLocalTrackCaptureStatus
import io.mityukov.geo.tracking.core.database.dao.TrackDao
import io.mityukov.geo.tracking.core.database.model.TrackPointEntity
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.di.DispatcherIO
import io.mityukov.geo.tracking.di.TrackCaptureStatusDataStore
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
import kotlin.uuid.Uuid

class TrackCaptureRepositoryImpl @Inject constructor(
    @TrackCaptureStatusDataStore private val dataStore: DataStore<ProtoLocalTrackCaptureStatus>,
    private val trackDao: TrackDao,
    private val geolocationProvider: GeolocationProvider,
    private val localAppSettingsRepository: LocalAppSettingsRepository,
    @DispatcherIO private val coroutineDispatcher: CoroutineDispatcher,
) : TrackCaptureRepository {
    private val mutex = Mutex()
    private var geolocationSubscription: Job? = null
    private val initialized: Boolean
        get() = geolocationSubscription?.isActive ?: false

    override suspend fun start() = withContext(coroutineDispatcher) {
        mutex.withLock {
            if (initialized) return@withContext
            logd("TrackCaptureRepositoryImpl start")

            val localAppSettings = localAppSettingsRepository.localAppSettings.first()

            geolocationSubscription = launch {
                geolocationProvider.locationUpdates(localAppSettings.geolocationUpdatesInterval)
                    .collect { result ->
                        logd("TrackCaptureRepositoryImpl locationCallback $result")
                        handleGeolocationUpdate(result)
                    }
            }
        }
    }

    override suspend fun stop() = withContext(coroutineDispatcher) {
        mutex.withLock {
            logd("TrackCaptureRepositoryImpl stop")
            geolocationSubscription?.cancel()
            geolocationSubscription = null
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun handleGeolocationUpdate(geolocationUpdateResult: GeolocationUpdateResult) {
        val trackCaptureStatus = dataStore.data.first()
        val currentTrackId = trackCaptureStatus.trackId
        val paused = trackCaptureStatus.paused
        val geolocation = geolocationUpdateResult.geolocation

        if (currentTrackId != null && geolocation != null && !paused) {
            val diff = System.currentTimeMillis() - geolocation.time

            if (diff < 60 * 1000) {
                val points =
                    trackDao.getTrackPoints(currentTrackId).first()

                val canBeAdded = if (points.isNotEmpty()) {
                    val latestPoint = points.last()
                    val distance = geolocation.distanceTo(
                        Geolocation(
                            latestPoint.latitude,
                            latestPoint.longitude,
                            latestPoint.altitude,
                            0L
                        )
                    )
                    logd("Can be added - distance $distance")
                    distance > 1
                } else {
                    true
                }

                if (canBeAdded) {
                    trackDao.insertTrackPoint(
                        TrackPointEntity(
                            id = Uuid.Companion.random().toString(),
                            trackId = currentTrackId,
                            latitude = geolocation.latitude,
                            longitude = geolocation.longitude,
                            altitude = geolocation.altitude,
                            time = geolocation.time,
                        )
                    )
                }
            }
        }
    }
}

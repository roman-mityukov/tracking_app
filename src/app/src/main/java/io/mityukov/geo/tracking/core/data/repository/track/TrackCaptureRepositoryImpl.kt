package io.mityukov.geo.tracking.core.data.repository.track

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.datastore.core.DataStore
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import io.mityukov.geo.tracking.core.data.repository.settings.app.LocalAppSettingsRepository
import io.mityukov.geo.tracking.core.data.repository.settings.app.proto.ProtoLocalTrackCaptureStatus
import io.mityukov.geo.tracking.core.database.dao.TrackDao
import io.mityukov.geo.tracking.core.database.model.TrackEntity
import io.mityukov.geo.tracking.core.database.model.TrackPointEntity
import io.mityukov.geo.tracking.di.DispatcherIO
import io.mityukov.geo.tracking.di.TrackCaptureStatusDataStore
import io.mityukov.geo.tracking.utils.PausableTimer
import io.mityukov.geo.tracking.utils.log.logd
import io.mityukov.geo.tracking.utils.permission.PermissionChecker
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TrackCaptureRepositoryImpl @Inject constructor(
    private val permissionChecker: PermissionChecker,
    @TrackCaptureStatusDataStore private val dataStore: DataStore<ProtoLocalTrackCaptureStatus>,
    private val trackDao: TrackDao,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val localAppSettingsRepository: LocalAppSettingsRepository,
    @DispatcherIO private val coroutineDispatcher: CoroutineDispatcher,
) : TrackCaptureRepository {
    private var initialized: Boolean = false
    private val coroutineScope by lazy {
        CoroutineScope(coroutineDispatcher)
    }
    private val mutex = Mutex()
    private var timer: PausableTimer? = null
    private var timerSubscriptionJob: Job? = null

    @OptIn(ExperimentalUuidApi::class)
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            coroutineScope.launch {
                logd("TrackCaptureRepositoryImpl locationCallback $locationResult")
                val trackCaptureStatus = dataStore.data.first()
                val currentTrackId = trackCaptureStatus.trackId
                val paused = trackCaptureStatus.paused
                val lastLocation = locationResult.lastLocation

                if (currentTrackId != null) {
                    if (lastLocation != null && !paused) {
                        val diff = System.currentTimeMillis() - lastLocation.time

                        if (diff > 60 * 1000) {
                            logd("Skip old location")
                            return@launch
                        }

                        val points = trackDao.getTrackPoints(currentTrackId).first()

                        val canBeAdded = if (points.isNotEmpty()) {
                            val latestPoint = points.last()
                            val results = floatArrayOf(0f, 0f, 0f)
                            Location.distanceBetween(
                                lastLocation.latitude,
                                lastLocation.longitude,
                                latestPoint.latitude,
                                latestPoint.longitude,
                                results
                            )
                            logd("Can be added ${results[0]}")
                            results[0] > 1
                        } else {
                            true
                        }

                        if (canBeAdded) {
                            trackDao.insertTrackPoint(
                                TrackPointEntity(
                                    id = Uuid.Companion.random().toString(),
                                    trackId = currentTrackId,
                                    latitude = lastLocation.latitude,
                                    longitude = lastLocation.longitude,
                                    altitude = lastLocation.altitude,
                                    time = lastLocation.time,
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun start() {
        coroutineScope.launch {
            mutex.withLock {
                if (initialized) return@launch

                if (permissionChecker.locationGranted) {
                    val localAppSettings = localAppSettingsRepository.localAppSettings.first()

                    fusedLocationClient.requestLocationUpdates(
                        LocationRequest.Builder(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            localAppSettings.geolocationUpdatesInterval.inWholeMilliseconds
                        )
                            .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                            .build(),
                        locationCallback,
                        Looper.getMainLooper(),
                    )

                    initialized = true

                    val currentTrackId = dataStore.data.first().trackId
                    timer = PausableTimer(
                        coroutineScope = coroutineScope
                    )
                    timer?.start()
                    timerSubscriptionJob = coroutineScope.launch {
                        timer?.events?.collect {
                            val trackCaptureStatus = dataStore.data.first()
                            if (trackCaptureStatus.paused.not()) {
                                val oldTrack = trackDao.getTrack(currentTrackId)
                                val newTrack =
                                    TrackEntity(
                                        oldTrack.id,
                                        oldTrack.name,
                                        oldTrack.duration + timer!!.interval
                                    )
                                trackDao.insertTrack(newTrack)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun stop() {
        coroutineScope.launch {
            mutex.withLock {
                fusedLocationClient.removeLocationUpdates(locationCallback)
                timer?.stop()
                timerSubscriptionJob?.cancel()
                initialized = false
            }
        }
    }
}

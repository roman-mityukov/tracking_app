package io.mityukov.geo.tracking

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.datastore.core.DataStore
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import io.mityukov.geo.tracking.app.AppProps
import io.mityukov.geo.tracking.core.data.repository.settings.app.LocalAppSettingsRepository
import io.mityukov.geo.tracking.core.data.repository.settings.app.proto.ProtoLocalTrackCaptureStatus
import io.mityukov.geo.tracking.core.database.dao.TrackDao
import io.mityukov.geo.tracking.core.database.model.TrackEntity
import io.mityukov.geo.tracking.core.database.model.TrackPointEntity
import io.mityukov.geo.tracking.di.DispatcherIO
import io.mityukov.geo.tracking.di.TrackCaptureStatusDataStore
import io.mityukov.geo.tracking.utils.PausableTimer
import io.mityukov.geo.tracking.utils.log.logd
import io.mityukov.geo.tracking.utils.log.logw
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@AndroidEntryPoint
class ForegroundTrackCaptureService : Service() {
    @Inject
    @TrackCaptureStatusDataStore
    lateinit var dataStore: DataStore<ProtoLocalTrackCaptureStatus>

    @Inject
    lateinit var trackDao: TrackDao

    @Inject
    lateinit var fusedLocationClient: FusedLocationProviderClient

    @Inject
    lateinit var localAppSettingsRepository: LocalAppSettingsRepository

    @Inject
    @DispatcherIO
    lateinit var coroutineDispatcher: CoroutineDispatcher
    private val coroutineScope by lazy {
        CoroutineScope(coroutineDispatcher)
    }

    private var timer: PausableTimer? = null
    private var initialized: Boolean = false

    @OptIn(ExperimentalUuidApi::class)
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            coroutineScope.launch {
                val currentTrackId = dataStore.data.first().trackId
                val paused = dataStore.data.first().paused
                val lastLocation = locationResult.lastLocation

                if (currentTrackId != null) {
                    if (lastLocation != null && !paused) {
                        val diff = System.currentTimeMillis() - lastLocation.time

                        logd("ForegroundGeolocationService lastLocation ${locationResult.lastLocation} diff $diff")

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
                            logd("Add point $lastLocation")
                            trackDao.insertTrackPoint(
                                TrackPointEntity(
                                    id = Uuid.random().toString(),
                                    trackId = currentTrackId,
                                    latitude = lastLocation.latitude,
                                    longitude = lastLocation.longitude,
                                    altitude = lastLocation.altitude,
                                    time = lastLocation.time,
                                )
                            )
                        }
                    }

                } else {
                    stopSelf()
                    ServiceCompat.stopForeground(
                        this@ForegroundTrackCaptureService,
                        ServiceCompat.STOP_FOREGROUND_REMOVE
                    )
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val pause: Boolean? = intent?.extras?.getBoolean(AppProps.EXTRA_INTENT_PAUSE)
        if (pause != null) {
            logd("ForegroundTrackCaptureService receives pause intent")
            if (pause) {
                timer?.pause()
            } else {
                timer?.resume()
            }
        } else if (initialized.not()) {
            logd("ForegroundTrackCaptureService receives startForeground intent")
            startForeground(intent)
            initialized = true
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun startForeground(intent: Intent?) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            logw("ForegroundGeolocationService no permissions - stopSelf")
            stopSelf()
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            return
        } else {
            ServiceCompat.startForeground(
                this,
                100,
                buildNotification(intent),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                } else {
                    0
                },
            )

            coroutineScope.launch {
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

                val currentTrackId = dataStore.data.first().trackId
                val track = trackDao.getTrack(currentTrackId)
                timer = PausableTimer(
                    initialValue = track.duration,
                    coroutineScope = coroutineScope
                )
                timer?.start()
                timer?.events?.collect {
                    val oldTrack = trackDao.getTrack(currentTrackId)
                    logd("timer oldDuration ${oldTrack.duration}")
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

    private fun buildNotification(intent: Intent?): Notification {
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(
                applicationContext,
                AppProps.TRACK_CAPTURE_CHANNEL_ID
            )

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            data = intent?.data
        }
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val notification = builder
            .setContentTitle(resources.getString(R.string.track_capture_notification_title))
            .setContentText(resources.getString(R.string.track_capture_notification_text))
            .setContentIntent(pendingIntent)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setOngoing(true)
            .setSilent(true)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .build()
        return notification
    }
}

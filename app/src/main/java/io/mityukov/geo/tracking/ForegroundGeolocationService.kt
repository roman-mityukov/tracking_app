package io.mityukov.geo.tracking

import android.Manifest
import android.app.ForegroundServiceStartNotAllowedException
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.datastore.core.DataStore
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import dagger.hilt.android.AndroidEntryPoint
import io.mityukov.geo.tracking.app.GeoAppProperties
import io.mityukov.geo.tracking.core.data.repository.settings.app.proto.ProtoLocalTrackCaptureStatus
import io.mityukov.geo.tracking.core.database.dao.TrackDao
import io.mityukov.geo.tracking.core.database.model.TrackPointEntity
import io.mityukov.geo.tracking.di.DispatcherIO
import io.mityukov.geo.tracking.di.TrackCaptureStatusDataStore
import io.mityukov.geo.tracking.utils.log.logd
import io.mityukov.geo.tracking.utils.log.logw
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@AndroidEntryPoint
class ForegroundGeolocationService : Service() {
    @Inject
    @TrackCaptureStatusDataStore
    lateinit var dataStore: DataStore<ProtoLocalTrackCaptureStatus>

    @Inject
    lateinit var trackDao: TrackDao

    @Inject
    lateinit var fusedLocationClient: FusedLocationProviderClient

    @Inject
    lateinit var locationRequest: LocationRequest

    @Inject
    @DispatcherIO
    lateinit var coroutineDispatcher: CoroutineDispatcher

    @OptIn(ExperimentalUuidApi::class)
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            logd("ForegroundGeolocationService locationResult.lastLocation ${locationResult.lastLocation} thread ${Thread.currentThread().name}")
            runBlocking(coroutineDispatcher) {
                val currentTrackId = dataStore.data.first().trackId
                val lastLocation = locationResult.lastLocation

                if (currentTrackId != null) {
                    if (lastLocation != null) {
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

                } else {
                    stopSelf()
                    ServiceCompat.stopForeground(
                        this@ForegroundGeolocationService,
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
        startForeground()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()

        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun startForeground() {
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
            try {
                val builder: NotificationCompat.Builder =
                    NotificationCompat.Builder(
                        applicationContext,
                        GeoAppProperties.TRACK_CAPTURE_CHANNEL_ID
                    )

                val notification = builder
                    .setContentTitle(resources.getString(R.string.track_capture_notification_title))
                    .setContentText(resources.getString(R.string.track_capture_notification_text))
                    .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                    .setOngoing(true)
                    .setSilent(true)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .build()

                ServiceCompat.startForeground(
                    this,
                    100,
                    notification,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                    } else {
                        0
                    },
                )
            } catch (e: Exception) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    && e is ForegroundServiceStartNotAllowedException
                ) {
                    // App not in a valid state to start foreground service
                    // (e.g. started from bg)
                    logw("ForegroundGeolocationService $e")
                } else {
                    throw e
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper(),
            )
        }
    }
}
package io.mityukov.geo.tracking.core.data.repository.track

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.mityukov.geo.tracking.MainActivity
import io.mityukov.geo.tracking.R
import io.mityukov.geo.tracking.app.AppProps
import io.mityukov.geo.tracking.app.DeepLinkProps
import io.mityukov.geo.tracking.core.data.repository.settings.app.proto.ProtoLocalTrackCaptureStatus
import io.mityukov.geo.tracking.di.DispatcherIO
import io.mityukov.geo.tracking.di.TrackCaptureStatusDataStore
import io.mityukov.geo.tracking.utils.log.logw
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi

@AndroidEntryPoint
class ForegroundTrackCaptureService : LifecycleService() {
    @Inject
    lateinit var trackCapturer: TrackCapturer

    @Inject
    @TrackCaptureStatusDataStore
    lateinit var dataStore: DataStore<ProtoLocalTrackCaptureStatus>

    @Inject
    @DispatcherIO
    lateinit var coroutineDispatcher: CoroutineDispatcher

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        lifecycleScope.launch {
            trackCapturer.stop()
        }
        super.onDestroy()
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun startForeground() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            logw("ForegroundGeolocationService no permissions - stopSelf")
            stopSelf()
            return
        } else {
            lifecycleScope.launch {
                val currentTrackCaptureStatus = dataStore.data.first()
                if (currentTrackCaptureStatus.trackId == null) {
                    stopSelf()
                } else {
                    ServiceCompat.startForeground(
                        this@ForegroundTrackCaptureService,
                        AppProps.TRACK_CAPTURE_NOTIFICATION_ID,
                        buildNotification(currentTrackCaptureStatus.trackId),
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                        } else {
                            0
                        },
                    )
                    trackCapturer.start()
                }
            }
        }
    }

    private fun buildNotification(trackId: String): Notification {
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(
                applicationContext,
                AppProps.TRACK_CAPTURE_CHANNEL_ID
            )

        val activityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            data = DeepLinkProps.TRACK_DETAILS_URI_PATTERN.replace(
                "{${DeepLinkProps.TRACK_DETAILS_PATH}}",
                trackId
            ).toUri()
        }
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                activityIntent,
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

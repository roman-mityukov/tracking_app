package io.rm.test.geo

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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import dagger.hilt.android.AndroidEntryPoint
import io.rm.test.geo.utils.log.logd
import io.rm.test.geo.utils.log.logw
import javax.inject.Inject

@AndroidEntryPoint
class ForegroundGeolocationService : Service() {
    @Inject
    lateinit var fusedLocationClient: FusedLocationProviderClient
    @Inject
    lateinit var locationRequest: LocationRequest

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground()

        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForeground() {
        try {
            val builder: NotificationCompat.Builder =
                NotificationCompat.Builder(
                    applicationContext,
                    "geolocationChannelId"
                )

            val notification = builder
                .setContentTitle("Построение трека")
                .setContentText("Сбор геолокации...")
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
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        logd("ForegroundGeolocationService locationResult.lastLocation ${locationResult.lastLocation}")
                    }
                },
                Looper.getMainLooper(),
            )
        }
    }
}
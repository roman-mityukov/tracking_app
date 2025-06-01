package io.rm.test.geo

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import io.rm.test.geo.utils.log.initLogs
import io.rm.test.geo.utils.log.logd
import io.rm.test.geo.utils.log.logw
import java.io.File

class CurrentGeolocationWorker(applicationContext: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(applicationContext, workerParameters) {
    val powerManager =
        this.applicationContext.getSystemService(PowerManager::class.java) as PowerManager
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.applicationContext)
    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        //createForegroundInfo()
        logd("powerManager.isPowerSaveMode " + powerManager.isPowerSaveMode)
        logd("powerManager.locationPowerSaveMode " + powerManager.locationPowerSaveMode)
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener {
            logd("onSuccess getCurrentLocation ${it?.toString()}")
        }.addOnFailureListener {
            logw("onFailure getCurrentLocation $it")
        }
        return Result.success()
    }

    private suspend fun createForegroundInfo() {
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(
                applicationContext,
                "geolocationChannelId"
            )

        val notification = builder
            .setContentTitle("contentTitle")
            .setContentText("contentText")
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setOngoing(true)
            .setSilent(true)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .build()

        setForeground(
            ForegroundInfo(
                1176,
                notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                } else {
                    0
                }
            )
        )
    }
}
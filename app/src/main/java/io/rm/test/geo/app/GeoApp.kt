package io.rm.test.geo.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import io.rm.test.geo.CurrentGeolocationWorker
import io.rm.test.geo.utils.log.initLogs
import io.rm.test.geo.utils.log.logd
import java.io.File
import java.time.Duration

class GeoApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val directoryName = "logs"
        val directory = File(applicationContext.getExternalFilesDir(null), directoryName)

        if (directory.exists().not()) {
            val isDirectoryCreated = directory.mkdir()
            if (isDirectoryCreated.not()) {
                error("Can not create directory with name $directoryName")
            }
        }
        initLogs(directory)

        logd("Application onCreate")

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel = NotificationChannel(
            "geolocationChannelId",
            "описание канала",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(notificationChannel)

        val requestBuilder =
            PeriodicWorkRequestBuilder<CurrentGeolocationWorker>(
                repeatInterval = Duration.ofMinutes(5)
            )
        val workManager = WorkManager.getInstance(this.applicationContext)
        workManager.enqueueUniquePeriodicWork(
            "CurrentGeolocationWorker",
            ExistingPeriodicWorkPolicy.REPLACE,
            requestBuilder.build()
        )
    }
}
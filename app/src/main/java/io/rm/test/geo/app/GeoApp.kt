package io.rm.test.geo.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import io.rm.test.geo.core.worker.AppWorkerFactory
import io.rm.test.geo.utils.log.initLogs
import io.rm.test.geo.utils.log.logd
import java.io.File
import javax.inject.Inject

@HiltAndroidApp
class GeoApp : Application() {
    @Inject
    lateinit var appWorkerFactory: AppWorkerFactory

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

        val workManagerConfig = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .setWorkerFactory(appWorkerFactory)
            .build()
        WorkManager.initialize(this, workManagerConfig)
    }
}
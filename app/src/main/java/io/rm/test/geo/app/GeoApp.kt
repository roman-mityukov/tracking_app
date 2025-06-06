package io.rm.test.geo.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import io.rm.test.geo.BuildConfig
import io.rm.test.geo.core.worker.AppWorkerFactory
import io.rm.test.geo.utils.log.initLogs
import io.rm.test.geo.utils.log.logd
import javax.inject.Inject

@HiltAndroidApp
class GeoApp : Application() {
    @Inject
    lateinit var appWorkerFactory: AppWorkerFactory

    override fun onCreate() {
        super.onCreate()

        MapKitFactory.setApiKey(BuildConfig.YANDEX_MAPKIT_API_KEY)

        initLogs(this)

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
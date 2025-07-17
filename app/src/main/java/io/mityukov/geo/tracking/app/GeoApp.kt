package io.mityukov.geo.tracking.app

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import io.mityukov.geo.tracking.BuildConfig
import io.mityukov.geo.tracking.core.worker.AppWorkerFactory
import io.mityukov.geo.tracking.utils.log.initLogs
import javax.inject.Inject

@HiltAndroidApp
class GeoApp : Application() {
    @Inject
    lateinit var appWorkerFactory: AppWorkerFactory

    override fun onCreate() {
        super.onCreate()

        MapKitFactory.setApiKey(BuildConfig.YANDEX_MAPKIT_API_KEY)

        initLogs(this)

        val workManagerConfig = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .setWorkerFactory(appWorkerFactory)
            .build()
        WorkManager.initialize(this, workManagerConfig)
    }
}

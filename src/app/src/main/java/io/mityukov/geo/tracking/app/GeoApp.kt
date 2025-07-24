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
import ru.ok.tracer.HasTracerConfiguration
import ru.ok.tracer.TracerConfiguration
import ru.ok.tracer.crash.report.CrashFreeConfiguration
import ru.ok.tracer.crash.report.CrashReportConfiguration
import javax.inject.Inject

@HiltAndroidApp
class GeoApp : Application(), HasTracerConfiguration {
    @Inject
    lateinit var appWorkerFactory: AppWorkerFactory

    override val tracerConfiguration: List<TracerConfiguration>
        get() = listOf(
            CrashReportConfiguration.build {
                setSendAnr(true)
                setNativeEnabled(true)
            },
            CrashFreeConfiguration.build {
                setEnabled(true)
            },
        )

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

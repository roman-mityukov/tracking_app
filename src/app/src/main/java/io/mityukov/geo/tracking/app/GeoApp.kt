package io.mityukov.geo.tracking.app

import android.app.Application
import android.os.StrictMode
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import io.mityukov.geo.tracking.BuildConfig
import io.mityukov.geo.tracking.core.common.di.LogsDirectory
import io.mityukov.geo.tracking.log.Logger
import ru.ok.tracer.HasTracerConfiguration
import ru.ok.tracer.TracerConfiguration
import ru.ok.tracer.crash.report.CrashFreeConfiguration
import ru.ok.tracer.crash.report.CrashReportConfiguration
import java.io.File
import javax.inject.Inject

@HiltAndroidApp
class GeoApp : Application(), HasTracerConfiguration {
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

    @Inject
    @LogsDirectory
    lateinit var logsDirectory: File

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            val threadPolicy = StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build()
            StrictMode.setThreadPolicy(threadPolicy)
            val vmPolicy = StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build()
            StrictMode.setVmPolicy(vmPolicy)
        }

        MapKitFactory.setApiKey(BuildConfig.YANDEX_MAPKIT_API_KEY)

        Logger.initLogs(logsDirectory)
    }
}

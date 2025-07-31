package io.mityukov.geo.tracking.app

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import io.mityukov.geo.tracking.BuildConfig
import io.mityukov.geo.tracking.utils.log.initLogs
import ru.ok.tracer.HasTracerConfiguration
import ru.ok.tracer.TracerConfiguration
import ru.ok.tracer.crash.report.CrashFreeConfiguration
import ru.ok.tracer.crash.report.CrashReportConfiguration

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

    override fun onCreate() {
        super.onCreate()

        MapKitFactory.setApiKey(BuildConfig.YANDEX_MAPKIT_API_KEY)

        initLogs(this)
    }
}

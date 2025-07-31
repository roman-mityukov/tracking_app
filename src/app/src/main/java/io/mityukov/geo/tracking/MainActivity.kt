package io.mityukov.geo.tracking

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.AndroidEntryPoint
import io.mityukov.geo.tracking.app.AppNavHost
import io.mityukov.geo.tracking.app.AppProps
import io.mityukov.geo.tracking.app.ui.theme.GeoAppTheme
import io.mityukov.geo.tracking.core.data.repository.settings.app.LocalAppSettings
import io.mityukov.geo.tracking.feature.splash.SplashViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var splashViewModel: SplashViewModel

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel = NotificationChannel(
            AppProps.TRACK_CAPTURE_CHANNEL_ID,
            resources.getString(R.string.track_capture_notification_channel_description),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(notificationChannel)

        MapKitFactory.initialize(this@MainActivity)
        enableEdgeToEdge()
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            true
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                val localAppSettings: LocalAppSettings = splashViewModel.getAppSettings()

                splashScreen.setKeepOnScreenCondition {
                    false
                }
                setContent {
                    GeoAppTheme {
                        AppNavHost(
                            showOnboarding = localAppSettings.showOnboarding
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        super.onStop()
        MapKitFactory.getInstance().onStop()
    }
}

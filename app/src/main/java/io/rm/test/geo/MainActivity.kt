package io.rm.test.geo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dagger.hilt.android.AndroidEntryPoint
import io.rm.test.geo.app.AppNavHost
import io.rm.test.geo.app.ui.theme.GeoAppTheme
import io.rm.test.geo.core.data.repository.settings.app.LocalAppSettings
import io.rm.test.geo.feature.splash.SplashViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var splashViewModel: SplashViewModel

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}
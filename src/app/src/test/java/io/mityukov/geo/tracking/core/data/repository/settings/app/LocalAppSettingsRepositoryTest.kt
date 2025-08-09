package io.mityukov.geo.tracking.core.data.repository.settings.app

import android.content.Context
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import io.mityukov.geo.tracking.app.AppProps
import io.mityukov.geo.tracking.core.data.repository.settings.app.proto.ProtoLocalAppSettings
import io.mityukov.geo.tracking.core.datastore.appSettingsDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Duration.Companion.seconds

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.VANILLA_ICE_CREAM])
class LocalAppSettingsRepositoryTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var dataStore: DataStore<ProtoLocalAppSettings>
    private lateinit var localAppSettingsRepository: LocalAppSettingsRepository

    @Before
    fun setUp() {
        dataStore = context.appSettingsDataStore
        localAppSettingsRepository =
            LocalAppSettingsRepositoryImpl(dataStore, Dispatchers.IO)
    }

    @After
    fun tearDown() {
        runBlocking {
            localAppSettingsRepository.resetToDefaults()
        }
    }

    @Test
    fun `localAppSettingsRepository has default values`() = runTest {
        localAppSettingsRepository.localAppSettings.test {
            val localAppSettings = awaitItem()
            assert(localAppSettings.showOnboarding)
            assert(localAppSettings.geolocationUpdatesInterval == AppProps.Defaults.GEOLOCATION_UPDATES_INTERVAL)
        }
    }

    @Test
    fun `switch onboarding`() = runTest {
        localAppSettingsRepository.switchOnboarding()
        localAppSettingsRepository.localAppSettings.test {
            val localAppSettings = awaitItem()
            assert(localAppSettings.showOnboarding.not())
            assert(localAppSettings.geolocationUpdatesInterval == AppProps.Defaults.GEOLOCATION_UPDATES_INTERVAL)
        }
    }

    @Test
    fun `change geolocation updates interval`() = runTest {
        val interval = 20.seconds
        localAppSettingsRepository.setGeolocationUpdatesRate(interval)
        localAppSettingsRepository.localAppSettings.test {
            val localAppSettings = awaitItem()
            assert(localAppSettings.showOnboarding)
            assert(localAppSettings.geolocationUpdatesInterval == interval)
        }
    }

    @Test
    fun `reset to defaults`() = runTest {
        localAppSettingsRepository.switchOnboarding()
        val interval = 20.seconds
        localAppSettingsRepository.setGeolocationUpdatesRate(interval)
        localAppSettingsRepository.localAppSettings.test {
            val localAppSettings = awaitItem()
            assert(localAppSettings.showOnboarding.not())
            assert(localAppSettings.geolocationUpdatesInterval == interval)
        }
        localAppSettingsRepository.resetToDefaults()
        localAppSettingsRepository.localAppSettings.test {
            val localAppSettings = awaitItem()
            assert(localAppSettings.showOnboarding)
            assert(localAppSettings.geolocationUpdatesInterval == AppProps.Defaults.GEOLOCATION_UPDATES_INTERVAL)
        }
    }
}
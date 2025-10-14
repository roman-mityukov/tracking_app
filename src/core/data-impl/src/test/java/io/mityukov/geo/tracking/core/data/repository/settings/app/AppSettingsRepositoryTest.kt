package io.mityukov.geo.tracking.core.data.repository.settings.app

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import io.mityukov.geo.tracking.core.datastore.appSettingsDataStore
import io.mityukov.geo.tracking.core.datastore.proto.ProtoLocalAppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.time.Duration.Companion.seconds

@RunWith(RobolectricTestRunner::class)
class AppSettingsRepositoryTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var dataStore: DataStore<ProtoLocalAppSettings>
    private lateinit var appSettingsRepository: AppSettingsRepository

    @Before
    fun setUp() {
        dataStore = context.appSettingsDataStore
        appSettingsRepository =
            AppSettingsRepositoryImpl(dataStore, Dispatchers.IO)
    }

    @After
    fun tearDown() {
        runBlocking {
            appSettingsRepository.resetToDefaults()
        }
    }

    @Test
    fun `localAppSettingsRepository has default values`() = runTest {
        appSettingsRepository.appSettings.test {
            val localAppSettings = awaitItem()
            assert(localAppSettings.showOnboarding)
            assert(localAppSettings.geolocationUpdatesInterval == AppSettings.Defaults.GEOLOCATION_UPDATES_INTERVAL)
        }
    }

    @Test
    fun `switch onboarding`() = runTest {
        appSettingsRepository.switchOnboarding()
        appSettingsRepository.appSettings.test {
            val localAppSettings = awaitItem()
            assert(localAppSettings.showOnboarding.not())
            assert(localAppSettings.geolocationUpdatesInterval == AppSettings.Defaults.GEOLOCATION_UPDATES_INTERVAL)
        }
    }

    @Test
    fun `change geolocation updates interval`() = runTest {
        val interval = 20.seconds
        appSettingsRepository.setGeolocationUpdatesRate(interval)
        appSettingsRepository.appSettings.test {
            val localAppSettings = awaitItem()
            assert(localAppSettings.showOnboarding)
            assert(localAppSettings.geolocationUpdatesInterval == interval)
        }
    }

    @Test
    fun `reset to defaults`() = runTest {
        appSettingsRepository.switchOnboarding()
        val interval = 20.seconds
        appSettingsRepository.setGeolocationUpdatesRate(interval)
        appSettingsRepository.appSettings.test {
            val localAppSettings = awaitItem()
            assert(localAppSettings.showOnboarding.not())
            assert(localAppSettings.geolocationUpdatesInterval == interval)
        }
        appSettingsRepository.resetToDefaults()
        appSettingsRepository.appSettings.test {
            val localAppSettings = awaitItem()
            assert(localAppSettings.showOnboarding)
            assert(localAppSettings.geolocationUpdatesInterval == AppSettings.Defaults.GEOLOCATION_UPDATES_INTERVAL)
        }
    }
}

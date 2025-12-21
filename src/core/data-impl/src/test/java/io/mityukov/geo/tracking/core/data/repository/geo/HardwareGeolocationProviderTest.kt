package io.mityukov.geo.tracking.core.data.repository.geo

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.os.PowerManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class HardwareGeolocationProviderTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    private lateinit var provider: HardwareGeolocationProviderImpl
    private lateinit var locationManager: LocationManager
    private lateinit var powerManager: PowerManager
    private lateinit var context: Context
    private val interval = 1.seconds
    private val minDistance = 10f

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        provider = HardwareGeolocationProviderImpl(context)
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    }

    @Test
    fun `getLastKnownLocation returns location`() = runTest {
        val mockLocation = Location(LocationManager.GPS_PROVIDER).apply {
            latitude = 55.7558
            longitude = 37.6173
            time = System.currentTimeMillis()
        }
        shadowOf(locationManager).setLastKnownLocation(LocationManager.GPS_PROVIDER, mockLocation)

        val result = provider.getLastKnownLocation()

        assert(result.location == mockLocation)
        assert(result.error == null)
    }

    @Test
    fun `getLastKnownLocation returns null`() = runTest {
        shadowOf(locationManager).setLastKnownLocation(LocationManager.GPS_PROVIDER, null)

        val result = provider.getLastKnownLocation()

        assert(result.location == null)
        assert(result.error is GeolocationUpdateException.LocationIsNull)
    }

    @Test
    fun `locationUpdates should emit locations when location changes`() = runTest {
        val testLocation = Location(LocationManager.GPS_PROVIDER).apply {
            latitude = 55.7559
            longitude = 37.6174
            time = System.currentTimeMillis()
        }

        provider.locationUpdates(interval, minDistance).test {
            val shadowLocationManager = shadowOf(locationManager)
            shadowLocationManager.simulateLocation(testLocation)

            val shadowMainLooper = shadowOf(Looper.getMainLooper())
            shadowMainLooper.runToEndOfTasks()

            val result = awaitItem()
            assertEquals(testLocation.latitude, result.location!!.latitude)
            assertNull(result.error)
        }
    }

    @Test
    fun `locationUpdates should emit error when location is disabled`() = runTest {
        provider.locationUpdates(interval, minDistance).test {
            shadowOf(locationManager).setProviderEnabled(LocationManager.GPS_PROVIDER, false)

            val shadowMainLooper = shadowOf(Looper.getMainLooper())
            shadowMainLooper.runToEndOfTasks()

            val result = awaitItem()
            assert(result.error is GeolocationUpdateException.LocationDisabled)
            assertNull(result.location)
        }
    }

    @Test
    fun `locationUpdates should remove locationListener when flow is cancelled`() = runTest {
        val shadowLocationManager = shadowOf(locationManager)

        val job = launch {
            provider.locationUpdates(interval, minDistance)
                .collect { }
        }
        advanceTimeBy(100)
        assert(shadowLocationManager.getRequestLocationUpdateListeners().size == 1)

        job.cancel()
        advanceTimeBy(100)
        assert(shadowLocationManager.getRequestLocationUpdateListeners().isEmpty())
    }
}

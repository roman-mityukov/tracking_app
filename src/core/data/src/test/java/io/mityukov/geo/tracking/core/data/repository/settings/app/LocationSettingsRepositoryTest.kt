package io.mityukov.geo.tracking.core.data.repository.settings.app

import android.app.Application
import android.content.Context
import android.location.LocationManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

@RunWith(RobolectricTestRunner::class)
class LocationSettingsRepositoryTest {
    lateinit var context: Application
    lateinit var locationSettingsRepository: LocationSettingsRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        locationSettingsRepository = LocationSettingsRepositoryImpl(context)
    }

    @Test
    fun locationEnabled_isFalse_locationDisabledOnDevice() {
        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val shadow = Shadows.shadowOf(locationManager)
        shadow.setLocationEnabled(false)

        assert(locationSettingsRepository.locationEnabled.not())
    }

    @Test
    fun locationEnabled_isTrue_locationEnabledOnDevice() {
        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val shadow = Shadows.shadowOf(locationManager)
        shadow.setLocationEnabled(true)

        assert(locationSettingsRepository.locationEnabled)
    }
}
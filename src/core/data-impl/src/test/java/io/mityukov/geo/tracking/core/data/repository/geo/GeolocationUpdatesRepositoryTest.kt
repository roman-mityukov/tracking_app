package io.mityukov.geo.tracking.core.data.repository.geo

import android.Manifest
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import io.mityukov.geo.tracking.core.data.permission.PermissionChecker
import io.mityukov.geo.tracking.core.data.permission.PermissionCheckerImpl
import io.mityukov.geo.tracking.core.data.repository.settings.app.LocationSettingsRepository
import io.mityukov.geo.tracking.core.data.repository.settings.app.LocationSettingsRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class GeolocationUpdatesRepositoryTest {
    private lateinit var context: Application
    private lateinit var locationSettingsRepository: LocationSettingsRepository
    private lateinit var geolocationProvider: GeolocationProvider
    private lateinit var permissionChecker: PermissionChecker
    private lateinit var geolocationUpdatesRepository: GeolocationUpdatesRepository
    private lateinit var locationManager: LocationManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        locationSettingsRepository = LocationSettingsRepositoryImpl(context)
        permissionChecker = PermissionCheckerImpl(context)
        geolocationProvider = HardwareGeolocationProviderImpl(context)
        geolocationUpdatesRepository = GeolocationUpdatesRepositoryImpl(
            locationSettingsRepository = locationSettingsRepository,
            geolocationProvider = geolocationProvider,
            permissionChecker = permissionChecker,
            coroutineDispatcher = Dispatchers.Main
        )

        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    @Test
    fun `GeolocationUpdateResult contains error when location is disabled`() = runTest {
        shadowOf(locationManager).setProviderEnabled(LocationManager.GPS_PROVIDER, false)

        geolocationUpdatesRepository.currentLocation.test {
            val result = awaitItem()
            assert(result.geolocation == null)
            assert(result.error is GeolocationUpdateException.LocationDisabled)
            awaitComplete()
        }
    }

    @Test
    fun `GeolocationUpdateResult contains error when permissions are not granted`() = runTest {
        shadowOf(locationManager).setProviderEnabled(LocationManager.GPS_PROVIDER, true)

        geolocationUpdatesRepository.currentLocation.test {
            val result = awaitItem()
            assert(result.geolocation == null)
            assert(result.error is GeolocationUpdateException.PermissionsNotGranted)
            awaitComplete()
        }
    }

    @Test
    fun `GeolocationUpdateResult contains geolocation`() = runTest {
        shadowOf(locationManager).setProviderEnabled(LocationManager.GPS_PROVIDER, true)

        shadowOf(locationManager).setLastKnownLocation(
            LocationManager.GPS_PROVIDER,
            Location(LocationManager.GPS_PROVIDER).apply {
                latitude = 55.7558
                longitude = 37.6173
                time = System.currentTimeMillis()
            })

        Shadows.shadowOf(context).grantPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val result = geolocationUpdatesRepository.currentLocation.first()

        assert(result.geolocation != null)
        assert(result.error == null)
    }
}

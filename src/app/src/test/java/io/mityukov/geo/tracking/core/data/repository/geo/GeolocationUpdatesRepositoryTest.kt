package io.mityukov.geo.tracking.core.data.repository.geo

import app.cash.turbine.test
import io.mityukov.geo.tracking.core.data.repository.settings.app.LocationSettingsRepository
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.test.utils.GeolocationUtils
import io.mityukov.geo.tracking.utils.permission.PermissionChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class GeolocationUpdatesRepositoryTest {
    @Test
    fun `first result is Initialization`() = runTest {
        val geolocationUpdatesRepository = buildRepositoryUnderTest(
            deviceLocationEnabled = false,
            permissionGranted = false,
        )

        geolocationUpdatesRepository.currentLocation.test {
            expectThat(awaitItem()).isEqualTo(
                GeolocationUpdateResult(
                    geolocation = null,
                    error = GeolocationUpdateException.Initialization
                )
            )
        }
    }

    @Test
    fun `location disabled then result is LocationDisabled`() = runTest {
        val geolocationUpdatesRepository = buildRepositoryUnderTest(
            deviceLocationEnabled = false,
            permissionGranted = false,
        )

        geolocationUpdatesRepository.start()

        geolocationUpdatesRepository.currentLocation.test {
            expectThat(awaitItem()).isEqualTo(
                GeolocationUpdateResult(
                    geolocation = null,
                    error = GeolocationUpdateException.LocationDisabled
                )
            )
        }
    }

    @Test
    fun `location permission is not granted then result is PermissionsNotGranted`() = runTest {
        val geolocationUpdatesRepository = buildRepositoryUnderTest(
            deviceLocationEnabled = true,
            permissionGranted = false,
        )

        geolocationUpdatesRepository.start()

        geolocationUpdatesRepository.currentLocation.test {
            expectThat(awaitItem()).isEqualTo(
                GeolocationUpdateResult(
                    geolocation = null,
                    error = GeolocationUpdateException.PermissionsNotGranted
                )
            )
        }
    }

    @Test
    fun `location enabled, location permission is granted then result is Geolocation`() = runTest {
        val geolocationUpdatesRepository = buildRepositoryUnderTest(
            deviceLocationEnabled = true,
            permissionGranted = true,
        )

        geolocationUpdatesRepository.start()

        geolocationUpdatesRepository.currentLocation.test {
            expectThat(awaitItem()).isEqualTo(
                GeolocationUpdateResult(
                    geolocation = GeolocationUtils.mockedGeolocation,
                    error = null
                )
            )
        }
    }

    @Test
    fun `geolocation provider emits null geolocation then result is LocationIsNull`() = runTest {
        val geolocationUpdatesRepository = buildRepositoryUnderTest(
            deviceLocationEnabled = true,
            permissionGranted = true,
            emitNullGeolocation = true
        )

        geolocationUpdatesRepository.start()

        geolocationUpdatesRepository.currentLocation.test {
            expectThat(awaitItem()).isEqualTo(
                GeolocationUpdateResult(
                    geolocation = null,
                    error = GeolocationUpdateException.LocationIsNull
                )
            )
        }
    }
}

private fun buildRepositoryUnderTest(
    deviceLocationEnabled: Boolean,
    permissionGranted: Boolean,
    mockedGeolocation: Geolocation = GeolocationUtils.mockedGeolocation,
    emitNullGeolocation: Boolean = false,
): GeolocationUpdatesRepository {
    val mockLocationSettingsRepository = mock<LocationSettingsRepository> {
        on { locationEnabled } doReturn deviceLocationEnabled
    }
    val mockPermissionChecker = mock<PermissionChecker> {
        on { locationGranted } doReturn permissionGranted
    }
    val fakeGeolocationProviderImpl =
        FakeGeolocationProviderImpl(
            mockedGeolocation = mockedGeolocation,
            emitNullGeolocation = emitNullGeolocation,
        )

    return GeolocationUpdatesRepositoryImpl(
        mockLocationSettingsRepository,
        fakeGeolocationProviderImpl,
        Dispatchers.IO,
        mockPermissionChecker
    )
}

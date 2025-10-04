package io.mityukov.geo.tracking.core.data.permission

import android.Manifest
import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

@RunWith(RobolectricTestRunner::class)
class PermissionCheckerTest {
    private lateinit var context: Context
    private lateinit var permissionCheckerImpl: PermissionChecker

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        permissionCheckerImpl = PermissionCheckerImpl(context)
    }

    @Test
    fun `locationGranted returns false if there are no granted location permissions`() {
        assert(!permissionCheckerImpl.locationGranted)
    }

    @Test
    fun `locationGranted returns false if there is only fine location`() {
        Shadows.shadowOf(context as Application).grantPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
        assert(!permissionCheckerImpl.locationGranted)
    }

    @Test
    fun `locationGranted returns false if there is only coarse location`() {
        Shadows.shadowOf(context as Application).grantPermissions(
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
        assert(!permissionCheckerImpl.locationGranted)
    }

    @Test
    fun locationGranted_returnsTrue() {
        Shadows.shadowOf(context as Application).grantPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        assert(permissionCheckerImpl.locationGranted)
    }
}
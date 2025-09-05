package io.mityukov.geo.tracking.feature.map

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MapContentScreenshotTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun initialState() {
        composeTestRule.setContent {
            MapContent(
                onUpdateCurrentLocation = { _ -> },
                onSharing = {},
                onPendingLocation = {},
                onPendingLocationComplete = {},
                mapViewFactory = { context -> View(context) },
                mapViewModelState = MapState.CurrentLocation(
                    data = Geolocation(
                        latitude = 53.654810,
                        longitude = 87.450375,
                        altitude = 310.2,
                        speed = 1.4f,
                        time = 1756964259,
                    )
                )
            )
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}

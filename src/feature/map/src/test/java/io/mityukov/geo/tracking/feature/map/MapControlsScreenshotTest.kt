package io.mityukov.geo.tracking.feature.map

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MapControlsScreenshotTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun initialState() {
        composeTestRule.setContent {
            MapControls(
                onNavigateTo = { _ -> },
                onZoomIn = {},
                onZoomOut = {},
                currentGeolocation = null
            )
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}

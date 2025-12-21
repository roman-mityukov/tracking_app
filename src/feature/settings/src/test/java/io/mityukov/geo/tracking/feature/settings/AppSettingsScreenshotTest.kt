package io.mityukov.geo.tracking.feature.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import io.mityukov.geo.tracking.feature.settings.geolocation.GeolocationSettingsState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.time.Duration.Companion.seconds

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.SmallPhone)
class AppSettingsScreenshotTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun initialState() {
        composeTestRule.setContent {
            AppSettingsContent(
                geolocationSettingsState = GeolocationSettingsState.Data(
                    interval = 3.seconds,
                    availableIntervals = listOf(3.seconds, 5.seconds),
                    accuracy = 0,
                    availableAccuracy = listOf(0, 10),
                    acceptableSpeed = 10,
                    availableAcceptableSpeed = listOf(0, 10),
                ),
                onInstructionsSelect = {},
                onIntervalSelect = {},
                onAccuracySelect = {},
                onSpeedSelect = {},
                onBack = {}
            )
        }

        composeTestRule.onRoot().captureRoboImage()
    }
}

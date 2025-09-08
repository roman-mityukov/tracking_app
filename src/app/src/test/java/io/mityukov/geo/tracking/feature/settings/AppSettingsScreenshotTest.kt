package io.mityukov.geo.tracking.feature.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import io.mityukov.geo.tracking.feature.settings.geolocation.GeolocationUpdatesIntervalState
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
            AppSettingsScreen(
                geolocationUpdatesIntervalState = GeolocationUpdatesIntervalState.Data(
                    interval = 3.seconds,
                    availableIntervals = listOf(),
                ),
                onIntervalSelect = {},
                onInstructionsSelect = {},
                onBack = {}
            )
        }
    }
}

package io.mityukov.geo.tracking.feature.track.list

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.SmallPhone)
class TracksListScreenshotTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun pending() {
        composeTestRule.setContent {
            TrackListUnderTest(state = TracksState.Pending)
        }

        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun emptyList() {
        composeTestRule.setContent {
            TrackListUnderTest(state = TracksState.Data(listOf()))
        }

        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun populatedList() {
        val state =
            TracksStateProvider().values.first {
                it is TracksState.Data && it.tracks.isNotEmpty()
            }
        composeTestRule.setContent {
            TrackListUnderTest(state = state)
        }

        composeTestRule.onRoot().captureRoboImage()
    }

    @Composable
    fun TrackListUnderTest(state: TracksState) {
        TrackList(
            state = state,
            onClick = {},
            onLongPress = {},
        )
    }
}

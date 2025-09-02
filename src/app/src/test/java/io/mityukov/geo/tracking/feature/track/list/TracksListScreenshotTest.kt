package io.mityukov.geo.tracking.feature.track.list

import androidx.activity.ComponentActivity
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
            TrackList(
                state = TracksState.Pending,
                onClick = {},
                onLongPress = {},
            )
        }

        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun emptyList() {
        composeTestRule.setContent {
            TrackList(
                state = TracksState.Data(listOf()),
                onClick = {},
                onLongPress = {},
            )
        }

        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun populatedList() {
        val tracks =
            (TracksStateProvider().values.first {
                it is TracksState.Data && it.tracks.isNotEmpty()
            } as TracksState.Data).tracks
        composeTestRule.setContent {
            TrackList(
                state = TracksState.Data(tracks = tracks),
                onClick = {},
                onLongPress = {},
            )
        }

        composeTestRule.onRoot().captureRoboImage()
    }
}

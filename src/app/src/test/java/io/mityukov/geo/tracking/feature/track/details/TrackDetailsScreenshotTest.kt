package io.mityukov.geo.tracking.feature.track.details

import android.view.View
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
class TrackDetailsScreenshotTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun pendingState() {
        composeTestRule.setContent {
            TrackDetailsScreen(
                state = TrackDetailsState.Pending,
                sharingState = null,
                mapViewFactory = { View(it) },
                onShowTrack = {},
                onTrackMapSelected = {},
                onDelete = {},
                onPrepareShare = {},
                onShare = {},
                onBack = {},
            )
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun dataState() {
        composeTestRule.setContent {
            TrackDetailsScreen(
                state = TrackDetailsStateProvider().values.first { it is TrackDetailsState.Data },
                sharingState = null,
                mapViewFactory = { View(it) },
                onShowTrack = {},
                onTrackMapSelected = {},
                onDelete = {},
                onPrepareShare = {},
                onShare = {},
                onBack = {},
            )
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}

package io.mityukov.geo.tracking.feature.track.capture

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import io.mityukov.geo.tracking.app.ui.theme.GeoAppTheme
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackCaptureStatus
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackInProgress
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import kotlin.time.Duration.Companion.seconds

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TrackCaptureScreenshotTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val trackInProgress = TrackInProgress(
        start = 1756973047,
        distance = 789.3f,
        altitudeUp = 3.3f,
        altitudeDown = 4.1f,
        duration = 123L.seconds,
        sumSpeed = 24.5f,
        minSpeed = 12.1f,
        maxSpeed = 13.2f,
        geolocationCount = 3,
        paused = false,
        lastLocation = null,
    )

    @Test
    fun trackCaptureControlsIdle() {
        composeTestRule.setContent {
            TrackCaptureControlsUnderTest(
                state = TrackCaptureState(TrackCaptureStatus.Idle),
            )
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun trackCaptureControlsError() {
        composeTestRule.setContent {
            TrackCaptureControlsUnderTest(
                state = TrackCaptureState(TrackCaptureStatus.Error),
            )
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun trackCaptureControlsRun() {
        composeTestRule.setContent {
            TrackCaptureControlsUnderTest(
                state = TrackCaptureState(TrackCaptureStatus.Run(trackInProgress)),
            )
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun trackCaptureControlsRunPaused() {
        composeTestRule.setContent {
            TrackCaptureControlsUnderTest(
                state = TrackCaptureState(
                    TrackCaptureStatus.Run(
                        trackInProgress.copy(paused = true)
                    )
                ),
            )
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Composable
    private fun TrackCaptureControlsUnderTest(state: TrackCaptureState) {
        GeoAppTheme {
            TrackCapture(
                trackCaptureState = state,
                onStartCapture = {},
                onStopCapture = {},
                onPlayCapture = {},
                onPauseCapture = {},
                onUpdateTrack = {}
            )
        }
    }
}

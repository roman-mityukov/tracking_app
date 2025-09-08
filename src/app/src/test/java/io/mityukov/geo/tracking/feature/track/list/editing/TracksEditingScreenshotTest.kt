package io.mityukov.geo.tracking.feature.track.list.editing

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import io.mityukov.geo.tracking.utils.test.AppTestTag
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.SmallPhone)
class TracksEditingScreenshotTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun pending() {
        composeTestRule.setContent {
            TracksEditingScreen (
                state = TracksEditingState.Pending,
                onChangeSelection = {},
                onDeleteConfirm = {},
                onBack = {},
            )
        }

        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun listWithSelection() {
        val state =
            TracksEditingStateProvider().values.first {
                it is TracksEditingState.Data && it.selectedTracks.isNotEmpty()
            }
        composeTestRule.setContent {
            TracksEditingScreenUnderTest (state = state)
        }

        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun dialog() {
        val state =
            TracksEditingStateProvider().values.first {
                it is TracksEditingState.Data && it.selectedTracks.isNotEmpty()
            }
        composeTestRule.setContent {
            TracksEditingScreenUnderTest (state = state)
        }

        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_DELETE).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onRoot().captureRoboImage()
    }

    @Composable
    fun TracksEditingScreenUnderTest(state: TracksEditingState) {
        TracksEditingScreen(
            state = state,
            onChangeSelection = {},
            onDeleteConfirm = {},
            onBack = {},
        )
    }
}

package io.mityukov.geo.tracking.feature.track.details

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import io.mityukov.geo.tracking.core.data.validation.TrackValidationResult
import io.mityukov.geo.tracking.feature.track.editing.TrackEditingSheet
import io.mityukov.geo.tracking.feature.track.editing.TrackEditingState
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
            TrackDetailsScreenUnderTest(TrackDetailsState.Pending)
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun dataState() {
        composeTestRule.setContent {
            TrackDetailsScreenUnderTest(
                TrackDetailsStateProvider().values.first {
                    it is TrackDetailsState.Data && it.detailedTrack.geolocations.isNotEmpty()
                },
            )
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun dataEmptyState() {
        composeTestRule.setContent {
            TrackDetailsScreenUnderTest(
                TrackDetailsStateProvider().values.first {
                    it is TrackDetailsState.Data && it.detailedTrack.geolocations.isEmpty()
                },
            )
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun editingValidData() {
        composeTestRule.setContent {
            TrackEditingSheet(
                track = track,
                viewModelState = TrackEditingState.Initial,
                onSave = {},
                onSaveCompleted = {},
                onDismiss = {}
            )
        }

        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun editingInvalidName() {
        composeTestRule.setContent {
            TrackEditingSheet(
                track = track,
                viewModelState = TrackEditingState.ValidationFailed(TrackValidationResult.Invalid.Name),
                onSave = {},
                onSaveCompleted = {},
                onDismiss = {}
            )
        }

        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun editingInvalidDescription() {
        composeTestRule.setContent {
            TrackEditingSheet(
                track = track,
                viewModelState = TrackEditingState.ValidationFailed(TrackValidationResult.Invalid.Description),
                onSave = {},
                onSaveCompleted = {},
                onDismiss = {}
            )
        }

        composeTestRule.onRoot().captureRoboImage()
    }

    @Composable
    private fun TrackDetailsScreenUnderTest(state: TrackDetailsState) {
        TrackDetailsScreen(
            state = state,
            sharingState = null,
            mapViewFactory = { View(it) },
            onShowTrack = {},
            onTrackMapSelected = {},
            onDelete = {},
            onDeleteFailed = {},
            onPrepareShare = {},
            onShare = {},
            onBack = {},
        )
    }
}

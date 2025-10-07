package io.mityukov.geo.tracking.feature.track.details

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import io.mityukov.geo.tracking.core.model.track.Track
import io.mityukov.geo.tracking.core.test.AppTestTag
import io.mityukov.geo.tracking.feature.track.editing.TrackEditingSheet
import io.mityukov.geo.tracking.feature.track.editing.TrackEditingState
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import kotlin.time.Duration.Companion.seconds

class TrackEditingSheetTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var mockOnSave: (Track) -> Unit
    private lateinit var mockOnDismiss: () -> Unit

    private val track = Track(
        id = "trackDomainId",
        description = "trackDomainDescription",
        name = "trackDomainName",
        start = 123,
        end = 456,
        distance = 789.3f,
        altitudeUp = 3.3f,
        altitudeDown = 4.1f,
        duration = 123L.seconds,
        sumSpeed = 24.5f,
        minSpeed = 12.1f,
        maxSpeed = 13.2f,
        geolocationCount = 3,
        filePath = "trackDomainFilePath",
    )

    @Before
    fun setUp() {
        mockOnSave = mock()
        mockOnDismiss = mock()
    }

    @Test
    fun buttonCancelClicked_onDismissCalled() {
        composeTestRule.setContent {
            TrackEditingSheetUnderTest(TrackEditingState.Initial)
        }
        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_CANCEL).performClick()
        verify(mockOnDismiss).invoke()
        verifyNoInteractions(mockOnSave)
    }

    @Test
    fun buttonSaveClicked_onSaveCalled() {
        composeTestRule.setContent {
            TrackEditingSheetUnderTest(TrackEditingState.Initial)
        }
        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_SAVE).performClick()
        verify(mockOnSave).invoke(track)
        verifyNoInteractions(mockOnDismiss)
    }

    @Composable
    fun TrackEditingSheetUnderTest(state: TrackEditingState) {
        TrackEditingSheet(
            track = track,
            viewModelState = state,
            onSave = mockOnSave,
            onSaveCompleted = {},
            onDismiss = mockOnDismiss,
        )
    }
}
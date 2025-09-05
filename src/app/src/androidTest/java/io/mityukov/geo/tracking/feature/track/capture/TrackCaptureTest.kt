package io.mityukov.geo.tracking.feature.track.capture

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import io.mityukov.geo.tracking.R
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackCaptureStatus
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackInProgress
import io.mityukov.geo.tracking.utils.test.AppTestTag
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class TrackCaptureTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var mockOnStart: () -> Unit
    private lateinit var mockOnStop: () -> Unit
    private lateinit var mockOnPlay: () -> Unit
    private lateinit var mockOnPause: () -> Unit

    @Before
    fun setUp() {
        mockOnStart = mock()
        mockOnStop = mock()
        mockOnPlay = mock()
        mockOnPause = mock()
    }

    @Test
    fun errorState_buttonStart_displayed() {
        composeTestRule.setContent {
            TrackCaptureControlsUnderTest(TrackCaptureState(status = TrackCaptureStatus.Error))
        }

        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_START_TRACK_CAPTURE)
            .assertIsDisplayed()
            .assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.resources.getString(
                R.string.content_description_map_start_track
            )
        )
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_STOP_TRACK_CAPTURE)
            .assertIsNotDisplayed()
    }

    @Test
    fun errorState_clickOnButtonStart_onStartCalled() {
        composeTestRule.setContent {
            TrackCaptureControlsUnderTest(TrackCaptureState(status = TrackCaptureStatus.Error))
        }

        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_START_TRACK_CAPTURE).performClick()

        verify(mockOnStart).invoke()
        verifyNoInteractions(mockOnStop)
        verifyNoInteractions(mockOnPlay)
        verifyNoInteractions(mockOnPause)
    }

    @Test
    fun idleState_buttonStart_displayed() {
        composeTestRule.setContent {
            TrackCaptureControlsUnderTest(TrackCaptureState(status = TrackCaptureStatus.Idle))
        }

        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_START_TRACK_CAPTURE)
            .assertIsDisplayed()
            .assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.resources.getString(
                R.string.content_description_map_start_track
            )
        )
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_STOP_TRACK_CAPTURE)
            .assertIsNotDisplayed()
    }

    @Test
    fun idleState_clickOnButtonStart_onStartCalled() {
        composeTestRule.setContent {
            TrackCaptureControlsUnderTest(TrackCaptureState(status = TrackCaptureStatus.Idle))
        }

        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_START_TRACK_CAPTURE).performClick()

        verify(mockOnStart).invoke()
        verifyNoInteractions(mockOnStop)
        verifyNoInteractions(mockOnPlay)
        verifyNoInteractions(mockOnPause)
    }

    @Test
    fun runStateNotPaused_buttonStopAndButtonPause_displayed() {
        composeTestRule.setContent {
            TrackCaptureControlsUnderTest(
                TrackCaptureState(
                    status = TrackCaptureStatus.Run(
                        TrackInProgress.empty()
                    )
                )
            )
        }

        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_START_TRACK_CAPTURE)
            .assertIsDisplayed()
            .assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.resources.getString(
                R.string.content_description_map_pause_track
            )
        )
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_STOP_TRACK_CAPTURE)
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun runStateNotPaused_clickOnButtonPause_onPauseCalled() {
        composeTestRule.setContent {
            TrackCaptureControlsUnderTest(
                TrackCaptureState(
                    status = TrackCaptureStatus.Run(
                        TrackInProgress.empty()
                    )
                )
            )
        }

        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_START_TRACK_CAPTURE).performClick()
        verify(mockOnPause).invoke()
        verifyNoInteractions(mockOnStop)
        verifyNoInteractions(mockOnPlay)
        verifyNoInteractions(mockOnStart)
    }

    @Test
    fun runStateNotPaused_clickOnButtonStop_onStopCalled() {
        composeTestRule.setContent {
            TrackCaptureControlsUnderTest(
                TrackCaptureState(
                    status = TrackCaptureStatus.Run(
                        TrackInProgress.empty()
                    )
                )
            )
        }

        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_STOP_TRACK_CAPTURE).performClick()
        verify(mockOnStop).invoke()
        verifyNoInteractions(mockOnPause)
        verifyNoInteractions(mockOnPlay)
        verifyNoInteractions(mockOnStart)
    }

    @Test
    fun runStatePaused_buttonStopAndButtonPlay_displayed() {
        composeTestRule.setContent {
            TrackCaptureControlsUnderTest(
                TrackCaptureState(
                    status = TrackCaptureStatus.Run(
                        TrackInProgress.empty().copy(paused = true)
                    )
                )
            )
        }

        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_START_TRACK_CAPTURE)
            .assertIsDisplayed()
            .assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.resources.getString(
                R.string.content_description_map_resume_track
            )
        )
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_STOP_TRACK_CAPTURE)
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun runStatePaused_clickOnButtonPlay_onPlayCalled() {
        composeTestRule.setContent {
            TrackCaptureControlsUnderTest(
                TrackCaptureState(
                    status = TrackCaptureStatus.Run(
                        TrackInProgress.empty().copy(paused = true)
                    )
                )
            )
        }

        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_START_TRACK_CAPTURE).performClick()
        verify(mockOnPlay).invoke()
        verifyNoInteractions(mockOnPause)
        verifyNoInteractions(mockOnStop)
        verifyNoInteractions(mockOnStart)
    }

    @Test
    fun runStatePaused_clickOnButtonStop_onStopCalled() {
        composeTestRule.setContent {
            TrackCaptureControlsUnderTest(
                TrackCaptureState(
                    status = TrackCaptureStatus.Run(
                        TrackInProgress.empty().copy(paused = true)
                    )
                )
            )
        }

        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_STOP_TRACK_CAPTURE).performClick()
        verify(mockOnStop).invoke()
        verifyNoInteractions(mockOnPause)
        verifyNoInteractions(mockOnPlay)
        verifyNoInteractions(mockOnStart)
    }

    @Composable
    fun TrackCaptureControlsUnderTest(state: TrackCaptureState) {
        TrackCapture(
            trackCaptureState = state,
            onStartCapture = mockOnStart,
            onStopCapture = mockOnStop,
            onPlayCapture = mockOnPlay,
            onPauseCapture = mockOnPause,
            onUpdateTrack = {}
        )
    }
}
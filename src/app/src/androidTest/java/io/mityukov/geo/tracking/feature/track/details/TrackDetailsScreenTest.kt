package io.mityukov.geo.tracking.feature.track.details

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import io.mityukov.geo.tracking.R
import io.mityukov.geo.tracking.utils.test.AppTestTag
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class TrackDetailsScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var mockOnBack: () -> Unit
    private lateinit var mockOnDelete: () -> Unit
    private lateinit var mockOnPrepareShare: () -> Unit
    private lateinit var mockOnTrackMapSelected: (String) -> Unit

    private val dataState: TrackDetailsState.Data =
        TrackDetailsStateProvider().values
            .first { it is TrackDetailsState.Data && it.detailedTrack.geolocations.isNotEmpty() } as TrackDetailsState.Data
    private val dataEmptyState: TrackDetailsState.Data =
        TrackDetailsStateProvider().values
            .first { it is TrackDetailsState.Data && it.detailedTrack.geolocations.isEmpty() } as TrackDetailsState.Data

    @Before
    fun setUp() {
        mockOnBack = mock()
        mockOnDelete = mock()
        mockOnTrackMapSelected = mock()
        mockOnPrepareShare = mock()
    }

    @Test
    fun buttonBackClicked_onBackCalled() {
        composeTestRule.setContent {
            TrackDetailsScreenUnderTest(TrackDetailsState.Pending)
        }

        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_BACK).performClick()
        verify(mockOnBack).invoke()
        verifyNoInteractions(mockOnDelete)
        verifyNoInteractions(mockOnTrackMapSelected)
        verifyNoInteractions(mockOnPrepareShare)
    }

    @Test
    fun buttonMapClicked_onTrackMapSelectedCalled() {
        composeTestRule.setContent {
            TrackDetailsScreenUnderTest(dataState)
        }

        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_TRACK_DETAILS_MAP).performClick()
        verify(mockOnTrackMapSelected).invoke(dataState.detailedTrack.track.id)
        verifyNoInteractions(mockOnDelete)
        verifyNoInteractions(mockOnBack)
        verifyNoInteractions(mockOnPrepareShare)
    }

    @Test
    fun buttonPrepareShareClicked_onPrepareShareCalled() {
        composeTestRule.setContent {
            TrackDetailsScreenUnderTest(dataState)
        }

        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_SHARE).performClick()
        verify(mockOnPrepareShare).invoke()
        verifyNoInteractions(mockOnDelete)
        verifyNoInteractions(mockOnBack)
        verifyNoInteractions(mockOnTrackMapSelected)
    }

    @Test
    fun buttonDeleteClicked_showDialog() {
        composeTestRule.setContent {
            TrackDetailsScreenUnderTest(dataState)
        }

        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_DELETE).performScrollTo()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_DELETE).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(AppTestTag.DIALOG_DELETE).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(composeTestRule.activity.resources.getString(R.string.track_details_delete_dialog_title))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(composeTestRule.activity.resources.getString(R.string.track_details_delete_dialog_text))
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_NO).assertIsDisplayed()
        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_YES).assertIsDisplayed()
        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_YES).performClick()
        verify(mockOnDelete).invoke()
    }

    @Test
    fun buttonDeleteClicked_emptyTrack_showDialog() {
        composeTestRule.setContent {
            TrackDetailsScreenUnderTest(dataEmptyState)
        }

        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_DELETE).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(AppTestTag.DIALOG_DELETE).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(composeTestRule.activity.resources.getString(R.string.track_details_delete_dialog_title))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(composeTestRule.activity.resources.getString(R.string.track_details_delete_dialog_text))
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_NO).assertIsDisplayed()
        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_YES).assertIsDisplayed()
        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_YES).performClick()
        verify(mockOnDelete).invoke()
    }

    @Composable
    fun TrackDetailsScreenUnderTest(state: TrackDetailsState) {
        TrackDetailsScreen(
            state = state,
            sharingState = null,
            mapViewFactory = { View(it) },
            onShowTrack = {},
            onTrackMapSelected = mockOnTrackMapSelected,
            onDelete = mockOnDelete,
            onPrepareShare = mockOnPrepareShare,
            onShare = {},
            onBack = mockOnBack,
        )
    }
}
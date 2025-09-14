package io.mityukov.geo.tracking.feature.track.list

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.mityukov.geo.tracking.core.test.AppTestTag
import io.mityukov.geo.tracking.feature.track.list.editing.TracksEditingScreen
import io.mityukov.geo.tracking.feature.track.list.editing.TracksEditingState
import io.mityukov.geo.tracking.feature.track.list.editing.TracksEditingStateProvider
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class TracksEditingScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    lateinit var mockOnChangeSelection: (String) -> Unit
    lateinit var mockOnDeleteConfirm: () -> Unit
    lateinit var mockOnBack: () -> Unit

    @Before
    fun setUp() {
        mockOnChangeSelection = mock()
        mockOnDeleteConfirm = mock()
        mockOnBack = mock()
    }

    @Test
    fun buttonBackClicked_onBackCalled() {
        composeTestRule.setContent {
            TracksEditingScreenUnderTest(TracksEditingState.Pending)
        }

        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_BACK).performClick()
        verify(mockOnBack).invoke()
        verifyNoInteractions(mockOnChangeSelection)
        verifyNoInteractions(mockOnDeleteConfirm)
    }

    @Test
    fun trackItemClicked_onChangeSelectionCalled() {
        val state =
            TracksEditingStateProvider().values.first {
                it is TracksEditingState.Data && it.selectedTracks.isNotEmpty()
            }
        composeTestRule.setContent {
            TracksEditingScreenUnderTest(state)
        }

        composeTestRule.onAllNodesWithTag(AppTestTag.TRACK_ITEM).onFirst().performClick()
        verify(mockOnChangeSelection).invoke(any())
        verifyNoInteractions(mockOnBack)
        verifyNoInteractions(mockOnDeleteConfirm)
    }

    @Test
    fun buttonDeleteClicked_showDialog() {
        val state =
            TracksEditingStateProvider().values.first {
                it is TracksEditingState.Data && it.selectedTracks.isNotEmpty()
            }
        composeTestRule.setContent {
            TracksEditingScreenUnderTest(state)
        }

        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_DELETE).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(AppTestTag.DIALOG_DELETE).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(composeTestRule.activity.resources.getString(R.string.tracks_editing_delete_dialog_title))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(composeTestRule.activity.resources.getString(R.string.tracks_editing_delete_dialog_text))
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_NO).assertIsDisplayed()
        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_YES).assertIsDisplayed()
        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_YES).performClick()
        verify(mockOnDeleteConfirm).invoke()
    }

    @Composable
    fun TracksEditingScreenUnderTest(state: TracksEditingState) {
        TracksEditingScreen(
            state = state,
            onChangeSelection = mockOnChangeSelection,
            onDeleteConfirm = mockOnDeleteConfirm,
            onBack = mockOnBack,
        )
    }
}
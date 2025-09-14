package io.mityukov.geo.tracking.feature.map

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.core.test.AppTestTag
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class MapControlsTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var mockOnNavigateTo: (Geolocation) -> Unit
    private lateinit var mockOnZoomIn: () -> Unit
    private lateinit var mockOnZoomOut: () -> Unit

    @Before
    fun setUp() {
        mockOnNavigateTo = mock()
        mockOnZoomIn = mock()
        mockOnZoomOut = mock()
    }

    @Test
    fun initialState() {
        composeTestRule.setContent {
            MapControls(
                onNavigateTo = mockOnNavigateTo,
                onZoomIn = mockOnZoomIn,
                onZoomOut = mockOnZoomOut,
                currentGeolocation = null
            )
        }

        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_ZOOM_IN)
            .assertIsDisplayed()
            .assertHasClickAction()
        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_ZOOM_OUT)
            .assertIsDisplayed()
            .assertHasClickAction()
        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_NAVIGATE_TO_LOCATION)
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun init_withGeolocation_onNavigateToCalled() {
        composeTestRule.setContent {
            MapControls(
                onNavigateTo = mockOnNavigateTo,
                onZoomIn = mockOnZoomIn,
                onZoomOut = mockOnZoomOut,
                currentGeolocation = Geolocation.empty()
            )
        }

        verify(mockOnNavigateTo).invoke(Geolocation.empty())
        verifyNoInteractions(mockOnZoomIn)
        verifyNoInteractions(mockOnZoomOut)
    }

    @Test
    fun init_withoutGeolocation_onNavigateToNotCalled() {
        composeTestRule.setContent {
            MapControls(
                onNavigateTo = mockOnNavigateTo,
                onZoomIn = mockOnZoomIn,
                onZoomOut = mockOnZoomOut,
                currentGeolocation = null
            )
        }

        verifyNoInteractions(mockOnNavigateTo)
        verifyNoInteractions(mockOnZoomIn)
        verifyNoInteractions(mockOnZoomOut)
    }

    @Test
    fun zoomInClicked_onZoomInCalled() {
        composeTestRule.setContent {
            MapControls(
                onNavigateTo = mockOnNavigateTo,
                onZoomIn = mockOnZoomIn,
                onZoomOut = mockOnZoomOut,
                currentGeolocation = null
            )
        }

        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_ZOOM_IN).performClick()
        verify(mockOnZoomIn).invoke()
        verifyNoInteractions(mockOnZoomOut)
        verifyNoInteractions(mockOnNavigateTo)
    }

    @Test
    fun zoomOutClicked_onZoomOutCalled() {
        composeTestRule.setContent {
            MapControls(
                onNavigateTo = mockOnNavigateTo,
                onZoomIn = mockOnZoomIn,
                onZoomOut = mockOnZoomOut,
                currentGeolocation = null
            )
        }

        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_ZOOM_OUT).performClick()
        verify(mockOnZoomOut).invoke()
        verifyNoInteractions(mockOnZoomIn)
        verifyNoInteractions(mockOnNavigateTo)
    }

    @Test
    fun navigateToClicked_currentLocationIsNull_onNavigateToNotCalled() {
        composeTestRule.setContent {
            MapControls(
                onNavigateTo = mockOnNavigateTo,
                onZoomIn = mockOnZoomIn,
                onZoomOut = mockOnZoomOut,
                currentGeolocation = null
            )
        }

        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_NAVIGATE_TO_LOCATION).performClick()
        verifyNoInteractions(mockOnZoomOut)
        verifyNoInteractions(mockOnZoomIn)
        verifyNoInteractions(mockOnNavigateTo)
    }

    @Test
    fun navigateToClicked_currentLocationIsNotNull_onNavigateToCalled() {
        composeTestRule.setContent {
            MapControls(
                onNavigateTo = mockOnNavigateTo,
                onZoomIn = mockOnZoomIn,
                onZoomOut = mockOnZoomOut,
                currentGeolocation = Geolocation.empty()
            )
        }

        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_NAVIGATE_TO_LOCATION).performClick()
        verifyNoInteractions(mockOnZoomOut)
        verifyNoInteractions(mockOnZoomIn)
        verify(mockOnNavigateTo, times(2)).invoke(Geolocation.empty())
    }

    @Test
    fun launchedEffectRunsOnlyOnce() {
        val firstLocation = Geolocation.empty()
        val secondLocation = Geolocation.empty().copy(time = 1)
        val state = mutableStateOf(firstLocation)
        composeTestRule.setContent {
            val state = remember { state }
            MapControls(
                onNavigateTo = mockOnNavigateTo,
                onZoomIn = mockOnZoomIn,
                onZoomOut = mockOnZoomOut,
                currentGeolocation = state.value
            )
        }
        composeTestRule.waitForIdle()
        state.value = secondLocation
        composeTestRule.waitForIdle()
        verify(mockOnNavigateTo).invoke(firstLocation)
        verify(mockOnNavigateTo, times(1)).invoke(any())
    }
}
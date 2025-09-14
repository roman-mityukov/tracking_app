package io.mityukov.geo.tracking.feature.settings

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import io.mityukov.geo.tracking.core.test.AppTestTag
import io.mityukov.geo.tracking.feature.settings.geolocation.GeolocationUpdatesIntervalState
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class AppSettingsTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()
    private lateinit var mockOnBack: () -> Unit
    private lateinit var mockOnInstructionsSelect: () -> Unit
    private lateinit var mockOnIntervalSelect: (Duration) -> Unit

    @Before
    fun setUp() {
        mockOnBack = mock()
        mockOnInstructionsSelect = mock()
        mockOnIntervalSelect = mock()
    }

    @Test
    fun backButtonClicked_onBackCalled() {
        composeTestRule.setContent {
            AppSettingsScreenUnderTest()
        }

        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_BACK).performClick()
        verify(mockOnBack).invoke()
        verifyNoInteractions(mockOnIntervalSelect)
        verifyNoInteractions(mockOnInstructionsSelect)
    }

    @Test
    fun instructionsButtonClicked_onInstructionsSelectCalled() {
        composeTestRule.setContent {
            AppSettingsScreenUnderTest()
        }

        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_INSTRUCTIONS).performClick()
        verify(mockOnInstructionsSelect).invoke()
        verifyNoInteractions(mockOnIntervalSelect)
        verifyNoInteractions(mockOnBack)
    }

    @Test
    fun intervalButtonClicked_onIntervalSelectCalled() {
        composeTestRule.setContent {
            AppSettingsScreenUnderTest()
        }

        composeTestRule.onNodeWithTag(AppTestTag.DROPDOWN_GEOLOCATIONS_UPDATES_INTERVAL)
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithTag(AppTestTag.DROPDOWN_ITEM_GEOLOCATIONS_UPDATES_INTERVAL)
            .onFirst().performClick()

        verify(mockOnIntervalSelect).invoke(3.seconds)
        verifyNoInteractions(mockOnInstructionsSelect)
        verifyNoInteractions(mockOnBack)
    }

    @Composable
    fun AppSettingsScreenUnderTest(modifier: Modifier = Modifier.Companion) {
        AppSettingsScreen(
            geolocationUpdatesIntervalState = GeolocationUpdatesIntervalState.Data(
                3.seconds,
                listOf(3.seconds, 4.seconds)
            ),
            onInstructionsSelect = mockOnInstructionsSelect,
            onIntervalSelect = mockOnIntervalSelect,
            onBack = mockOnBack,
        )
    }
}
package io.mityukov.geo.tracking.feature.profile

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import io.mityukov.geo.tracking.core.test.AppTestTag
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class ProfileScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var mockOnSettingsSelected: () -> Unit
    private lateinit var mockOnAboutSelected: () -> Unit

    @Before
    fun setUp() {
        mockOnSettingsSelected = mock()
        mockOnAboutSelected = mock()
    }

    @Test
    fun initialState() {
        composeTestRule.setContent {
            ProfileScreen(
                onSettingsSelected = mockOnSettingsSelected,
                onAboutSelected = mockOnAboutSelected,
            )
        }

        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_SETTINGS).assertIsDisplayed()
        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_ABOUT).assertIsDisplayed()
    }

    @Test
    fun clickButtonSettings_onSettingCalled() {
        composeTestRule.setContent {
            ProfileScreen(
                onSettingsSelected = mockOnSettingsSelected,
                onAboutSelected = mockOnAboutSelected,
            )
        }

        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_SETTINGS).performClick()
        verify(mockOnSettingsSelected).invoke()
        verifyNoInteractions(mockOnAboutSelected)
    }

    @Test
    fun clickButtonAbout_onAboutCalled() {
        composeTestRule.setContent {
            ProfileScreen(
                onSettingsSelected = mockOnSettingsSelected,
                onAboutSelected = mockOnAboutSelected,
            )
        }

        composeTestRule.onNodeWithTag(AppTestTag.BUTTON_ABOUT).performClick()
        verify(mockOnAboutSelected).invoke()
        verifyNoInteractions(mockOnSettingsSelected)
    }
}
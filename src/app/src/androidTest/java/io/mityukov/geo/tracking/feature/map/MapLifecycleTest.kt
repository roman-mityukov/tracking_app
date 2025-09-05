package io.mityukov.geo.tracking.feature.map

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class MapLifecycleTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()
    private lateinit var mockOnResume: () -> Unit
    private lateinit var mockOnStart: () -> Unit
    private lateinit var mockOnStop: () -> Unit

    @Before
    fun setUp() {
        mockOnStop = mock()
        mockOnStart = mock()
        mockOnResume = mock()
    }

    @Test
    fun onStart_whenLifecycleOwnerStart_called() {
        composeTestRule.setContent {
            MapLifecycle(
                onStart = mockOnStart,
                onStop = mockOnStop,
                onResume = mockOnResume
            )
        }

        verify(mockOnStart).invoke()
        verify(mockOnResume).invoke()
        verifyNoInteractions(mockOnStop)
    }

    @Test
    fun onStop_whenLifecycleOwnerStop_called() {
        val testLifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides testLifecycleOwner) {
                MapLifecycle(
                    onStart = mockOnStart,
                    onStop = mockOnStop,
                    onResume = mockOnResume,
                )
            }
        }

        testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        verify(mockOnStop).invoke()
    }
}
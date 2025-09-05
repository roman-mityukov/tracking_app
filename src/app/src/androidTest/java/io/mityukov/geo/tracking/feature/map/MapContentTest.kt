package io.mityukov.geo.tracking.feature.map

import android.content.Context
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import org.junit.Before
import org.junit.Rule


class MapContentTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
    }
}

@Composable
private fun MapContentUnderTest(
    onUpdateCurrentLocation: (Geolocation) -> Unit = { _ -> },
    onSharingError: () -> Unit = {},
    onPendingLocation: () -> Unit = {},
    onPendingLocationComplete: () -> Unit = {},
    mapViewFactory: (Context) -> View = { context -> View(context) },
    viewModelState: MapState,
) {
    MapContent(
        onUpdateCurrentLocation = onUpdateCurrentLocation,
        onSharing = onSharingError,
        onPendingLocation = onPendingLocation,
        onPendingLocationComplete = onPendingLocationComplete,
        mapViewFactory = mapViewFactory,
        mapViewModelState = viewModelState,
    )
}
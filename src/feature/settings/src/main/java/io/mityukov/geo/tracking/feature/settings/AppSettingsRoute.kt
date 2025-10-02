package io.mityukov.geo.tracking.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.mityukov.geo.tracking.core.designsystem.component.ButtonBack
import io.mityukov.geo.tracking.feature.settings.geolocation.GeolocationUpdatesIntervalEvent
import io.mityukov.geo.tracking.feature.settings.geolocation.GeolocationUpdatesIntervalState
import io.mityukov.geo.tracking.feature.settings.geolocation.GeolocationUpdatesIntervalView
import io.mityukov.geo.tracking.feature.settings.geolocation.GeolocationUpdatesIntervalViewModel
import io.mityukov.geo.tracking.feature.settings.instructions.InstructionsView
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun AppSettingsRoute(
    geolocationUpdatesIntervalViewModel: GeolocationUpdatesIntervalViewModel = hiltViewModel(),
    onInstructionsSelected: () -> Unit,
    onBack: () -> Unit,
) {
    val geolocationUpdatesIntervalState =
        geolocationUpdatesIntervalViewModel.stateFlow.collectAsStateWithLifecycle()
    AppSettingsScreen(
        geolocationUpdatesIntervalState = geolocationUpdatesIntervalState.value,
        onInstructionsSelect = onInstructionsSelected,
        onIntervalSelect = { interval ->
            geolocationUpdatesIntervalViewModel.add(
                GeolocationUpdatesIntervalEvent.SelectInterval(
                    interval
                )
            )
        },
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    geolocationUpdatesIntervalState: GeolocationUpdatesIntervalState,
    onInstructionsSelect: () -> Unit,
    onIntervalSelect: (Duration) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.feature_settings_title)) },
                navigationIcon = {
                    ButtonBack(onBack = onBack)
                }
            )
        },
        contentWindowInsets = WindowInsets.safeContent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .consumeWindowInsets(paddingValues)
        ) {
            InstructionsView(onInstructionsSelect)
            GeolocationUpdatesIntervalView(
                state = geolocationUpdatesIntervalState,
                onIntervalSelect = onIntervalSelect
            )
        }
    }
}

@Preview
@Composable
fun AppSettingsPreview() {
    AppSettingsScreen(
        geolocationUpdatesIntervalState = GeolocationUpdatesIntervalState.Data(
            interval = 3.seconds,
            availableIntervals = listOf(3.seconds, 5.seconds)
        ),
        onInstructionsSelect = {},
        onIntervalSelect = {},
        onBack = {}
    )
}

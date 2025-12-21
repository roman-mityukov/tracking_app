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
import io.mityukov.geo.tracking.feature.settings.geolocation.GeolocationSettingsEvent
import io.mityukov.geo.tracking.feature.settings.geolocation.GeolocationSettingsState
import io.mityukov.geo.tracking.feature.settings.geolocation.GeolocationSettingsView
import io.mityukov.geo.tracking.feature.settings.geolocation.GeolocationSettingsViewModel
import io.mityukov.geo.tracking.feature.settings.instructions.InstructionsView
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun AppSettingsPane(
    geolocationSettingsViewModel: GeolocationSettingsViewModel = hiltViewModel(),
    onInstructionsSelected: () -> Unit,
    onBack: () -> Unit,
) {
    val geolocationUpdatesIntervalState =
        geolocationSettingsViewModel.stateFlow.collectAsStateWithLifecycle()
    AppSettingsContent(
        geolocationSettingsState = geolocationUpdatesIntervalState.value,
        onInstructionsSelect = onInstructionsSelected,
        onIntervalSelect = { interval ->
            geolocationSettingsViewModel.add(
                GeolocationSettingsEvent.SelectInterval(interval)
            )
        },
        onAccuracySelect = { accuracy ->
            geolocationSettingsViewModel.add(
                GeolocationSettingsEvent.SelectAccuracy(accuracy)
            )
        },
        onSpeedSelect = { velocity ->
            geolocationSettingsViewModel.add(
                GeolocationSettingsEvent.SelectSpeed(velocity)
            )
        },
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppSettingsContent(
    geolocationSettingsState: GeolocationSettingsState,
    onInstructionsSelect: () -> Unit,
    onIntervalSelect: (Duration) -> Unit,
    onAccuracySelect: (Int) -> Unit,
    onSpeedSelect: (Int) -> Unit,
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
            GeolocationSettingsView(
                state = geolocationSettingsState,
                onIntervalSelect = onIntervalSelect,
                onAcceptableAccuracySelect = onAccuracySelect,
                onAcceptableSpeedSelect = onSpeedSelect,
            )
        }
    }
}

@Preview
@Composable
internal fun AppSettingsContentPreview() {
    AppSettingsContent(
        geolocationSettingsState = GeolocationSettingsState.Data(
            interval = 3.seconds,
            availableIntervals = listOf(3.seconds, 5.seconds),
            accuracy = 0,
            availableAccuracy = listOf(0, 10),
            acceptableSpeed = 10,
            availableAcceptableSpeed = listOf(0, 10),
        ),
        onInstructionsSelect = {},
        onIntervalSelect = {},
        onAccuracySelect = {},
        onSpeedSelect = {},
        onBack = {}
    )
}

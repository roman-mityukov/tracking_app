package io.mityukov.geo.tracking.feature.settings.geolocation

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.mityukov.geo.tracking.core.test.AppTestTag
import io.mityukov.geo.tracking.feature.settings.R
import kotlin.time.Duration

@Composable
internal fun GeolocationSettingsView(
    state: GeolocationSettingsState,
    onIntervalSelect: (Duration) -> Unit,
    onAcceptableAccuracySelect: (Int) -> Unit,
    onAcceptableSpeedSelect: (Int) -> Unit,
) {
    when (state) {
        is GeolocationSettingsState.Data -> {
            Text(
                modifier = Modifier.padding(all = 16.dp),
                text = stringResource(R.string.feature_settings_geolocation_alert_label),
                color = Color.Red,
            )
            GeolocationSettingDropdownMenu(
                label = stringResource(R.string.feature_settings_geolocation_updates_rate_label),
                parentTestTag = AppTestTag.DROPDOWN_GEOLOCATIONS_UPDATES_INTERVAL,
                itemTestTag = AppTestTag.DROPDOWN_ITEM_GEOLOCATIONS_UPDATES_INTERVAL,
                currentValue = state.interval,
                availableValues = state.availableIntervals,
                onSelect = { value ->
                    onIntervalSelect(value)
                }
            )
            GeolocationSettingDropdownMenu(
                label = stringResource(R.string.feature_settings_geolocation_acceptable_accuracy_label),
                parentTestTag = AppTestTag.DROPDOWN_GEOLOCATIONS_ACCURACY,
                itemTestTag = AppTestTag.DROPDOWN_ITEM_GEOLOCATIONS_ACCURACY,
                currentValue = state.accuracy,
                availableValues = state.availableAccuracy,
                onSelect = { value ->
                    onAcceptableAccuracySelect(value)
                }
            )
            GeolocationSettingDropdownMenu(
                label = stringResource(R.string.feature_settings_geolocation_acceptable_speed_label),
                parentTestTag = AppTestTag.DROPDOWN_GEOLOCATIONS_SPEED,
                itemTestTag = AppTestTag.DROPDOWN_ITEM_GEOLOCATIONS_SPEED,
                currentValue = state.acceptableSpeed,
                availableValues = state.availableAcceptableSpeed,
                onSelect = { value ->
                    onAcceptableSpeedSelect(value)
                }
            )
        }

        GeolocationSettingsState.Pending -> {
            // no op
        }
    }
}

@Composable
private fun <T> GeolocationSettingDropdownMenu(
    label: String,
    parentTestTag: String,
    itemTestTag: String,
    currentValue: T,
    availableValues: List<T>,
    onSelect: (T) -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(
                text = label
            )
        },
        trailingContent = {
            var expanded by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .wrapContentSize(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = currentValue.toString(),
                    modifier = Modifier
                        .testTag(parentTestTag)
                        .clickable { expanded = true }
                        .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                        .padding(8.dp)
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    availableValues.forEach { value ->
                        DropdownMenuItem(
                            modifier = Modifier.testTag(itemTestTag),
                            text = { Text(value.toString()) },
                            onClick = {
                                expanded = false
                                onSelect(value)
                            }
                        )
                    }
                }
            }
        },
    )
}

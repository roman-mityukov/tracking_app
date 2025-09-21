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
internal fun GeolocationUpdatesIntervalView(
    state: GeolocationUpdatesIntervalState,
    onIntervalSelect: (Duration) -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(R.string.feature_settings_geolocation_updates_rate_label)
            )
        },
        trailingContent = {
            when (state) {
                is GeolocationUpdatesIntervalState.Data -> {
                    val selectedInterval = state.interval
                    val availableIntervals = state.availableIntervals
                    var expanded by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .wrapContentSize(Alignment.TopStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = selectedInterval.inWholeSeconds.toString(),
                            modifier = Modifier
                                .testTag(AppTestTag.DROPDOWN_GEOLOCATIONS_UPDATES_INTERVAL)
                                .clickable { expanded = true }
                                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                                .padding(8.dp)
                        )

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            availableIntervals.forEach { interval ->
                                DropdownMenuItem(
                                    modifier = Modifier.testTag(AppTestTag.DROPDOWN_ITEM_GEOLOCATIONS_UPDATES_INTERVAL),
                                    text = { Text(interval.inWholeSeconds.toString()) },
                                    onClick = {
                                        expanded = false
                                        onIntervalSelect(interval)
                                    }
                                )
                            }
                        }
                    }
                }

                GeolocationUpdatesIntervalState.Pending -> {
                    // no op
                }
            }
        }
    )
}

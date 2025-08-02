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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.mityukov.geo.tracking.R

@Composable
fun GeolocationUpdatesIntervalView(viewModel: GeolocationUpdatesIntervalViewModel = hiltViewModel()) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    ListItem(
        headlineContent = {
            Text(
                text = stringResource(R.string.geolocation_updates_rate_label)
            )
        },
        trailingContent = {
            when (state) {
                is GeolocationUpdatesIntervalState.Data -> {
                    val selectedInterval = (state as GeolocationUpdatesIntervalState.Data).interval
                    val availableIntervals = (state as GeolocationUpdatesIntervalState.Data).availableIntervals
                    var expanded by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .wrapContentSize(Alignment.TopStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = selectedInterval.inWholeSeconds.toString(),
                            modifier = Modifier
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
                                    text = { Text(interval.inWholeSeconds.toString()) },
                                    onClick = {
                                        expanded = false
                                        viewModel.add(
                                            GeolocationUpdatesIntervalEvent.SelectInterval(
                                                interval
                                            )
                                        )
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

package io.mityukov.geo.tracking.feature.track.capture

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.mityukov.geo.tracking.R
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackCaptureStatus

@Composable
fun TrackCaptureView(viewModel: TrackCaptureViewModel = hiltViewModel()) {
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (state.value.status is TrackCaptureStatus.Run) {
            ButtonStopTrackCapture(onClick = { viewModel.add(TrackCaptureEvent.StopCapture) })
            Spacer(Modifier.width(8.dp))
        }
        ButtonStartTrackCapture(viewModel = viewModel)
    }
}

@Composable
private fun ButtonStopTrackCapture(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier.size(48.dp),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
    ) {
        Icon(
            painterResource(R.drawable.icon_stop),
            contentDescription = stringResource(R.string.content_description_map_stop_track),
        )
    }
}

@Composable
private fun ButtonStartTrackCapture(modifier: Modifier = Modifier, viewModel: TrackCaptureViewModel) {
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()
    val trackCaptureStatus = state.value.status

    IconToggleButton(
        modifier = modifier.size(64.dp),
        checked = trackCaptureStatus is TrackCaptureStatus.Run,
        onCheckedChange = {
            if ((trackCaptureStatus is TrackCaptureStatus.Run).not()) {
                viewModel.add(TrackCaptureEvent.StartCapture)
            } else {
                if (trackCaptureStatus.trackInProgress.paused) {
                    viewModel.add(TrackCaptureEvent.PlayCapture)
                } else {
                    viewModel.add(TrackCaptureEvent.PauseCapture)
                }
            }
        },
        colors = IconButtonDefaults.iconToggleButtonColors().copy(
            containerColor = Color.Red,
            contentColor = ButtonDefaults.buttonColors().contentColor,
            checkedContainerColor = Color.Red,
            checkedContentColor = ButtonDefaults.buttonColors().contentColor,
        )
    ) {
        if (trackCaptureStatus is TrackCaptureStatus.Run) {
            if (trackCaptureStatus.trackInProgress.paused) {
                Icon(
                    painterResource(R.drawable.icon_play),
                    contentDescription = stringResource(R.string.content_description_map_resume_track),
                )
            } else {
                Icon(
                    painterResource(R.drawable.icon_pause),
                    contentDescription = stringResource(R.string.content_description_map_pause_track),
                )
            }
        } else {
            Icon(
                painterResource(R.drawable.home_track_filled),
                contentDescription = stringResource(R.string.content_description_map_start_track)
            )
        }
    }
}

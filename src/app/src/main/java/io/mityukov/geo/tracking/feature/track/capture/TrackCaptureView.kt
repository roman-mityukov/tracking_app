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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.mityukov.geo.tracking.R
import io.mityukov.geo.tracking.core.data.repository.track.TrackCaptureStatus

@Composable
fun TrackCaptureView(viewModel: TrackCaptureViewModel = hiltViewModel()) {
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (state.value.status is TrackCaptureStatus.Run) {
            Button(
                onClick = {
                    viewModel.add(TrackCaptureEvent.StopCapture)
                },
                Modifier.size(48.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
            ) {
                Icon(painterResource(R.drawable.icon_stop), contentDescription = null)
            }
            Spacer(Modifier.width(8.dp))
        }
        IconToggleButton(
            modifier = Modifier.size(64.dp),
            checked = state.value.status is TrackCaptureStatus.Run,
            onCheckedChange = {
                if ((state.value.status is TrackCaptureStatus.Run).not()) {
                    viewModel.add(TrackCaptureEvent.StartCapture)
                } else {
                    if ((state.value.status as TrackCaptureStatus.Run).paused) {
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
            if (state.value.status is TrackCaptureStatus.Run) {
                if ((state.value.status as TrackCaptureStatus.Run).paused) {
                    Icon(painterResource(R.drawable.icon_play), contentDescription = null)
                } else {
                    Icon(painterResource(R.drawable.icon_pause), contentDescription = null)
                }
            } else {
                Icon(painterResource(R.drawable.home_track_filled), contentDescription = null)
            }
        }
    }
}

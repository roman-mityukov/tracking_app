package io.mityukov.geo.tracking.feature.track.capture

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.mityukov.geo.tracking.R
import io.mityukov.geo.tracking.core.data.repository.track.TrackCaptureStatus

@Composable
fun TrackCaptureView(viewModel: TrackCaptureViewModel = hiltViewModel()) {
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()

    var checked: Boolean = state.value.status is TrackCaptureStatus.Running

    IconToggleButton(
        checked = checked,
        onCheckedChange = {
            viewModel.add(TrackCaptureEvent.Switch)
        },
        colors = IconButtonDefaults.iconToggleButtonColors().copy(
            containerColor = ButtonDefaults.buttonColors().containerColor,
            contentColor = ButtonDefaults.buttonColors().contentColor,
            checkedContainerColor = Color.Red,
            checkedContentColor = ButtonDefaults.buttonColors().contentColor,
        )
    ) {
        Icon(painterResource(R.drawable.home_track_outlined), contentDescription = null)
    }
}
package io.mityukov.geo.tracking.feature.track.capture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import io.mityukov.geo.tracking.core.common.time.TimeUtils
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackCaptureStatus
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackInProgress
import io.mityukov.geo.tracking.core.designsystem.icon.AppIcons
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.core.test.AppTestTag
import io.mityukov.geo.tracking.core.ui.FontScalePreviews
import io.mityukov.geo.tracking.core.ui.TrackProperties
import io.mityukov.geo.tracking.core.ui.UiProps

@Composable
fun TrackCapture(
    modifier: Modifier = Modifier,
    trackCaptureState: TrackCaptureState,
    onStartCapture: () -> Unit,
    onStopCapture: () -> Unit,
    onPauseCapture: () -> Unit,
    onPlayCapture: () -> Unit,
    onUpdateTrack: (List<Geolocation>) -> Unit,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.End) {
        if (trackCaptureState.status is TrackCaptureStatus.Run) {
            LaunchedEffect(trackCaptureState.geolocations.size) {
                onUpdateTrack(trackCaptureState.geolocations)
            }

            CurrentTrack(trackInProgress = trackCaptureState.status.trackInProgress)
            Spacer(modifier = Modifier.height(8.dp))
        } else if (trackCaptureState.status is TrackCaptureStatus.Error) {
            CurrentTrackError()
            Spacer(modifier = Modifier.height(8.dp))
        }
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (trackCaptureState.status is TrackCaptureStatus.Run) {
                ButtonStopTrackCapture(onClick = onStopCapture)
                Spacer(Modifier.width(8.dp))
            }
            ButtonStartTrackCapture(
                viewModelState = trackCaptureState,
                onStartCapture = onStartCapture,
                onPlayCapture = onPlayCapture,
                onPauseCapture = onPauseCapture,
            )
        }
    }
}

@Composable
private fun ButtonStopTrackCapture(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier
            .size(48.dp)
            .testTag(AppTestTag.BUTTON_STOP_TRACK_CAPTURE),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
    ) {
        Icon(
            imageVector = AppIcons.Stop,
            contentDescription = stringResource(R.string.feature_track_capture_content_description_stop),
        )
    }
}

@Composable
private fun ButtonStartTrackCapture(
    modifier: Modifier = Modifier,
    viewModelState: TrackCaptureState,
    onStartCapture: () -> Unit,
    onPlayCapture: () -> Unit,
    onPauseCapture: () -> Unit,
) {
    val trackCaptureStatus = viewModelState.status

    IconToggleButton(
        modifier = modifier
            .size(64.dp)
            .testTag(AppTestTag.BUTTON_START_TRACK_CAPTURE),
        checked = trackCaptureStatus is TrackCaptureStatus.Run,
        onCheckedChange = {
            if ((trackCaptureStatus is TrackCaptureStatus.Run).not()) {
                onStartCapture()
            } else {
                if (trackCaptureStatus.trackInProgress.paused) {
                    onPlayCapture()
                } else {
                    onPauseCapture()
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
                    imageVector = AppIcons.Play,
                    contentDescription = stringResource(R.string.feature_track_capture_content_description_resume),
                )
            } else {
                Icon(
                    imageVector = AppIcons.Pause,
                    contentDescription = stringResource(R.string.feature_track_capture_content_description_pause),
                )
            }
        } else {
            Icon(
                imageVector = AppIcons.Track,
                contentDescription = stringResource(R.string.feature_track_capture_content_description_start)
            )
        }
    }
}

@Composable
private fun CurrentTrack(
    modifier: Modifier = Modifier,
    trackInProgress: TrackInProgress,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                InProgressTrackHeadline(
                    startTime = trackInProgress.start,
                    paused = trackInProgress.paused
                )
                TrackProperties(
                    duration = trackInProgress.duration,
                    distance = trackInProgress.distance,
                    altitudeUp = trackInProgress.altitudeUp,
                    altitudeDown = trackInProgress.altitudeDown,
                    speed = trackInProgress.currentSpeed,
                )
            }
        }
    }
}

@Composable
private fun InProgressTrackHeadline(
    modifier: Modifier = Modifier,
    startTime: Long,
    paused: Boolean
) {
    val formattedStartTime =
        TimeUtils.getFormattedLocalFromUTC(startTime, UiProps.DEFAULT_DATE_TIME_FORMATTER)

    Text(
        modifier = modifier,
        text = buildAnnotatedString {
            append("$formattedStartTime ")
            withStyle(
                style = SpanStyle(
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                )
            ) {
                if (paused) {
                    append(stringResource(R.string.feature_track_capture_title_pause))
                } else {
                    append(stringResource(R.string.feature_track_capture_title_capturing))
                }
            }
        })
}

@Composable
private fun CurrentTrackError(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.feature_track_capture_error),
                style = TextStyle(color = Color.Red, fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Preview
@FontScalePreviews
@Composable
fun TrackCaptureControlsPreview(@PreviewParameter(TrackCaptureStateProvider::class) state: TrackCaptureState) {
    TrackCapture(
        trackCaptureState = state,
        onStartCapture = {},
        onStopCapture = {},
        onPlayCapture = {},
        onPauseCapture = {},
        onUpdateTrack = {},
    )
}

private class TrackCaptureStateProvider : PreviewParameterProvider<TrackCaptureState> {
    override val values: Sequence<TrackCaptureState> = sequenceOf(
        TrackCaptureState(status = TrackCaptureStatus.Idle),
        TrackCaptureState(status = TrackCaptureStatus.Error),
        TrackCaptureState(status = TrackCaptureStatus.Run(TrackInProgress.empty())),
        TrackCaptureState(
            status = TrackCaptureStatus.Run(
                TrackInProgress.empty().copy(paused = true)
            )
        ),
    )
}

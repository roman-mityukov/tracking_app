package io.mityukov.geo.tracking.feature.track.list

import android.text.format.DateUtils
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.mityukov.geo.tracking.R
import io.mityukov.geo.tracking.app.AppProps
import io.mityukov.geo.tracking.core.model.track.Track
import io.mityukov.geo.tracking.utils.time.TimeUtils
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TracksRoute(
    viewModel: TracksViewModel = hiltViewModel(),
    onNavigateToTrack: (String) -> Unit,
    onNavigateToTracksEditing: (String) -> Unit,
) {
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()

    TrackList(
        state = state.value,
        onClick = {
            onNavigateToTrack(it)
        },
        onLongPress = {
            onNavigateToTracksEditing(it)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackList(
    modifier: Modifier = Modifier,
    state: TracksState,
    onClick: (String) -> Unit,
    onLongPress: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.tracks_title)) },
            )
        },
    ) { paddingValues ->
        when (state) {
            is TracksState.Data -> {
                val tracks = state.tracks

                LazyColumn(
                    modifier = modifier
                        .testTag("TracksLazyColumn")
                        .padding(paddingValues),
                ) {
                    if (tracks.isEmpty()) {
                        items(count = 1) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(stringResource(R.string.tracks_empty_list_message))
                                }
                            }
                        }
                    } else {
                        items(items = tracks, key = { track -> track.id }) {
                            TrackItem(
                                track = it,
                                onClick = onClick,
                                onLongPress = onLongPress,
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }

            TracksState.Pending -> {
                Box(
                    modifier = modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = modifier)
                }
            }
        }
    }
}

@Composable
private fun TrackItem(
    modifier: Modifier = Modifier,
    track: Track,
    onClick: (String) -> Unit,
    onLongPress: (String) -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    ListItem(
        modifier = modifier
            .testTag("TrackItem")
            .combinedClickable(
                enabled = true,
                onClick = {
                    onClick(track.id)
                },
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress(track.id)
                }
            ),
        headlineContent = {
            CompletedTrackHeadline(startTime = track.start)
        },
        supportingContent = {
            TrackProperties(
                duration = track.duration,
                distance = track.distance,
                altitudeUp = track.altitudeUp,
                altitudeDown = track.altitudeDown,
                speed = track.averageSpeed,
            )
        },
    )
}


@Composable
fun CompletedTrackHeadline(
    modifier: Modifier = Modifier,
    startTime: Long,
) {
    val formattedStartTime =
        TimeUtils.getFormattedLocalFromUTC(startTime, AppProps.UI_DATE_TIME_FORMATTER)

    Text(modifier = modifier, text = formattedStartTime)
}

@Composable
fun TrackItemProperty(
    modifier: Modifier = Modifier,
    iconResource: Int,
    text: String,
    contentDescription: String
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(iconResource),
            contentDescription = contentDescription
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, fontSize = 12.sp, overflow = TextOverflow.Clip)
        Spacer(modifier = Modifier.width(16.dp))
    }
}

@Composable
fun TrackProperties(
    modifier: Modifier = Modifier,
    duration: Duration,
    distance: Float,
    altitudeUp: Float,
    altitudeDown: Float,
    speed: Float,
) {
    FlowRow(modifier = modifier) {
        TrackItemProperty(
            iconResource = R.drawable.icon_duration,
            text = DateUtils.formatElapsedTime(
                duration.inWholeSeconds
            ),
            contentDescription = stringResource(R.string.content_description_track_time),
        )
        TrackItemProperty(
            iconResource = R.drawable.icon_distance,
            text = "${distance.roundToInt()}м",
            contentDescription = stringResource(R.string.content_description_track_distance),
        )
        TrackItemProperty(
            iconResource = R.drawable.icon_altitude_up,
            text = "${altitudeUp.roundToInt()}м",
            contentDescription = stringResource(R.string.content_description_track_altitude_up),
        )
        TrackItemProperty(
            iconResource = R.drawable.icon_altitude_down,
            text = "${altitudeDown.roundToInt()}м",
            contentDescription = stringResource(R.string.content_description_track_altitude_down),
        )
        TrackItemProperty(
            iconResource = R.drawable.icon_speed,
            text = "${String.format(Locale.getDefault(), "%.2f", speed)}м/с",
            contentDescription = stringResource(R.string.content_description_track_average_speed),
        )
    }
}

@Preview
@Composable
fun TrackListPreview(@PreviewParameter(TracksStateProvider::class) state: TracksState) {
    TrackList(
        state = state,
        onClick = {},
        onLongPress = {}
    )
}

class TracksStateProvider : PreviewParameterProvider<TracksState> {
    override val values: Sequence<TracksState> = sequenceOf(
        TracksState.Data(
            listOf(
                Track(
                    id = "49defd14-ae28-4705-9334-59761914de0c",
                    name = "Тестовый трек 1",
                    start = 1757038748000,
                    duration = 78.seconds,
                    end = 1757038758000,
                    distance = 1547f,
                    altitudeUp = 32f,
                    altitudeDown = 12f,
                    sumSpeed = 256f,
                    maxSpeed = 1.2f,
                    minSpeed = 1.0f,
                    geolocationCount = 10,
                    filePath = "",
                ),
                Track(
                    id = "87f958b4-9d10-400f-8c12-19f650bc7db4",
                    name = "Тестовый трек 2",
                    start = 1757038798000,
                    duration = 135.seconds,
                    end = 1757038858000,
                    distance = 25645f,
                    altitudeUp = 3200f,
                    altitudeDown = 1200f,
                    sumSpeed = 2560f,
                    maxSpeed = 1.5f,
                    minSpeed = 1.1f,
                    geolocationCount = 100,
                    filePath = "",
                )
            )
        ),
        TracksState.Data(listOf()),
        TracksState.Pending,
    )
}

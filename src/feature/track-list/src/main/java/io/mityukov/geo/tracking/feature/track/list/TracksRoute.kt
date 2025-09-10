package io.mityukov.geo.tracking.feature.track.list

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.mityukov.geo.tracking.core.common.time.TimeUtils
import io.mityukov.geo.tracking.core.model.track.Track
import io.mityukov.geo.tracking.core.test.AppTestTag
import io.mityukov.geo.tracking.core.ui.TrackProperties
import io.mityukov.geo.tracking.core.ui.UiProps
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
            .testTag(AppTestTag.TRACK_ITEM)
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
        TimeUtils.getFormattedLocalFromUTC(startTime, UiProps.DEFAULT_DATE_TIME_FORMATTER)

    Text(modifier = modifier, text = formattedStartTime)
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

package io.mityukov.geo.tracking.feature.track.list

import android.text.format.DateUtils
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.mityukov.geo.tracking.R
import io.mityukov.geo.tracking.app.AppProps
import io.mityukov.geo.tracking.core.model.track.Track
import kotlin.time.DurationUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TracksScreen(
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
private fun TrackList(
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
                val capturedTrackId = state.capturedTrackId

                LazyColumn(
                    modifier = modifier.padding(paddingValues)
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
                        items(items = tracks) {
                            TrackItem(
                                track = it,
                                isCapturedTrack = it.id == capturedTrackId,
                                paused = state.paused,
                                onClick = onClick,
                                onLongPress = onLongPress,
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }

            TracksState.Pending -> {
                CircularProgressIndicator(modifier = modifier)
            }
        }
    }
}

@Composable
fun TrackHeadline(
    modifier: Modifier = Modifier,
    track: Track,
    isCapturedTrack: Boolean,
    paused: Boolean
) {
    if (track.points.isNotEmpty()) {
        val startTime =
            track.points.first().geolocation.localDateTime.format(AppProps.UI_DATE_TIME_FORMATTER)

        if (isCapturedTrack) {
            Text(
                modifier = modifier,
                text = buildAnnotatedString {
                    append("$startTime ")
                    withStyle(
                        style = SpanStyle(
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                        )
                    ) {
                        if (paused) {
                            append(stringResource(R.string.tracks_item_title_pause))
                        } else {
                            append(stringResource(R.string.tracks_item_title_capturing))
                        }
                    }
                })
        } else {
            Text(modifier = modifier, text = startTime)
        }
    }
}

@Composable
fun TrackItemProperty(modifier: Modifier = Modifier, iconResource: Int, text: String, contentDescription: String) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(iconResource),
            contentDescription = contentDescription
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text)
        Spacer(modifier = Modifier.width(16.dp))
    }
}

@Composable
fun TrackProperties(modifier: Modifier = Modifier, track: Track) {
    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        TrackItemProperty(
            iconResource = R.drawable.icon_duration,
            text = DateUtils.formatElapsedTime(
                track.duration.toLong(
                    DurationUnit.SECONDS
                )
            ),
            contentDescription = stringResource(R.string.content_description_track_time),
        )
        TrackItemProperty(
            iconResource = R.drawable.icon_distance,
            text = "${track.distance}м",
            contentDescription = stringResource(R.string.content_description_track_distance),
        )
        TrackItemProperty(
            iconResource = R.drawable.icon_altitude_up,
            text = "${track.altitudeUp}",
            contentDescription = stringResource(R.string.content_description_track_altitude_up),
        )
        TrackItemProperty(
            iconResource = R.drawable.icon_altitude_down,
            text = "${track.altitudeDown}",
            contentDescription = stringResource(R.string.content_description_track_altitude_down),
        )
    }
}

@Composable
private fun TrackItem(
    modifier: Modifier = Modifier,
    track: Track,
    isCapturedTrack: Boolean,
    paused: Boolean,
    onClick: (String) -> Unit,
    onLongPress: (String) -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    ListItem(
        modifier = modifier.combinedClickable(
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
            TrackHeadline(track = track, isCapturedTrack = isCapturedTrack, paused = paused)
        },
        supportingContent = {
            TrackProperties(track = track)
        },
    )
}

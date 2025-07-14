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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.mityukov.geo.tracking.R
import io.mityukov.geo.tracking.core.model.track.Track
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
                    modifier = Modifier.padding(paddingValues)
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
                                onClick = onClick,
                                onLongPress = onLongPress,
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }

            TracksState.Pending -> {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun TrackHeadline(track: Track, isCapturedTrack: Boolean) {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
    val startTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(track.points.first().geolocation.time),
        ZoneId.systemDefault()
    ).format(formatter)

    if (isCapturedTrack) {
        Text(buildAnnotatedString {
            append("$startTime ")
            withStyle(
                style = SpanStyle(
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                )
            ) {
                append(stringResource(R.string.tracks_item_title_capturing))
            }
        })
    } else {
        Text(startTime)
    }
}

@Composable
fun TrackItemProperty(iconResource: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(iconResource),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text)
        Spacer(modifier = Modifier.width(16.dp))
    }
}

@Composable
fun TrackProperties(track: Track) {
    Row(verticalAlignment = Alignment.Bottom) {
        TrackItemProperty(
            R.drawable.icon_duration,
            DateUtils.formatElapsedTime(
                track.duration.toLong(
                    DurationUnit.SECONDS
                )
            ),
        )
        TrackItemProperty(R.drawable.icon_distance, "${track.distance}м")
        TrackItemProperty(R.drawable.icon_my_location, "${track.points.size}")
        TrackItemProperty(R.drawable.icon_altitude_up, "${track.altitudeUp}")
        TrackItemProperty(R.drawable.icon_altitude_down, "${track.altitudeDown}")
    }
}

@Composable
private fun TrackItem(
    track: Track,
    isCapturedTrack: Boolean,
    onClick: (String) -> Unit,
    onLongPress: (String) -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    ListItem(
        modifier = Modifier.combinedClickable(
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
            TrackHeadline(track, isCapturedTrack)
        },
        supportingContent = {
            TrackProperties(track)
        },
    )
}

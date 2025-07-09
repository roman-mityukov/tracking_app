package io.mityukov.geo.tracking.feature.track.list

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
fun TracksListScreen(
    viewModel: TracksListViewModel = hiltViewModel(),
    onTrackSelected: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.tracks_title)) },
            )
        },
    ) { paddingValues ->
        val state = viewModel.stateFlow.collectAsStateWithLifecycle()
        TrackList(
            modifier = Modifier.padding(paddingValues),
            isRefreshing = false,
            tracks = state.value.tracks,
            onClick = {
                onTrackSelected(it)
            },
            onRefresh = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackList(
    modifier: Modifier,
    isRefreshing: Boolean,
    tracks: List<Track>,
    onClick: (String) -> Unit,
    onRefresh: () -> Unit
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier
    ) {
        LazyColumn {
            if (tracks.isEmpty() && !isRefreshing) {
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
                    TrackItem(track = it, onClick = onClick)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
private fun TrackItemProperty(
    iconResource: Int,
    text: String,
) {
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
private fun TrackItem(track: Track, onClick: (String) -> Unit) {
    ListItem(
        modifier = Modifier.clickable(
            enabled = true,
            onClick = {
                onClick(track.id)
            },
        ),
        headlineContent = {
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
            Text(
                text = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(track.points.first().geolocation.time),
                    ZoneId.systemDefault()
                ).format(formatter),
            )
        },
        supportingContent = {
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
        },
    )
}
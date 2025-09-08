package io.mityukov.geo.tracking.feature.track.list.editing

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.mityukov.geo.tracking.R
import io.mityukov.geo.tracking.app.ui.ButtonBack
import io.mityukov.geo.tracking.app.ui.CommonAlertDialog
import io.mityukov.geo.tracking.core.model.track.Track
import io.mityukov.geo.tracking.feature.track.list.CompletedTrackHeadline
import io.mityukov.geo.tracking.feature.track.list.TrackProperties
import io.mityukov.geo.tracking.utils.test.AppTestTag
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TracksEditingRoute(
    viewModel: TracksEditingViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {

    val state = viewModel.stateFlow.collectAsStateWithLifecycle()

    TracksEditingScreen(
        state = state.value,
        onDeleteConfirm = {
            viewModel.add(TracksEditingEvent.Delete)
        },
        onChangeSelection = { id ->
            viewModel.add(TracksEditingEvent.ChangeSelection(id))
        },
        onBack = onBack,
    )
}

@Composable
fun TracksEditingScreen(
    state: TracksEditingState,
    onDeleteConfirm: () -> Unit,
    onChangeSelection: (String) -> Unit,
    onBack: () -> Unit,
) {
    when (state) {
        is TracksEditingState.Data -> {
            val openDeleteDialog = remember { mutableStateOf(false) }
            val allTracks = state.allTracks
            val selectedTracks = state.selectedTracks

            if (selectedTracks.isEmpty()) {
                LaunchedEffect(Unit) {
                    onBack()
                }
            } else {
                Scaffold(
                    topBar = {
                        TracksEditingTopBar(
                            onDeleteInit = {
                                openDeleteDialog.value = true
                            },
                            onBack = onBack,
                        )
                    },
                ) { paddingValues ->
                    LazyColumn(modifier = Modifier.padding(paddingValues)) {
                        items(items = allTracks, key = { track -> track.id }) { track ->
                            TrackItem(
                                track = track,
                                isSelected = selectedTracks.any { it.id == track.id },
                                onClick = {
                                    onChangeSelection(track.id)
                                })
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }

                if (openDeleteDialog.value) {
                    CommonAlertDialog(
                        modifier = Modifier.testTag(AppTestTag.DIALOG_DELETE),
                        onDismiss = {
                            openDeleteDialog.value = false
                        },
                        onConfirm = onDeleteConfirm,
                        dialogTitle = stringResource(R.string.tracks_editing_delete_dialog_title),
                        dialogText = stringResource(R.string.tracks_editing_delete_dialog_text)
                    )
                }
            }
        }

        TracksEditingState.DeletionComplete -> {
            LaunchedEffect(Unit) {
                onBack()
            }
        }

        TracksEditingState.Pending -> {
            Scaffold(
                topBar = {
                    TracksEditingTopBar(
                        onDeleteInit = null,
                        onBack = onBack,
                    )
                },
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TracksEditingTopBar(
    modifier: Modifier = Modifier,
    onDeleteInit: (() -> Unit)?,
    onBack: () -> Unit
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = { Text(text = stringResource(R.string.tracks_editing_title)) },
        navigationIcon = {
            ButtonBack(onBack = onBack)
        },
        actions = {
            if (onDeleteInit != null) {
                IconButton(
                    modifier = Modifier.testTag(AppTestTag.BUTTON_DELETE),
                    onClick = onDeleteInit,
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.content_description_delete)
                    )
                }
            }
        }
    )
}

@Composable
private fun TrackItem(
    modifier: Modifier = Modifier,
    track: Track,
    isSelected: Boolean,
    onClick: (String) -> Unit,
) {
    Row(modifier = modifier) {
        ListItem(
            modifier = Modifier.testTag(AppTestTag.TRACK_ITEM).clickable(
                enabled = true,
                onClick = {
                    onClick(track.id)
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
            trailingContent = {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.content_description_checked)
                    )
                }
            }
        )
    }
}

@Preview
@Composable
fun TracksEditingScreenPreview(@PreviewParameter(TracksEditingStateProvider::class) state: TracksEditingState) {
    TracksEditingScreen(state = state, onDeleteConfirm = {}, onChangeSelection = {}, onBack = {})
}

class TracksEditingStateProvider : PreviewParameterProvider<TracksEditingState> {
    val track1 = Track(
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
    )
    val track2 = Track(
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
    override val values: Sequence<TracksEditingState> = sequenceOf(
        TracksEditingState.Data(
            allTracks = listOf(track1, track2),
            selectedTracks = listOf(track1)
        ),
        TracksEditingState.Pending,
    )
}

package io.mityukov.geo.tracking.feature.track.list.editing

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.mityukov.geo.tracking.R
import io.mityukov.geo.tracking.app.ui.CommonAlertDialog
import io.mityukov.geo.tracking.feature.track.list.CompletedTrack
import io.mityukov.geo.tracking.feature.track.list.InProgressTrackHeadline
import io.mityukov.geo.tracking.feature.track.list.TrackProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TracksEditingScreen(
    viewModel: TracksEditingViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val openDeleteDialog = remember { mutableStateOf(false) }
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()

    when (state.value) {
        is TracksEditingState.Data -> {
            val data = state.value as TracksEditingState.Data
            val allTracks = data.allTracks
            val selectedTracks = data.selectedTracks

            if (selectedTracks.isEmpty()) {
                LaunchedEffect(Unit) {
                    onBack()
                }
            } else {
                Scaffold(
                    topBar = {
                        TracksEditingTopBar(openDeleteDialog = openDeleteDialog, onBack = onBack)
                    },
                ) { paddingValues ->
                    LazyColumn(modifier = Modifier.padding(paddingValues)) {
                        items(items = allTracks, key = {track -> track.id}) { track ->
                            TrackItem(
                                track = track,
                                isSelected = selectedTracks.any { it.id == track.id },
                                isCapturedTrack = track.id == data.capturedTrack,
                                onClick = {
                                    viewModel.add(TracksEditingEvent.ChangeSelection(track.id))
                                })
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }

                if (openDeleteDialog.value) {
                    CommonAlertDialog(
                        onDismiss = {
                            openDeleteDialog.value = false
                        },
                        onConfirm = {
                            viewModel.add(TracksEditingEvent.Delete)
                        },
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
            // no op
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TracksEditingTopBar(
    modifier: Modifier = Modifier,
    openDeleteDialog: MutableState<Boolean>,
    onBack: () -> Unit
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = { Text(text = stringResource(R.string.tracks_editing_title)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.content_description_back_button),
                )
            }
        },
        actions = {
            IconButton(onClick = {
                openDeleteDialog.value = true
            }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.content_description_delete)
                )
            }
        }
    )
}

@Composable
private fun TrackItem(
    modifier: Modifier = Modifier,
    track: CompletedTrack,
    isSelected: Boolean,
    isCapturedTrack: Boolean,
    onClick: (String) -> Unit,
) {
    Row(modifier = modifier) {
        ListItem(
            modifier = Modifier.clickable(
                enabled = true,
                onClick = {
                    onClick(track.id)
                }
            ),
            headlineContent = {
                InProgressTrackHeadline(
                    startTime = track.start,
                    isCapturedTrack = isCapturedTrack,
                    paused = false
                )
            },
            supportingContent = {
                TrackProperties(
                    duration = track.duration,
                    distance = track.distance,
                    altitudeUp = track.altitudeUp,
                    altitudeDown = track.altitudeDown,
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

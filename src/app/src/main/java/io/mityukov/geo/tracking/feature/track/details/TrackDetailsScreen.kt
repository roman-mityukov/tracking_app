package io.mityukov.geo.tracking.feature.track.details

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yandex.mapkit.mapview.MapView
import io.mityukov.geo.tracking.R
import io.mityukov.geo.tracking.app.AppProps
import io.mityukov.geo.tracking.app.ui.CommonAlertDialog
import io.mityukov.geo.tracking.core.model.track.Track
import io.mityukov.geo.tracking.yandex.showTrack
import kotlinx.coroutines.launch
import kotlin.time.DurationUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackDetailsScreen(
    viewModel: TrackDetailsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val openDeleteDialog = remember { mutableStateOf(false) }
    val uriString by viewModel.sharingStateFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val errorMessage = stringResource(R.string.error_sharing)

    LaunchedEffect(uriString) {
        if (uriString != null) {
            try {
                val uri = Uri.parse(uriString)
                val intent = Intent(Intent.ACTION_SEND)
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                intent.type = "application/octet-stream"
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(
                    Intent.createChooser(
                        intent,
                        context.getString(R.string.track_details_sharing_chooser_title)
                    )
                )
            } catch (_: ActivityNotFoundException) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(message = errorMessage)
                }
            } finally {
                viewModel.add(TrackDetailsEvent.ConsumeShare)
            }
        }
    }

    Scaffold(
        topBar = {
            TrackDetailsTopBar(viewModel = viewModel, onBack = onBack)
        },
    ) { paddingValues ->
        val state = viewModel.stateFlow.collectAsStateWithLifecycle()

        when (state.value) {
            TrackDetailsState.DeleteCompleted -> {
                onBack()
            }

            is TrackDetailsState.Data -> {
                val track = (state.value as TrackDetailsState.Data).data

                TrackDetailsContent(
                    paddingValues = paddingValues,
                    track = track,
                    onDelete = {
                        openDeleteDialog.value = true
                    },
                )
            }

            TrackDetailsState.Pending -> {
                CircularProgressIndicator()
            }
        }

        if (openDeleteDialog.value) {
            CommonAlertDialog(
                onDismiss = {
                    openDeleteDialog.value = false
                },
                onConfirm = {
                    viewModel.add(TrackDetailsEvent.Delete)
                    onBack()
                },
                dialogTitle = stringResource(R.string.track_details_delete_dialog_title),
                dialogText = stringResource(R.string.track_details_delete_dialog_text)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackDetailsTopBar(
    modifier: Modifier = Modifier,
    viewModel: TrackDetailsViewModel,
    onBack: () -> Unit,
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = { Text(text = stringResource(R.string.track_details_title)) },
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
                viewModel.add(TrackDetailsEvent.Share)
            }) {
                Icon(
                    painterResource(R.drawable.icon_share),
                    contentDescription = stringResource(R.string.content_description_share),
                )
            }
        }
    )
}

@Composable
private fun TrackDetailsContent(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    track: Track,
    onDelete: () -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = paddingValues.calculateTopPadding(),
            )
            .verticalScroll(scrollState)
    ) {

        TrackDetailsList(track = track)
        Spacer(modifier = Modifier.height(16.dp))
        TrackDetailsMap(track = track)
        Spacer(modifier = Modifier.height(16.dp))
        ButtonDeleteTrack(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onDelete = onDelete,
        )
    }
}

@Composable
private fun TrackDetailsMap(modifier: Modifier = Modifier, track: Track) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(ratio = 1f),
        factory = { context ->
            mapView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            mapView.setNoninteractive(false)
            mapView
        }
    )

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(Unit) {
        val observer = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                mapView.onStart()
            }

            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                mapView.onStop()
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    if (track.points.isNotEmpty()) {
        LaunchedEffect(track.points.last()) {
            mapView.showTrack(context, track, true)
        }
    }
}

@Composable
private fun TrackDetailsList(modifier: Modifier = Modifier, track: Track) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(
                R.string.track_details_start,
                track.points.first().geolocation.localDateTime.format(AppProps.UI_DATE_TIME_FORMATTER)
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(
                R.string.track_details_finish,
                track.points.last().geolocation.localDateTime.format(AppProps.UI_DATE_TIME_FORMATTER)
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(
                R.string.track_details_duration,
                DateUtils.formatElapsedTime(track.duration.toLong(DurationUnit.SECONDS)),
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = stringResource(R.string.track_details_distance, track.distance))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = stringResource(R.string.track_details_altitude_up, track.altitudeUp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = stringResource(R.string.track_details_altitude_down, track.altitudeDown))
    }
}

@Composable
fun ButtonDeleteTrack(modifier: Modifier = Modifier, onDelete: () -> Unit) {
    Button(
        modifier = modifier,
        onClick = {
            onDelete()
        },
        colors = ButtonDefaults.buttonColors().copy(
            containerColor = Color.Red
        )
    ) {
        Text(stringResource(R.string.track_details_button_delete_label))
    }
}

package io.mityukov.geo.tracking.feature.track.details

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import io.mityukov.geo.tracking.app.ui.ButtonBack
import io.mityukov.geo.tracking.app.ui.CommonAlertDialog
import io.mityukov.geo.tracking.core.model.track.DetailedTrack
import io.mityukov.geo.tracking.utils.log.logd
import io.mityukov.geo.tracking.utils.time.TimeUtils
import io.mityukov.geo.tracking.yandex.showTrack
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.time.DurationUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackDetailsScreen(
    viewModel: TrackDetailsViewModel = hiltViewModel(),
    onTrackMapSelected: (String) -> Unit,
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
                LaunchedEffect(Unit) {
                    onBack()
                }
            }

            is TrackDetailsState.Data -> {
                val track = (state.value as TrackDetailsState.Data).data

                TrackDetailsContent(
                    modifier = Modifier.padding(paddingValues),
                    detailedTrack = track,
                    onTrackMapSelected = onTrackMapSelected,
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
            ButtonBack(onBack = onBack)
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
    detailedTrack: DetailedTrack,
    onTrackMapSelected: (String) -> Unit,
    onDelete: () -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {

        TrackDetailsList(detailedTrack = detailedTrack)
        Spacer(modifier = Modifier.height(16.dp))
        AltitudeChart(
            chartData = AltitudeChartData(
                detailedTrack.altitudeByDistance.map { ChartPoint(it.second, it.first) }
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        SpeedChart(
            chartData = SpeedChartData(detailedTrack.speedByDistance.map {
                SpeedChartPoint(
                    it.second,
                    it.first
                )
            })
        )
        Spacer(modifier = Modifier.height(16.dp))
        TrackDetailsMap(track = detailedTrack, onTrackMapSelected = onTrackMapSelected)
        Spacer(modifier = Modifier.height(16.dp))
        ButtonDeleteTrack(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onDelete = onDelete,
        )
    }
}

@Composable
private fun TrackDetailsMap(
    modifier: Modifier = Modifier,
    track: DetailedTrack,
    onTrackMapSelected: (String) -> Unit,
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    Box {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(ratio = 1f),
            factory = { context ->
                mapView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                mapView.setNoninteractive(true)
                mapView
            }
        )
        Button(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(48.dp)
                .padding(8.dp),
            onClick = {
                onTrackMapSelected(track.data.id)
            },
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
        ) {
            Icon(
                painterResource(R.drawable.icon_fullscreen),
                contentDescription = stringResource(R.string.content_description_track_details_map_fullscreen),
            )
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                logd("onStart1")
                super.onStart(owner)
                mapView.onStart()
            }

            override fun onStop(owner: LifecycleOwner) {
                logd("onStop1")
                super.onStop(owner)
                mapView.onStop()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (track.geolocations.isNotEmpty()) {
        LaunchedEffect(track.geolocations.last()) {
            mapView.showTrack(context, track.geolocations, true)
        }
    }
}

@Composable
private fun TrackDetailsList(modifier: Modifier = Modifier, detailedTrack: DetailedTrack) {
    val track = detailedTrack.data
    Column(modifier = modifier) {
        Text(
            text = stringResource(
                R.string.track_details_start,
                TimeUtils.getFormattedLocalFromUTC(track.start, AppProps.UI_DATE_TIME_FORMATTER)
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(
                R.string.track_details_finish,
                TimeUtils.getFormattedLocalFromUTC(track.end, AppProps.UI_DATE_TIME_FORMATTER)
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
        Text(text = stringResource(R.string.track_details_distance, track.distance.roundToInt()))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(
                R.string.track_details_altitude_up,
                track.altitudeUp.roundToInt()
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(
                R.string.track_details_altitude_down,
                track.altitudeDown.roundToInt()
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(
                R.string.track_details_average_speed,
                String.format(Locale.getDefault(), "%.2f", track.averageSpeed)
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(
                R.string.track_details_min_speed,
                String.format(Locale.getDefault(), "%.2f", track.minSpeed)
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(
                R.string.track_details_max_speed,
                String.format(Locale.getDefault(), "%.2f", track.maxSpeed)
            )
        )
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

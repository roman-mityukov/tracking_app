package io.mityukov.geo.tracking.feature.track.details

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.text.format.DateUtils
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yandex.mapkit.mapview.MapView
import io.mityukov.geo.tracking.core.common.time.TimeUtils
import io.mityukov.geo.tracking.core.designsystem.component.ButtonBack
import io.mityukov.geo.tracking.core.designsystem.component.CommonAlertDialog
import io.mityukov.geo.tracking.core.designsystem.icon.AppIcons
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.core.model.track.DetailedTrack
import io.mityukov.geo.tracking.core.model.track.Track
import io.mityukov.geo.tracking.core.test.AppTestTag
import io.mityukov.geo.tracking.core.ui.FontScalePreviews
import io.mityukov.geo.tracking.core.ui.UiProps
import io.mityukov.geo.tracking.core.yandexmap.MapViewHolder
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TrackDetailsRoute(
    viewModel: TrackDetailsViewModel = hiltViewModel(),
    onTrackMapSelected: (String) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()
    val uriStringState = viewModel.sharingStateFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val resources = LocalResources.current
    val coroutineScope = rememberCoroutineScope()
    val mapViewHolder = remember { MapViewHolder(MapView(context), context.applicationContext) }

    MapLifecycle(
        onStart = {
            mapViewHolder.onStart()
        },
        onStop = {
            mapViewHolder.onStop()
        },
        onResume = {}
    )
    TrackDetailsScreen(
        state = state.value,
        sharingState = uriStringState.value,
        mapViewFactory = { _ ->
            val mapView = mapViewHolder.mapView
            mapView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            mapView.setNoninteractive(false)
            mapView
        },
        onPrepareShare = {
            viewModel.add(TrackDetailsEvent.Share)
        },
        onShare = { uriString ->
            try {
                val uri = uriString.toUri()
                val intent = Intent(Intent.ACTION_SEND)
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                intent.type = "application/octet-stream"
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(
                    Intent.createChooser(
                        intent,
                        context.getString(R.string.feature_track_details_sharing_chooser_title)
                    )
                )
            } catch (_: ActivityNotFoundException) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = resources.getString(io.mityukov.geo.tracking.core.ui.R.string.core_ui_error_sharing)
                    )
                }
            } finally {
                viewModel.add(TrackDetailsEvent.ConsumeShare)
            }
        },
        onDelete = {
            viewModel.add(TrackDetailsEvent.Delete)
            onBack()
        },
        onBack = onBack,
        onShowTrack = { geolocations ->
            mapViewHolder.updateTrack(geolocations = geolocations, moveCamera = true)
        },
        onTrackMapSelected = onTrackMapSelected,
    )
}

@Composable
internal fun TrackDetailsScreen(
    state: TrackDetailsState,
    sharingState: String?,
    mapViewFactory: (Context) -> View,
    onShowTrack: (List<Geolocation>) -> Unit,
    onTrackMapSelected: (String) -> Unit,
    onDelete: () -> Unit,
    onPrepareShare: () -> Unit,
    onShare: (String) -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(sharingState) {
        if (sharingState != null) {
            onShare(sharingState)
        }
    }

    val openDeleteDialog = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TrackDetailsTopBar(onShare = onPrepareShare, onBack = onBack)
        },
    ) { paddingValues ->

        when (state) {
            TrackDetailsState.DeleteCompleted -> {
                LaunchedEffect(Unit) {
                    onBack()
                }
            }

            is TrackDetailsState.Data -> {
                val track = state.detailedTrack

                if (track.geolocations.isEmpty()) {
                    TrackEmptyContent(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize(),
                        onShowDeleteDialog = {
                            openDeleteDialog.value = true
                        }
                    )
                } else {
                    TrackDetailsContent(
                        modifier = Modifier.padding(paddingValues),
                        detailedTrack = track,
                        mapViewFactory = mapViewFactory,
                        onTrackMapSelected = onTrackMapSelected,
                        onShowTrack = onShowTrack,
                        onShowDeleteDialog = {
                            openDeleteDialog.value = true
                        },
                    )
                }


            }

            TrackDetailsState.Pending -> {
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

    if (openDeleteDialog.value) {
        CommonAlertDialog(
            modifier = Modifier.testTag(AppTestTag.DIALOG_DELETE),
            onDismiss = {
                openDeleteDialog.value = false
            },
            onConfirm = onDelete,
            dialogTitle = stringResource(R.string.feature_track_details_delete_dialog_title),
            dialogText = stringResource(R.string.feature_track_details_delete_dialog_text)
        )
    }
}

@Preview
@FontScalePreviews
@Composable
internal fun TrackDetailsScreenPreview(@PreviewParameter(TrackDetailsStateProvider::class) state: TrackDetailsState) {
    TrackDetailsScreen(
        state = state,
        sharingState = null,
        mapViewFactory = { View(it) },
        onShowTrack = {},
        onTrackMapSelected = {},
        onDelete = {},
        onPrepareShare = {},
        onShare = {},
        onBack = {},
    )
}

internal class TrackDetailsStateProvider : PreviewParameterProvider<TrackDetailsState> {
    override val values: Sequence<TrackDetailsState> = sequenceOf(
        TrackDetailsState.Pending,
        TrackDetailsState.Data(
            detailedTrack = DetailedTrack(
                track = Track(
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
                    geolocationCount = 2,
                    filePath = "",
                ),
                geolocations = listOf(
                    Geolocation(
                        latitude = 53.654810,
                        longitude = 87.450375,
                        altitude = 310.2,
                        speed = 1.4f,
                        time = 1756964259,
                    ),
                    Geolocation(
                        latitude = 53.657810,
                        longitude = 87.458375,
                        altitude = 210.2,
                        speed = 1.2f,
                        time = 1756964270,
                    )
                ),
            )
        ),
        TrackDetailsState.Data(
            detailedTrack = DetailedTrack(
                track = Track(
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
                    geolocationCount = 2,
                    filePath = "",
                ),
                geolocations = listOf(),
            )
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackDetailsTopBar(
    modifier: Modifier = Modifier,
    onShare: () -> Unit,
    onBack: () -> Unit,
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = { Text(text = stringResource(R.string.feature_track_details_title)) },
        navigationIcon = {
            ButtonBack(onBack = onBack)
        },
        actions = {
            IconButton(modifier = Modifier.testTag(AppTestTag.BUTTON_SHARE), onClick = onShare) {
                Icon(
                    imageVector = AppIcons.Share,
                    contentDescription = stringResource(
                        io.mityukov.geo.tracking.core.ui.R.string.core_ui_content_description_share
                    ),
                )
            }
        }
    )
}

@Composable
private fun TrackEmptyContent(
    modifier: Modifier = Modifier,
    onShowDeleteDialog: () -> Unit,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Трек не содержит записанных геолокаций.",
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        ButtonDeleteTrack(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onDelete = onShowDeleteDialog,
        )
    }
}

@Composable
private fun TrackDetailsContent(
    modifier: Modifier = Modifier,
    detailedTrack: DetailedTrack,
    mapViewFactory: (Context) -> View,
    onTrackMapSelected: (String) -> Unit,
    onShowTrack: (List<Geolocation>) -> Unit,
    onShowDeleteDialog: () -> Unit,
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
        TrackDetailsMap(
            track = detailedTrack,
            mapViewFactory = mapViewFactory,
            onTrackMapSelected = onTrackMapSelected,
            onShowTrack = onShowTrack,
        )
        Spacer(modifier = Modifier.height(16.dp))
        ButtonDeleteTrack(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onDelete = onShowDeleteDialog,
        )
    }
}

@Composable
private fun TrackDetailsMap(
    modifier: Modifier = Modifier,
    track: DetailedTrack,
    mapViewFactory: (Context) -> View,
    onShowTrack: (List<Geolocation>) -> Unit,
    onTrackMapSelected: (String) -> Unit,
) {
    Box {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(ratio = 1f),
            factory = mapViewFactory
        )
        Button(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(48.dp)
                .padding(8.dp)
                .testTag(AppTestTag.BUTTON_TRACK_DETAILS_MAP),
            onClick = {
                onTrackMapSelected(track.track.id)
            },
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
        ) {
            Icon(
                imageVector = AppIcons.FullScreen,
                contentDescription = stringResource(R.string.feature_track_details_content_description_map_fullscreen),
            )
        }
    }

    if (track.geolocations.isNotEmpty()) {
        LaunchedEffect(track.geolocations.last()) {
            onShowTrack(track.geolocations)
        }
    }
}

@Composable
private fun TrackDetailsList(modifier: Modifier = Modifier, detailedTrack: DetailedTrack) {
    val track = detailedTrack.track
    Column(modifier = modifier) {
        Text(
            text = stringResource(
                R.string.feature_track_details_start,
                TimeUtils.getFormattedLocalFromUTC(track.start, UiProps.DEFAULT_DATE_TIME_FORMATTER)
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(
                R.string.feature_track_details_finish,
                TimeUtils.getFormattedLocalFromUTC(track.end, UiProps.DEFAULT_DATE_TIME_FORMATTER)
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(
                R.string.feature_track_details_duration,
                DateUtils.formatElapsedTime(track.duration.toLong(DurationUnit.SECONDS)),
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = stringResource(R.string.feature_track_details_distance, track.distance.roundToInt()))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(
                R.string.feature_track_details_altitude_up,
                track.altitudeUp.roundToInt()
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(
                R.string.feature_track_details_altitude_down,
                track.altitudeDown.roundToInt()
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(
                R.string.feature_track_details_average_speed,
                String.format(Locale.getDefault(), "%.2f", track.averageSpeed)
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(
                R.string.feature_track_details_min_speed,
                String.format(Locale.getDefault(), "%.2f", track.minSpeed)
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(
                R.string.feature_track_details_max_speed,
                String.format(Locale.getDefault(), "%.2f", track.maxSpeed)
            )
        )
    }
}

@Composable
private fun ButtonDeleteTrack(modifier: Modifier = Modifier, onDelete: () -> Unit) {
    Button(
        modifier = modifier.testTag(AppTestTag.BUTTON_DELETE),
        onClick = {
            onDelete()
        },
        colors = ButtonDefaults.buttonColors().copy(
            containerColor = Color.Red
        )
    ) {
        Text(stringResource(R.string.feature_track_details_button_delete_label))
    }
}

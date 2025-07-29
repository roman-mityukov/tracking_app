package io.mityukov.geo.tracking.feature.track.details

import android.content.Context
import android.content.Intent
import android.graphics.PointF
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.LineStyle
import com.yandex.mapkit.map.TextStyle
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import io.mityukov.geo.tracking.R
import io.mityukov.geo.tracking.app.GeoAppProps
import io.mityukov.geo.tracking.app.ui.CommonAlertDialog
import io.mityukov.geo.tracking.core.model.track.Track
import io.mityukov.geo.tracking.yandex.TrackAppearanceSettings
import kotlin.time.DurationUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackDetailsScreen(
    viewModel: TrackDetailsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val openDeleteDialog = remember { mutableStateOf(false) }
    val uriString by viewModel.sharingStateFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uriString) {
        if (uriString != null) {
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
            viewModel.add(TrackDetailsEvent.ConsumeShare)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.track_details_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.add(TrackDetailsEvent.Share)
                    }) {
                        Icon(painterResource(R.drawable.icon_share), contentDescription = null)
                    }
                }
            )
        },
    ) { paddingValues ->
        val state = viewModel.stateFlow.collectAsStateWithLifecycle()

        when (state.value) {
            TrackDetailsState.DeleteCompleted -> {
                onBack()
            }

            is TrackDetailsState.Data -> {
                val track = (state.value as TrackDetailsState.Data).data

                TrackDetailsContent(paddingValues = paddingValues, track = track, onDelete = {
                    openDeleteDialog.value = true
                })
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

@Composable
fun TrackDetailsContent(
    paddingValues: PaddingValues,
    track: Track,
    onDelete: () -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = paddingValues.calculateTopPadding(),
            )
            .verticalScroll(scrollState)
    ) {
        TrackDetailsList(track)
        Spacer(modifier = Modifier.height(16.dp))
        val context = LocalContext.current
        val mapView = remember { MapView(context) }
        AndroidView(
            modifier = Modifier
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
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = {
                onDelete()
            },
            colors = ButtonDefaults.buttonColors().copy(
                containerColor = Color.Red
            )
        ) {
            Text(stringResource(R.string.track_details_button_delete_label))
        }
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        LaunchedEffect(Unit) {
            lifecycle.addObserver(
                object : DefaultLifecycleObserver {
                    override fun onStart(owner: LifecycleOwner) {
                        super.onStart(owner)
                        mapView.onStart()
                    }

                    override fun onStop(owner: LifecycleOwner) {
                        super.onStop(owner)
                        mapView.onStop()
                    }
                },
            )
        }

        if (track.points.isNotEmpty()) {
            LaunchedEffect(track.points.last()) {
                mapView.showTrack(
                    context,
                    track,
                    TrackAppearanceSettings.ZOOM_OUT_CORRECTION_DETAILS
                )
            }
        }
    }
}

@Composable
fun TrackDetailsList(track: Track) {
    Column {
        Text(
            text = "Старт ${
                track.points.first().geolocation.localDateTime.format(
                    GeoAppProps.UI_DATE_TIME_FORMATTER
                )
            }"
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Финиш ${
                track.points.last().geolocation.localDateTime.format(
                    GeoAppProps.UI_DATE_TIME_FORMATTER
                )
            }"
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Продолжительность ${
                DateUtils.formatElapsedTime(
                    track.duration.toLong(
                        DurationUnit.SECONDS
                    )
                )
            }"
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Расстояние ${track.distance}м")
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Количество точек ${track.points.size}")
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Набор высоты ${track.altitudeUp}м")
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Сброс высоты ${track.altitudeDown}м")
    }
}

fun MapView.showTrack(context: Context, track: Track, zoomOutCorrection: Float) {
    map.mapObjects.clear()
    val points = track.points.map {
        Point(it.geolocation.latitude, it.geolocation.longitude)
    }

    val imageProvider =
        ImageProvider.fromResource(context, R.drawable.pin_track_point)

    val pinsCollection = map.mapObjects.addCollection()
    val textStyle = TextStyle().apply {
        size = 10f
        placement = TextStyle.Placement.RIGHT
        offset = 0f
    }
    points.forEachIndexed { index, point ->
        val placemark = pinsCollection.addPlacemark()
        placemark.apply {
            geometry = Point(point.latitude, point.longitude)
            setIcon(imageProvider)
            setText(
                index.toString(),
                textStyle,
            )
        }
        placemark.setIconStyle(
            IconStyle().apply {
                anchor = PointF(
                    TrackAppearanceSettings.PLACEMARK_ANCHOR_X,
                    TrackAppearanceSettings.PLACEMARK_ANCHOR_Y
                )
                scale = TrackAppearanceSettings.PLACEMARK_SCALE
            }
        )
    }

    val geometry = if (track.points.size > 1) {
        val polyline = Polyline(points)
        val polylineObject = map.mapObjects.addPolyline(polyline)
        polylineObject.apply {
            style = LineStyle().apply {
                strokeWidth = 2f
                setStrokeColor(ContextCompat.getColor(context, R.color.teal_700))
            }
        }

        Geometry.fromPolyline(polyline)
    } else {
        Geometry.fromPoint(points.first())
    }

    val position = map.cameraPosition(geometry)
    map.move(
        CameraPosition(
            position.target,
            position.zoom - zoomOutCorrection,
            position.azimuth,
            position.tilt
        )
    )
}

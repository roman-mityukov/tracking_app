package io.mityukov.geo.tracking.feature.track.details

import android.graphics.PointF
import android.text.format.DateUtils
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.LineStyle
import com.yandex.mapkit.map.TextStyle
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import io.mityukov.geo.tracking.R
import io.mityukov.geo.tracking.app.ui.CommonAlertDialog
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.DurationUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackDetailsScreen(
    viewModel: TrackDetailsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val openDeleteDialog = remember { mutableStateOf(false) }

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
                        openDeleteDialog.value = true
                    }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
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
                val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")

                Column(
                    modifier = Modifier.padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding(),
                    )
                ) {
                    Text(
                        text = "Старт ${
                            LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(track.points.first().geolocation.time),
                                ZoneId.systemDefault()
                            ).format(formatter)
                        }"
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Финиш ${
                            LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(track.points.last().geolocation.time),
                                ZoneId.systemDefault()
                            ).format(formatter)
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
                    Spacer(modifier = Modifier.height(16.dp))
                    val context = LocalContext.current
                    val mapView = remember { MapView(context) }
                    AndroidView(
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

                    val points = track.points.map {
                        Point(it.geolocation.latitude, it.geolocation.longitude)
                    }
                    val polyline = Polyline(points)
                    val polylineObject = mapView.map.mapObjects.addPolyline(polyline)
                    polylineObject.apply {
                        style = LineStyle().apply {
                            strokeWidth = 2f
                            setStrokeColor(ContextCompat.getColor(context, R.color.teal_700))
                        }
                    }

                    val imageProvider =
                        ImageProvider.fromResource(context, R.drawable.pin_track_point)

                    val pinsCollection = mapView.map.mapObjects.addCollection()
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
                                anchor = PointF(0.5f, 1.0f)
                                scale = 0.5f
                            }
                        )
                    }

                    val geometry = Geometry.fromPolyline(polyline)
                    val position = mapView.map.cameraPosition(geometry)
                    mapView.map.move(position)
                }
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
package io.rm.test.geo.feature.map

import android.content.Intent
import android.graphics.PointF
import android.provider.Settings
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import io.rm.test.geo.R
import io.rm.test.geo.core.data.repository.geo.GeolocationUpdateException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object YandexMapSettings {
    const val ZOOM_STEP: Float = 1f
}

fun Map.zoom(value: Float) {
    with(cameraPosition) {
        move(
            CameraPosition(target, zoom + value, azimuth, tilt),
            Animation(Animation.Type.SMOOTH, 0.4f),
            null,
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel(),
    onPoiSelected: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val context = LocalContext.current
        val mapView = remember { MapView(context) }
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        var needMoveToCurrentLocation = remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            lifecycle.addObserver(
                object : DefaultLifecycleObserver {
                    override fun onResume(owner: LifecycleOwner) {
                        super.onResume(owner)
                        viewModel.add(MapEvent.Start)
                    }

                    override fun onStart(owner: LifecycleOwner) {
                        super.onStart(owner)
                        mapView.onStart()
                    }

                    override fun onStop(owner: LifecycleOwner) {
                        super.onStop(owner)
                        mapView.onStop()
                        viewModel.add(MapEvent.Stop)
                    }
                },
            )
        }

        val multiplePermissionsState = rememberMultiplePermissionsState(
            listOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
            )
        )

        val openShouldShowRationale = remember { mutableStateOf(false) }

        if (openShouldShowRationale.value) {
            BasicAlertDialog(
                onDismissRequest = { openShouldShowRationale.value = false }

            ) {
                Surface(
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight(),
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = AlertDialogDefaults.TonalElevation
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text =
                                "Чтобы узнать Ваше текущее местоположение нужно дать разрешение на доступ к геолокации устройства.",
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        TextButton(
                            onClick = {
                                openShouldShowRationale.value = false
                                multiplePermissionsState.launchMultiplePermissionRequest()
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Дать разрешение")
                        }
                    }
                }
            }
        }

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

        ButtonsPanel(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 32.dp),
            onZoomIn = {
                mapView.map.zoom(YandexMapSettings.ZOOM_STEP)
            },
            onZoomOut = {
                mapView.map.zoom(-YandexMapSettings.ZOOM_STEP)
            },
            onMyLocation = {
                needMoveToCurrentLocation.value = true
                viewModel.add(MapEvent.GetCurrentLocation)
            },
            onCaptureTrack = {

            },
        )

        val viewModelState = viewModel.stateFlow.collectAsStateWithLifecycle()

        when (viewModelState.value) {
            is MapState.CurrentLocation -> {
                snackbarHostState.currentSnackbarData?.dismiss()
                val geolocation = (viewModelState.value as MapState.CurrentLocation).data
                mapView.map.mapObjects.clear()
                val placemark = mapView.map.mapObjects.addPlacemark()
                placemark.apply {
                    geometry = Point(geolocation.latitude, geolocation.longitude)
                    setIcon(ImageProvider.fromResource(context, R.drawable.pin_my_location1))
                }
                placemark.setIconStyle(
                    IconStyle().apply {
                        anchor = PointF(0.5f, 1.0f)
                        scale = 0.2f
                    }
                )

                if (needMoveToCurrentLocation.value) {
                    needMoveToCurrentLocation.value = false
                    mapView.map.move(
                        CameraPosition(
                            Point(geolocation.latitude, geolocation.longitude),
                            15f,
                            0f,
                            0f,
                        )
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(
                            horizontal = 16.dp,
                            vertical = WindowInsets.safeDrawing.asPaddingValues()
                                .calculateTopPadding() + 16.dp
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val clipboardManager = LocalClipboardManager.current
                        val localDateTime = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(geolocation.time),
                            ZoneId.systemDefault()
                        )
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "Последнее обновление $localDateTime\nШирота ${geolocation.latitude} Долгота ${geolocation.longitude}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        IconButton(onClick = {
                            clipboardManager.setText(AnnotatedString(text = "${geolocation.latitude},${geolocation.longitude}"))
                        }) {
                            Icon(painterResource(R.drawable.icon_copy), null)
                        }
                    }
                }
            }

            is MapState.NoLocation -> {
                LaunchedEffect(viewModelState.value) {
                    when ((viewModelState.value as MapState.NoLocation).cause) {
                        GeolocationUpdateException.LocationDisabled -> {
                            val result = snackbarHostState.showSnackbar(
                                message = "Для отображения вашего местоположения на карте необходимо включить локацию в настройка системы",
                                actionLabel = "Включить",
                                withDismissAction = true,
                                duration = SnackbarDuration.Indefinite,
                            )
                            when (result) {
                                SnackbarResult.Dismissed -> {
                                    // no op
                                }

                                SnackbarResult.ActionPerformed -> {
                                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                    context.startActivity(intent)
                                }
                            }
                        }

                        GeolocationUpdateException.PermissionsNotGranted -> {
                            if (multiplePermissionsState.shouldShowRationale) {
                                openShouldShowRationale.value = true
                            } else if (multiplePermissionsState.allPermissionsGranted.not()) {
                                multiplePermissionsState.launchMultiplePermissionRequest()
                            }
                        }

                        else -> {
                            // no op
                        }
                    }
                }
            }

            MapState.PendingLocationUpdates -> {
                LaunchedEffect(viewModelState.value) {
                    snackbarHostState.showSnackbar("Обновление местоположения...")
                }
            }
        }
    }
}

@Composable
private fun ButtonsPanel(
    modifier: Modifier,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onMyLocation: () -> Unit,
    onCaptureTrack: (Boolean) -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        var checked: Boolean by remember { mutableStateOf(false) }

        Button(
            modifier = Modifier.size(42.dp),
            onClick = onZoomIn,
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
        ) {
            Icon(painterResource(R.drawable.icon_plus), contentDescription = null)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            modifier = Modifier.size(42.dp),
            onClick = onZoomOut,
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
        ) {
            Icon(painterResource(R.drawable.icon_minus), contentDescription = null)
        }
        Spacer(modifier = Modifier.height(8.dp))
        IconToggleButton(
            checked = checked,
            onCheckedChange = {
                checked = it
                onCaptureTrack(checked)
            },
            colors = IconButtonDefaults.iconToggleButtonColors().copy(
                containerColor = ButtonDefaults.buttonColors().containerColor,
                contentColor = ButtonDefaults.buttonColors().contentColor,
                checkedContainerColor = Color.Red,
                checkedContentColor = ButtonDefaults.buttonColors().contentColor,
            )
        ) {
            Icon(painterResource(R.drawable.home_track_outlined), contentDescription = null)
        }
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            modifier = Modifier.size(42.dp),
            onClick = onMyLocation,
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
        ) {
            Icon(painterResource(R.drawable.icon_my_location), contentDescription = null)
        }
    }
}
package io.mityukov.geo.tracking.feature.map

import android.content.Intent
import android.graphics.PointF
import android.os.Build
import android.provider.Settings
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.yandex.mapkit.ScreenPoint
import com.yandex.mapkit.ScreenRect
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import io.mityukov.geo.tracking.R
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationUpdateException
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.core.model.track.Track
import io.mityukov.geo.tracking.feature.track.capture.TrackCaptureView
import io.mityukov.geo.tracking.feature.track.details.showTrack
import io.mityukov.geo.tracking.feature.track.list.TrackHeadline
import io.mityukov.geo.tracking.feature.track.list.TrackProperties
import io.mityukov.geo.tracking.yandex.TrackAppearanceSettings
import io.mityukov.geo.tracking.yandex.YandexMapSettings
import io.mityukov.geo.tracking.yandex.zoom

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val context = LocalContext.current
        val mapView = remember { MapView(context) }
        val viewModelState = viewModel.stateFlow.collectAsStateWithLifecycle()

        MapLifecycle(mapView, viewModel)
        MapPermissions(viewModelState.value, snackbarHostState)

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

        val needMoveToCurrentLocation = remember { mutableStateOf(true) }
        ButtonsPanel(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 32.dp),
            state = viewModelState.value,
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
        )

        MapContent(viewModelState.value, mapView, snackbarHostState, needMoveToCurrentLocation)
    }
}

@Composable
private fun BoxScope.MapContent(
    viewModelState: MapState,
    mapView: MapView,
    snackbarHostState: SnackbarHostState,
    needMoveToCurrentLocation: MutableState<Boolean>
) {
    when (viewModelState) {
        is MapState.CurrentTrack -> {
            CurrentTrack(viewModelState.track, mapView)
        }

        is MapState.CurrentLocation -> {
            snackbarHostState.currentSnackbarData?.dismiss()
            CurrentGeolocation(
                viewModelState.data,
                mapView,
                needMoveToCurrentLocation
            )
        }

        MapState.PendingLocationUpdates -> {
            val snackbarMessage = stringResource(R.string.map_update_location)
            LaunchedEffect(viewModelState) {
                snackbarHostState.showSnackbar(snackbarMessage)
            }
        }

        else -> {
            // no op
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun MapPermissions(viewModelState: MapState, snackbarHostState: SnackbarHostState) {
    val permissions = mutableListOf(
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    val multiplePermissionsState = rememberMultiplePermissionsState(permissions)
    val showLocationRationale = remember { mutableStateOf(false) }
    if (showLocationRationale.value) {
        LocationRationaleDialog(
            onNegative = {
                showLocationRationale.value = false
            },
            onPositive = {
                showLocationRationale.value = false
                multiplePermissionsState.launchMultiplePermissionRequest()
            },
        )
    }

    if (viewModelState is MapState.NoLocation) {
        NoLocation(
            viewModelState,
            snackbarHostState,
            {
                if (multiplePermissionsState.shouldShowRationale) {
                    showLocationRationale.value = true
                } else if (multiplePermissionsState.allPermissionsGranted.not()) {
                    multiplePermissionsState.launchMultiplePermissionRequest()
                }
            }
        )
    }
}

@Composable
private fun MapLifecycle(
    mapView: MapView,
    viewModel: MapViewModel,
) {
    val viewModelState = viewModel.stateFlow.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(Unit) {
        lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    super.onResume(owner)
                    if (viewModelState.value !is MapState.CurrentTrack) {
                        viewModel.add(MapEvent.StartUpdateCurrentLocation)
                    }
                }

                override fun onStart(owner: LifecycleOwner) {
                    super.onStart(owner)
                    mapView.onStart()
                }

                override fun onStop(owner: LifecycleOwner) {
                    super.onStop(owner)
                    mapView.onStop()
                    if (viewModelState.value !is MapState.CurrentTrack) {
                        viewModel.add(MapEvent.StopUpdateCurrentLocation)
                    }
                }
            },
        )
    }
}

@Composable
private fun NoLocation(
    state: MapState.NoLocation,
    snackbarHostState: SnackbarHostState,
    onPermissionsNotGranted: () -> Unit,
) {
    val context = LocalContext.current
    val message = stringResource(R.string.map_disabled_location_permission_description)
    val actionLabel = stringResource(R.string.map_disabled_location_permission_consent)
    LaunchedEffect(state) {
        when (state.cause) {
            GeolocationUpdateException.LocationDisabled -> {
                val result = snackbarHostState.showSnackbar(
                    message = message,
                    actionLabel = actionLabel,
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
                onPermissionsNotGranted()
            }

            else -> {
                // no op
            }
        }
    }
}

@Composable
private fun BoxScope.CurrentGeolocation(
    geolocation: Geolocation,
    mapView: MapView,
    needMoveToCurrentLocation: MutableState<Boolean>
) {
    mapView.map.mapObjects.clear()
    val placemark = mapView.map.mapObjects.addPlacemark()
    placemark.apply {
        geometry = Point(geolocation.latitude, geolocation.longitude)
        setIcon(ImageProvider.fromResource(LocalContext.current, R.drawable.pin_my_location))
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

    if (needMoveToCurrentLocation.value) {
        needMoveToCurrentLocation.value = false
        mapView.map.move(
            CameraPosition(
                Point(geolocation.latitude, geolocation.longitude),
                YandexMapSettings.ZOOM_DEFAULT,
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
            Text(
                modifier = Modifier.weight(1f),
                text = "Последнее обновление ${geolocation.localDateTime}\n" +
                        "Широта ${geolocation.latitude} Долгота ${geolocation.longitude}",
                style = MaterialTheme.typography.bodySmall,
            )
            IconButton(onClick = {
                clipboardManager.setText(
                    AnnotatedString(
                        text = "${geolocation.latitude},${geolocation.longitude}"
                    )
                )
            }) {
                Icon(painterResource(R.drawable.icon_copy), null)
            }
        }
    }
}

@Composable
private fun BoxScope.CurrentTrack(
    track: Track,
    mapView: MapView,
) {
    if (track.points.isNotEmpty()) {
        mapView.showTrack(
            LocalContext.current,
            track,
            TrackAppearanceSettings.ZOOM_OUT_CORRECTION_MAP
        )
        mapView.mapWindow.focusRect = ScreenRect(
            ScreenPoint(0f, TrackAppearanceSettings.UI_TOP_OFFSET),
            ScreenPoint(
                mapView.mapWindow.width()
                    .toFloat() - TrackAppearanceSettings.UI_RIGHT_OFFSET,
                mapView.mapWindow.height().toFloat()
            )
        )

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
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    TrackHeadline(track, true)
                    TrackProperties(track)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationRationaleDialog(
    onNegative: () -> Unit,
    onPositive: () -> Unit,
) {
    BasicAlertDialog(
        onDismissRequest = onNegative
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = stringResource(R.string.map_location_permission_description))
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(
                    onClick = onPositive,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(text = stringResource(R.string.map_location_permission_consent))
                }
            }
        }
    }
}

@Composable
private fun ButtonsPanel(
    modifier: Modifier,
    state: MapState,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onMyLocation: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {


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
        TrackCaptureView()
        if (state !is MapState.CurrentTrack) {
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                modifier = Modifier.size(42.dp),
                onClick = onMyLocation,
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
            ) {
                Icon(painterResource(R.drawable.icon_my_location), contentDescription = null)
            }
        } else {
            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

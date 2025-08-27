@file:Suppress("TooManyFunctions")

package io.mityukov.geo.tracking.feature.map

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.keepScreenOn
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ShareCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.yandex.mapkit.mapview.MapView
import io.mityukov.geo.tracking.R
import io.mityukov.geo.tracking.app.AppProps
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationUpdateException
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.feature.track.capture.TrackCaptureView
import io.mityukov.geo.tracking.feature.track.list.TrackProperties
import io.mityukov.geo.tracking.utils.time.TimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val context = LocalContext.current
        val mapViewHolder = remember { MapViewHolder(MapView(context), context) }
        val viewModelState = viewModel.stateFlow.collectAsStateWithLifecycle()

        MapLifecycle(
            onStart = {
                mapViewHolder.onStart()
            },
            onResume = {
                viewModel.add(MapEvent.ResumeCurrentLocationUpdate)
            },
            onStop = {
                mapViewHolder.onStop()
                viewModel.add(MapEvent.PauseCurrentLocationUpdate)
            },
        )
        MapPermissions(viewModelState = viewModelState.value, snackbarHostState = snackbarHostState)
        MapContent(mapViewHolder = mapViewHolder)
        MapControls(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 32.dp),
            mapViewHolder = mapViewHolder,
            currentLocationFlow = viewModel.currentLocationFlow,
        )
        MapInfoContent(
            modifier = Modifier.align(Alignment.TopCenter),
            viewModelState = viewModelState.value,
            mapViewHolder = mapViewHolder,
            snackbarHostState = snackbarHostState,
        )
    }
}

@Composable
private fun MapContent(modifier: Modifier = Modifier, mapViewHolder: MapViewHolder) {
    AndroidView(
        modifier = modifier.keepScreenOn(),
        factory = { context ->
            val mapView = mapViewHolder.mapView
            mapView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            mapView.setNoninteractive(false)
            mapView
        }
    )
}

@Composable
private fun MapInfoContent(
    modifier: Modifier,
    viewModelState: MapState,
    mapViewHolder: MapViewHolder,
    snackbarHostState: SnackbarHostState,
) {
    when (viewModelState) {
        is MapState.CurrentTrack -> {
            if (viewModelState.geolocations.isNotEmpty()) {
                LaunchedEffect(viewModelState.geolocations.size) {
                    mapViewHolder.updateTrack(viewModelState.geolocations)
                }
            }

            CurrentTrack(
                modifier = modifier,
                viewModelState = viewModelState,
                snackbarHostState = snackbarHostState,
            )
        }

        MapState.CurrentTrackError -> {
            CurrentTrackError(modifier = modifier)
        }

        is MapState.CurrentLocation -> {
            snackbarHostState.currentSnackbarData?.dismiss()

            LaunchedEffect(viewModelState.data) {
                mapViewHolder.currentLocationPlacemark(viewModelState.data)
            }

            CurrentGeolocation(
                modifier = modifier,
                geolocation = viewModelState.data,
                snackbarHostState = snackbarHostState,
            )
        }

        MapState.PendingLocationUpdates -> {
            val snackbarMessage = stringResource(R.string.map_update_location)
            LaunchedEffect(viewModelState) {
                snackbarHostState.showSnackbar(snackbarMessage)
            }
        }

        is MapState.NoLocation -> {
            // Обработка происходит в MapPermissions
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun MapPermissions(
    viewModelState: MapState,
    snackbarHostState: SnackbarHostState
) {
    val permissions = buildList {
        add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
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
            state = viewModelState,
            snackbarHostState = snackbarHostState,
            onPermissionsNotGranted = {
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
    onStart: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                onResume()
            }

            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                onStart()
            }

            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                onStop()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@Composable
private fun MapControls(
    modifier: Modifier,
    currentLocationFlow: Flow<Geolocation?>,
    mapViewHolder: MapViewHolder,
) {
    val currentLocationState: State<Geolocation?> =
        currentLocationFlow.collectAsStateWithLifecycle(null)
    val mapNavigateTo = {
        val geolocation = currentLocationState.value
        mapViewHolder.navigateTo(geolocation)
    }

    LaunchedEffect(currentLocationState.value != null) {
        mapNavigateTo()
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
    ) {
        MapControlButton(
            onClick = {
                mapViewHolder.zoomIn()
            },
            icon = R.drawable.icon_plus,
            contentDescription = stringResource(R.string.content_description_map_zoom_in),
        )
        Spacer(modifier = Modifier.height(8.dp))
        MapControlButton(
            onClick = {
                mapViewHolder.zoomOut()
            },
            icon = R.drawable.icon_minus,
            contentDescription = stringResource(R.string.content_description_map_zoom_out),
        )
        Spacer(modifier = Modifier.height(8.dp))
        MapControlButton(
            onClick = mapNavigateTo,
            icon = R.drawable.icon_my_location,
            contentDescription = stringResource(R.string.content_description_map_my_location),
        )
        Spacer(modifier = Modifier.height(48.dp))
        TrackCaptureView()
    }
}

@Composable
private fun CurrentGeolocation(
    modifier: Modifier,
    geolocation: Geolocation,
    snackbarHostState: SnackbarHostState,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = WindowInsets.safeDrawing.asPaddingValues()
                    .calculateTopPadding() + 16.dp
            )
    ) {
        CurrentGeolocationSharing(geolocation = geolocation, snackbarHostState = snackbarHostState)
    }
}

@Composable
private fun CurrentTrack(
    modifier: Modifier,
    viewModelState: MapState.CurrentTrack,
    snackbarHostState: SnackbarHostState,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = WindowInsets.safeDrawing.asPaddingValues()
                    .calculateTopPadding() + 16.dp
            )
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    InProgressTrackHeadline(
                        startTime = viewModelState.trackInProgress.start,
                        paused = viewModelState.trackInProgress.paused
                    )
                    TrackProperties(
                        duration = viewModelState.trackInProgress.duration,
                        distance = viewModelState.trackInProgress.distance,
                        altitudeUp = viewModelState.trackInProgress.altitudeUp,
                        altitudeDown = viewModelState.trackInProgress.altitudeDown,
                        speed = viewModelState.trackInProgress.currentSpeed,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (viewModelState.currentLocation != null) {
            CurrentGeolocationSharing(
                geolocation = viewModelState.currentLocation,
                snackbarHostState = snackbarHostState
            )
        }
    }
}

@Composable
private fun CurrentTrackError(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
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
            Text(
                text = stringResource(R.string.error_track_capture),
                style = TextStyle(color = Color.Red, fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
private fun InProgressTrackHeadline(
    modifier: Modifier = Modifier,
    startTime: Long,
    paused: Boolean
) {
    val formattedStartTime =
        TimeUtils.getFormattedLocalFromUTC(startTime, AppProps.UI_DATE_TIME_FORMATTER)

    Text(
        modifier = modifier,
        text = buildAnnotatedString {
            append("$formattedStartTime ")
            withStyle(
                style = SpanStyle(
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                )
            ) {
                if (paused) {
                    append(stringResource(R.string.tracks_item_title_pause))
                } else {
                    append(stringResource(R.string.tracks_item_title_capturing))
                }
            }
        })
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

            GeolocationUpdateException.LocationIsNull, GeolocationUpdateException.Initialization -> {
                // Валидное состояние - ничего не делаем
            }

            null -> {
                // Валидное состояние если локация не пришла без ошибки
            }
        }
    }
}

@Composable
private fun CurrentGeolocationSharing(
    modifier: Modifier = Modifier,
    geolocation: Geolocation,
    snackbarHostState: SnackbarHostState,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()
            val errorMessage = stringResource(R.string.error_sharing)

            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                text = stringResource(
                    R.string.map_current_location_message,
                    geolocation.localDateTime.format(AppProps.UI_DATE_TIME_FORMATTER),
                    geolocation.latitude,
                    geolocation.longitude,
                    geolocation.altitude.roundToInt(),
                    String.format(Locale.getDefault(), "%.1f", geolocation.speed),
                ),
                style = MaterialTheme.typography.bodySmall,
            )
            IconButton(
                onClick = {
                    try {
                        ShareCompat.IntentBuilder(context)
                            .setType("text/plain")
                            .setText(
                                context.resources.getString(
                                    R.string.map_current_location_share_text,
                                    geolocation.latitude,
                                    geolocation.longitude
                                )
                            )
                            .setChooserTitle(context.resources.getString(R.string.map_current_location_share_title))
                            .startChooser()
                    } catch (_: ActivityNotFoundException) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(message = errorMessage)
                        }
                    }
                }
            ) {
                Icon(painterResource(R.drawable.icon_share), null)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationRationaleDialog(
    modifier: Modifier = Modifier,
    onNegative: () -> Unit,
    onPositive: () -> Unit,
) {
    BasicAlertDialog(
        onDismissRequest = onNegative
    ) {
        Surface(
            modifier = modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.map_location_permission_description),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onNegative) {
                        Text(text = stringResource(R.string.dialog_no))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onPositive) {
                        Text(text = stringResource(R.string.map_location_permission_consent))
                    }
                }
            }
        }
    }
}

@Composable
private fun MapControlButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: Int,
    contentDescription: String,
) {
    Button(
        modifier = modifier.size(48.dp),
        onClick = onClick,
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
    ) {
        Icon(painterResource(icon), contentDescription = contentDescription)
    }
}

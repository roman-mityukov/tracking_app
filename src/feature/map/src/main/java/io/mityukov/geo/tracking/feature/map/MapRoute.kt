@file:Suppress("LongMethod")

package io.mityukov.geo.tracking.feature.map

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import androidx.core.app.ShareCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.yandex.mapkit.mapview.MapView
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackCaptureStatus
import io.mityukov.geo.tracking.core.yandexmap.MapViewHolder
import io.mityukov.geo.tracking.feature.track.capture.TrackCapture
import io.mityukov.geo.tracking.feature.track.capture.TrackCaptureEvent
import io.mityukov.geo.tracking.feature.track.capture.TrackCaptureViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun MapRoute(
    mapViewModel: MapViewModel = hiltViewModel(),
    trackCaptureViewModel: TrackCaptureViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val resources = LocalResources.current
    val mapViewHolder = remember { MapViewHolder(MapView(context), context.applicationContext) }
    val mapViewModelState = mapViewModel.stateFlow.collectAsStateWithLifecycle()
    val currentLocationState = mapViewModel.currentLocationFlow.collectAsStateWithLifecycle(null)
    val trackCaptureState = trackCaptureViewModel.stateFlow.collectAsStateWithLifecycle()

    MapLifecycle(
        onStart = {
            mapViewHolder.onStart()
        },
        onStop = {
            mapViewHolder.onStop()
            mapViewModel.add(MapEvent.PauseCurrentLocationUpdate)
        },
        onResume = {
            mapViewModel.add(MapEvent.ResumeCurrentLocationUpdate)
        }
    )
    MapPermissions(
        viewModelState = mapViewModelState.value,
        onLocationDisabled = { message, actionLabel ->
            coroutineScope.launch {
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
        },
    )
    Box {
        MapContent(
            onUpdateCurrentLocation = { geolocation ->
                if (trackCaptureState.value.status !is TrackCaptureStatus.Run) {
                    mapViewHolder.currentLocationPlacemark(geolocation)
                }
            },
            mapViewFactory = { _ ->
                val mapView = mapViewHolder.mapView
                mapView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                mapView.setNoninteractive(false)
                mapView
            },
            onSharing = {
                val geolocation = currentLocationState.value
                if (geolocation != null) {
                    try {
                        ShareCompat.IntentBuilder(context)
                            .setType("text/plain")
                            .setText(
                                resources.getString(
                                    R.string.feature_map_current_location_share_text,
                                    geolocation.latitude,
                                    geolocation.longitude
                                )
                            )
                            .setChooserTitle(resources.getString(R.string.feature_map_current_location_share_title))
                            .startChooser()
                    } catch (_: ActivityNotFoundException) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                resources.getString(
                                    io.mityukov.geo.tracking.core.ui.R.string.core_ui_error_sharing
                                )
                            )
                        }
                    }
                }
            },
            onPendingLocation = {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(resources.getString(R.string.feature_map_update_location))
                }
            },
            onPendingLocationComplete = {
                snackbarHostState.currentSnackbarData?.dismiss()
            },
            mapViewModelState = mapViewModelState.value,
        )
        MapControls(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            onNavigateTo = { geolocation ->
                mapViewHolder.navigateTo(geolocation)
            },
            onZoomIn = {
                mapViewHolder.zoomIn()
            },
            onZoomOut = {
                mapViewHolder.zoomOut()
            },
            currentGeolocation = currentLocationState.value,
        )
        TrackCapture(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(start = 16.dp, end = 16.dp, bottom = 48.dp),
            trackCaptureState = trackCaptureState.value,
            onStartCapture = {
                trackCaptureViewModel.add(TrackCaptureEvent.StartCapture)
            },
            onStopCapture = {
                mapViewHolder.clearMap()
                val currentLocation = currentLocationState.value
                if (currentLocation != null) {
                    mapViewHolder.currentLocationPlacemark(currentLocation)
                }
                trackCaptureViewModel.add(TrackCaptureEvent.StopCapture)
            },
            onPlayCapture = {
                trackCaptureViewModel.add(TrackCaptureEvent.PlayCapture)
            },
            onPauseCapture = {
                trackCaptureViewModel.add(TrackCaptureEvent.PauseCapture)
            },
            onUpdateTrack = { geolocations ->
                mapViewHolder.updateTrack(geolocations)
            },
        )
    }
}

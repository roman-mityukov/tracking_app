package io.mityukov.geo.tracking.feature.map

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.app.AppProps
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationUpdateException
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationUpdatesRepository
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackCaptureStatus
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackCapturerController
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackInProgress
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.utils.geolocation.toDomainGeolocation
import io.mityukov.geo.tracking.utils.log.logd
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MapEvent {
    data object PauseCurrentLocationUpdate : MapEvent
    data object ResumeCurrentLocationUpdate : MapEvent
}

sealed interface MapState {
    data object PendingLocationUpdates : MapState
    data class CurrentLocation(
        val data: Geolocation,
        val timestamp: Long = System.currentTimeMillis()
    ) : MapState

    data class CurrentTrack(
        val trackInProgress: TrackInProgress,
        val geolocations: List<Geolocation>,
        val currentLocation: Geolocation?,
    ) : MapState

    data object CurrentTrackError : MapState

    data class NoLocation(val cause: GeolocationUpdateException?) : MapState
}

@HiltViewModel
class MapViewModel @Inject constructor(
    private val geolocationUpdatesRepository: GeolocationUpdatesRepository,
    private val trackCapturerController: TrackCapturerController,
    private val tracksRepository: TracksRepository,
) :
    ViewModel() {
    private var lastKnownLocation: Geolocation? = null

    val stateFlow: StateFlow<MapState> = trackCapturerController.status
        .combine(geolocationUpdatesRepository.currentLocation) { trackCaptureStatus, currentLocation ->
            if (currentLocation.geolocation != null) {
                lastKnownLocation = currentLocation.geolocation
            }

            val state = when (trackCaptureStatus) {
                is TrackCaptureStatus.Run -> {
                    //stopCurrentLocationUpdates()
                    lastKnownLocation =
                        trackCaptureStatus.trackInProgress.lastLocation?.toDomainGeolocation()
                            ?: currentLocation.geolocation

                    val geolocations = tracksRepository.getAllGeolocations()
                    MapState.CurrentTrack(
                        trackCaptureStatus.trackInProgress,
                        geolocations,
                        lastKnownLocation,
                    )
                }

                is TrackCaptureStatus.Error -> {
                    MapState.CurrentTrackError
                }

                is TrackCaptureStatus.Idle -> {
                    //startCurrentLocationUpdates()
                    val geolocation = lastKnownLocation
                    if (geolocation != null) {
                        MapState.CurrentLocation(data = geolocation)
                    } else {
                        MapState.NoLocation(cause = currentLocation.error)
                    }
                }
            }
            logd("MapViewModel state $state")
            state
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = AppProps.STOP_TIMEOUT_MILLISECONDS),
            initialValue = MapState.PendingLocationUpdates
        )
    val currentLocationFlow =
        stateFlow.filter { it is MapState.CurrentLocation || it is MapState.CurrentTrack }
            .map {
                val geolocation = when (it) {
                    is MapState.CurrentLocation -> it.data
                    is MapState.CurrentTrack -> it.currentLocation
                    else -> null
                }
                geolocation
            }


    // Пермишены на локацию проверяются в MapScreen
    fun add(event: MapEvent) {
        when (event) {
            MapEvent.PauseCurrentLocationUpdate -> {
                viewModelScope.launch {
                    stopCurrentLocationUpdates()
                }
            }

            MapEvent.ResumeCurrentLocationUpdate -> {
                viewModelScope.launch {
                    startCurrentLocationUpdates()
                    val status = trackCapturerController.status.first()
                    if (status is TrackCaptureStatus.Error) {
                        trackCapturerController.bind()
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun startCurrentLocationUpdates() {
        geolocationUpdatesRepository.start()
    }

    private suspend fun stopCurrentLocationUpdates() {
        geolocationUpdatesRepository.stop()
    }
}

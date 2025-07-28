package io.mityukov.geo.tracking.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationUpdateException
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationUpdatesRepository
import io.mityukov.geo.tracking.core.data.repository.track.TrackCaptureService
import io.mityukov.geo.tracking.core.data.repository.track.TrackCaptureStatus
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.core.model.track.Track
import io.mityukov.geo.tracking.utils.log.logd
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

sealed interface MapEvent {
    data object GetCurrentLocation : MapEvent
    data object CurrentLocationConsumed : MapEvent
}

sealed interface MapState {
    data object PendingLocationUpdates : MapState
    data class CurrentLocation(
        val data: Geolocation,
        val timestamp: Long = System.currentTimeMillis()
    ) : MapState

    data class CurrentTrack(
        val track: Track,
        val status: TrackCaptureStatus.Run,
        val currentLocation: Geolocation?,
        val timestamp: Long = System.currentTimeMillis()
    ) : MapState

    data class NoLocation(val cause: GeolocationUpdateException?) : MapState
}

@HiltViewModel
class MapViewModel @Inject constructor(
    geolocationUpdatesRepository: GeolocationUpdatesRepository,
    trackCaptureService: TrackCaptureService,
) :
    ViewModel() {
    private var lastKnownLocation: Geolocation? = null

    private val mutableStateFlow = MutableStateFlow<MapState?>(null)

    val stateFlow: StateFlow<MapState> = trackCaptureService.status
        .combine(mutableStateFlow) { trackCaptureStatus, mutableState ->
            Pair(trackCaptureStatus, mutableState)
        }
        .combine(
            geolocationUpdatesRepository.getGeolocationUpdates()
        ) { pair, currentLocation ->
            val (trackCaptureStatus, mutableState) = pair

            val state = mutableState
                ?: if (trackCaptureStatus is TrackCaptureStatus.Run) {
                    MapState.CurrentTrack(
                        trackCaptureStatus.track,
                        trackCaptureStatus,
                        currentLocation.geolocation
                    )
                } else {
                    if (currentLocation.geolocation != null) {
                        lastKnownLocation = currentLocation.geolocation
                        logd("MapViewModel lastKnownLocation $lastKnownLocation")
                        MapState.CurrentLocation(data = currentLocation.geolocation)
                    } else {
                        MapState.NoLocation(cause = currentLocation.error)
                    }
                }
            logd("current state $state")
            state
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
            initialValue = MapState.PendingLocationUpdates
        )

    fun add(event: MapEvent) {
        when (event) {
            MapEvent.GetCurrentLocation -> {
                lastKnownLocation?.let { geolocation ->
                    mutableStateFlow.update {
                        MapState.CurrentLocation(data = geolocation)
                    }
                }
            }

            MapEvent.CurrentLocationConsumed -> {
                mutableStateFlow.update {
                    null
                }
            }
        }
    }
}

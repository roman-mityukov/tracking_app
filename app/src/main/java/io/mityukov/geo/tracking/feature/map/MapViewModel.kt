package io.mityukov.geo.tracking.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationUpdateException
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationUpdatesRepository
import io.mityukov.geo.tracking.core.data.repository.track.TrackCaptureService
import io.mityukov.geo.tracking.core.data.repository.track.TrackCaptureStatus
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.core.model.track.Track
import io.mityukov.geo.tracking.utils.log.logd
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MapEvent {
    data object GetCurrentLocation : MapEvent
    data object StartUpdateCurrentLocation : MapEvent
    data object StopUpdateCurrentLocation : MapEvent
}

sealed interface MapState {
    data object PendingLocationUpdates : MapState
    data class CurrentLocation(
        val data: Geolocation,
        val timestamp: Long = System.currentTimeMillis()
    ) : MapState

    data class CurrentTrack(val track: Track) : MapState

    data class NoLocation(val cause: GeolocationUpdateException?) : MapState
}

@HiltViewModel
class MapViewModel @Inject constructor(
    private val geolocationUpdatesRepository: GeolocationUpdatesRepository,
    private val trackCaptureService: TrackCaptureService,
    private val tracksRepository: TracksRepository,
) :
    ViewModel() {
    private var lastKnownLocation: Geolocation? = null
    private var geoUpdatesSubscriptionJob: Job? = null

    private val mutableStateFlow = MutableStateFlow<MapState>(MapState.PendingLocationUpdates)
    val stateFlow = mutableStateFlow.asStateFlow()

    private var trackSubscriptionJob: Job? = null

    init {
        viewModelScope.launch {
            trackCaptureService.status.collect { status ->
                if (status is TrackCaptureStatus.Running) {
                    geoUpdatesSubscriptionJob?.cancel()
                    geoUpdatesSubscriptionJob = null
                    trackSubscriptionJob?.cancel()
                    trackSubscriptionJob = viewModelScope.launch {
                        tracksRepository.getTrack(status.track.id).collect { track ->
                            mutableStateFlow.update {
                                MapState.CurrentTrack(track)
                            }
                        }
                    }
                } else {
                    add(MapEvent.StartUpdateCurrentLocation)
                }
            }
        }
    }

    fun add(event: MapEvent) {
        when (event) {
            MapEvent.GetCurrentLocation -> {
                lastKnownLocation?.let { geolocation ->
                    mutableStateFlow.update {
                        MapState.CurrentLocation(data = geolocation)
                    }
                }
            }

            MapEvent.StartUpdateCurrentLocation -> {
                if (geoUpdatesSubscriptionJob != null) {
                    return
                }

                mutableStateFlow.update { MapState.PendingLocationUpdates }

                geoUpdatesSubscriptionJob = viewModelScope.launch {
                    geolocationUpdatesRepository.getGeolocationUpdates()
                        .collect { geolocationUpdateResult ->
                            mutableStateFlow.update {
                                if (geolocationUpdateResult.geolocation != null) {
                                    lastKnownLocation = geolocationUpdateResult.geolocation
                                    logd("MapViewModel lastKnownLocation $lastKnownLocation")
                                    MapState.CurrentLocation(data = geolocationUpdateResult.geolocation)
                                } else {
                                    when (geolocationUpdateResult.error) {
                                        GeolocationUpdateException.LocationDisabled, GeolocationUpdateException.PermissionsNotGranted -> {
                                            unsubscribe()
                                        }

                                        else -> {
                                            // no op
                                        }
                                    }
                                    MapState.NoLocation(cause = geolocationUpdateResult.error)
                                }
                            }
                        }
                }
                logd("subscription is successful")
            }

            MapEvent.StopUpdateCurrentLocation -> {
                unsubscribe()
            }
        }
    }

    private fun unsubscribe() {
        geoUpdatesSubscriptionJob?.cancel()
        geoUpdatesSubscriptionJob = null
    }

    override fun onCleared() {
        super.onCleared()
        unsubscribe()
    }
}
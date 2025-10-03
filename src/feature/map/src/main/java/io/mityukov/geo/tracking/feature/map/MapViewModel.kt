package io.mityukov.geo.tracking.feature.map

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationUpdateException
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationUpdatesRepository
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.log.logd
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
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

    data class NoLocation(val cause: GeolocationUpdateException?) : MapState
}

@HiltViewModel
class MapViewModel @Inject constructor(
    private val geolocationUpdatesRepository: GeolocationUpdatesRepository,
) :
    ViewModel() {
    private var lastKnownLocation: Geolocation? = null
    private var repositorySubscription: Job? = null
    private val mutableStateFlow = MutableStateFlow<MapState>(MapState.PendingLocationUpdates)
    val stateFlow: StateFlow<MapState> = mutableStateFlow.asStateFlow()
    val currentLocationFlow: Flow<Geolocation?> =
        stateFlow.filter {
            it is MapState.CurrentLocation
        }
            .map {
                val geolocation = when (it) {
                    is MapState.CurrentLocation -> it.data
                    else -> null
                }
                geolocation
            }


    // Пермишены на локацию проверяются в UI
    @SuppressLint("MissingPermission")
    fun add(event: MapEvent) {
        when (event) {
            MapEvent.PauseCurrentLocationUpdate -> {
                repositorySubscription?.cancel()
                repositorySubscription = null
            }

            MapEvent.ResumeCurrentLocationUpdate -> {
                viewModelScope.launch {
                    repositorySubscription = launch {
                        geolocationUpdatesRepository.currentLocation
                            .map { currentLocation ->
                                val geolocation = currentLocation.geolocation
                                val state = if (geolocation != null) {
                                    lastKnownLocation = geolocation
                                    MapState.CurrentLocation(data = geolocation)
                                } else {
                                    MapState.NoLocation(cause = currentLocation.error)
                                }
                                logd("MapViewModel state $state")
                                state
                            }.collect { mapState ->
                                mutableStateFlow.update {
                                    mapState
                                }
                            }
                    }
                }
            }
        }
    }
}

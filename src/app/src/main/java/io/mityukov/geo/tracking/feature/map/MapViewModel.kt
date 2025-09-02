package io.mityukov.geo.tracking.feature.map

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.app.AppProps
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationUpdateException
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationUpdatesRepository
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.utils.log.logd
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
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

    data class NoLocation(val cause: GeolocationUpdateException?) : MapState
}

@HiltViewModel
class MapViewModel @Inject constructor(
    private val geolocationUpdatesRepository: GeolocationUpdatesRepository,
) :
    ViewModel() {
    private var lastKnownLocation: Geolocation? = null

    val stateFlow: StateFlow<MapState> = geolocationUpdatesRepository.currentLocation
        .map { currentLocation ->
            val state = if (currentLocation.geolocation != null) {
                lastKnownLocation = currentLocation.geolocation
                MapState.CurrentLocation(data = currentLocation.geolocation)
            } else {
                MapState.NoLocation(cause = currentLocation.error)
            }
            logd("MapViewModel state $state")
            state
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = AppProps.STOP_TIMEOUT_MILLISECONDS),
            initialValue = MapState.PendingLocationUpdates
        )
    val currentLocationFlow =
        stateFlow.filter { it is MapState.CurrentLocation }
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
                viewModelScope.launch {
                    geolocationUpdatesRepository.stop()
                }
            }

            MapEvent.ResumeCurrentLocationUpdate -> {
                viewModelScope.launch {
                    geolocationUpdatesRepository.start()
                }
            }
        }
    }
}

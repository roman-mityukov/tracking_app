package io.mityukov.geo.tracking.feature.track.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.app.AppProps
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackCaptureStatus
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackCapturerController
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TrackCaptureEvent {
    data object StartCapture : TrackCaptureEvent
    data object PlayCapture : TrackCaptureEvent
    data object PauseCapture : TrackCaptureEvent
    data object StopCapture : TrackCaptureEvent
}

data class TrackCaptureState(
    val status: TrackCaptureStatus,
    val geolocations: List<Geolocation> = listOf(),
)

@HiltViewModel
class TrackCaptureViewModel @Inject constructor(
    private val trackCapturerController: TrackCapturerController,
    private val tracksRepository: TracksRepository,
) : ViewModel() {
    val stateFlow = trackCapturerController.status
        .map {
            val state = when (it) {
                TrackCaptureStatus.Error -> TrackCaptureState(it)
                TrackCaptureStatus.Idle -> TrackCaptureState(it)
                is TrackCaptureStatus.Run -> {
                    val geolocations = tracksRepository.getAllGeolocations()
                    TrackCaptureState(it, geolocations)
                }
            }
            state
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(stopTimeoutMillis = AppProps.STOP_TIMEOUT_MILLISECONDS),
            TrackCaptureState(TrackCaptureStatus.Idle)
        )

    init {
        viewModelScope.launch {
            trackCapturerController.bind()
        }
    }

    fun add(event: TrackCaptureEvent) {
        viewModelScope.launch {
            when (event) {
                TrackCaptureEvent.StartCapture -> {
                    trackCapturerController.start()
                }

                TrackCaptureEvent.StopCapture -> {
                    trackCapturerController.stop()
                }

                TrackCaptureEvent.PauseCapture -> {
                    trackCapturerController.pause()
                }

                TrackCaptureEvent.PlayCapture -> {
                    trackCapturerController.resume()
                }
            }
        }
    }
}

package io.mityukov.geo.tracking.feature.track.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.core.data.repository.track.TrackCaptureController
import io.mityukov.geo.tracking.core.data.repository.track.TrackCaptureStatus
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

data class TrackCaptureState(val status: TrackCaptureStatus)

@HiltViewModel
class TrackCaptureViewModel @Inject constructor(
    private val trackCaptureController: TrackCaptureController,
) : ViewModel() {
    val stateFlow = trackCaptureController.status
        .map {
            TrackCaptureState(it)
        }
        .stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
            TrackCaptureState(TrackCaptureStatus.Idle)
        )

    init {
        viewModelScope.launch {
            trackCaptureController.bind()
        }
    }

    fun add(event: TrackCaptureEvent) {
        when (event) {
            TrackCaptureEvent.StartCapture -> {
                viewModelScope.launch {
                    trackCaptureController.start()
                }
            }

            TrackCaptureEvent.StopCapture -> {
                viewModelScope.launch {
                    trackCaptureController.stop()
                }
            }

            TrackCaptureEvent.PauseCapture -> {
                viewModelScope.launch {
                    trackCaptureController.pause()
                }
            }

            TrackCaptureEvent.PlayCapture -> {
                viewModelScope.launch {
                    trackCaptureController.resume()
                }
            }
        }
    }
}

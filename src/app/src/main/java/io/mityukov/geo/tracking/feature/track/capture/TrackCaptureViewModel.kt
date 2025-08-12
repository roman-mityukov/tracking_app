package io.mityukov.geo.tracking.feature.track.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.app.AppProps
import io.mityukov.geo.tracking.core.data.repository.track.TrackCapturerController
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
    private val trackCapturerController: TrackCapturerController,
) : ViewModel() {
    val stateFlow = trackCapturerController.status
        .map {
            TrackCaptureState(it)
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

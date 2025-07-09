package io.mityukov.geo.tracking.feature.track.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.core.data.repository.track.TrackCaptureService
import io.mityukov.geo.tracking.core.data.repository.track.TrackCaptureStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TrackCaptureEvent {
    data object Switch : TrackCaptureEvent
}

data class TrackCaptureState(val status: TrackCaptureStatus)

@HiltViewModel
class TrackCaptureViewModel @Inject constructor(
    private val trackCaptureService: TrackCaptureService,
) : ViewModel() {
    val stateFlow = trackCaptureService.status
        .map {
            TrackCaptureState(it)
        }
        .stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
            TrackCaptureState(TrackCaptureStatus.Idle)
        )

    init {
        viewModelScope.launch {
            trackCaptureService.bind()
        }
    }

    fun add(event: TrackCaptureEvent) {
        when (event) {
            TrackCaptureEvent.Switch -> {
                viewModelScope.launch {
                    trackCaptureService.switch()
                }
            }
        }
    }
}
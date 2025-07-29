package io.mityukov.geo.tracking.feature.track.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.core.data.repository.track.TrackCaptureController
import io.mityukov.geo.tracking.core.data.repository.track.TrackCaptureStatus
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.core.model.track.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TracksState {
    data object Pending : TracksState
    data class Data(val tracks: List<Track>, val capturedTrackId: String?, val paused: Boolean) :
        TracksState
}

@HiltViewModel
class TracksViewModel @Inject constructor(
    private val tracksRepository: TracksRepository,
    private val trackCaptureController: TrackCaptureController,
) :
    ViewModel() {
    private val mutableStateFlow =
        MutableStateFlow<TracksState>(TracksState.Pending)
    val stateFlow = mutableStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            tracksRepository.refreshTracks().combine(trackCaptureController.status) { tracks, status ->
                TracksState.Data(
                    tracks = tracks,
                    capturedTrackId = (status as? TrackCaptureStatus.Run)?.track?.id,
                    paused = (status as? TrackCaptureStatus.Run)?.paused ?: false
                )
            }.collect { state: TracksState.Data ->
                mutableStateFlow.update {
                    state
                }
            }
        }
    }
}

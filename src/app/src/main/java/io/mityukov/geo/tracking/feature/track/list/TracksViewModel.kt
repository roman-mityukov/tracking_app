package io.mityukov.geo.tracking.feature.track.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.app.AppProps
import io.mityukov.geo.tracking.core.data.repository.track.TrackCaptureController
import io.mityukov.geo.tracking.core.data.repository.track.TrackCaptureStatus
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.core.model.track.Track
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface TracksState {
    data object Pending : TracksState
    data class Data(val tracks: List<Track>, val capturedTrackId: String?, val paused: Boolean) :
        TracksState
}

@HiltViewModel
class TracksViewModel @Inject constructor(
    tracksRepository: TracksRepository,
    trackCaptureController: TrackCaptureController,
) : ViewModel() {
    val stateFlow =
        tracksRepository.refreshTracks()
            .combine(trackCaptureController.status) { tracks, status ->
                TracksState.Data(
                    tracks = tracks,
                    capturedTrackId = (status as? TrackCaptureStatus.Run)?.track?.id,
                    paused = (status as? TrackCaptureStatus.Run)?.paused ?: false
                )
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(stopTimeoutMillis = AppProps.STOP_TIMEOUT_MILLISECONDS),
                TracksState.Pending
            )
}

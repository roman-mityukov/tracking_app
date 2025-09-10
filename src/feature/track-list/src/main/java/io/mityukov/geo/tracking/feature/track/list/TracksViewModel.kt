package io.mityukov.geo.tracking.feature.track.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.core.common.CommonAppProps
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.core.model.track.Track
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface TracksState {
    data object Pending : TracksState
    data class Data(
        val tracks: List<Track>,
    ) :
        TracksState
}

@HiltViewModel
class TracksViewModel @Inject constructor(tracksRepository: TracksRepository) : ViewModel() {
    val stateFlow =
        tracksRepository.tracks
            .map { tracks ->
                TracksState.Data(tracks = tracks)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(stopTimeoutMillis = CommonAppProps.STOP_TIMEOUT_MILLISECONDS),
                TracksState.Pending
            )
}

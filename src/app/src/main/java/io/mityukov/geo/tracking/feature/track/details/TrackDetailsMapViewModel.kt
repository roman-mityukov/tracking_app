package io.mityukov.geo.tracking.feature.track.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.app.AppProps
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.feature.home.HomeRouteTrackDetailsMap
import io.mityukov.geo.tracking.feature.track.list.CompletedTrack
import io.mityukov.geo.tracking.feature.track.list.toCompletedTrack
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface TrackDetailsMapState {
    data object Pending : TrackDetailsMapState
    data class Data(val data: CompletedTrack) : TrackDetailsMapState
}

@HiltViewModel
class TrackDetailsMapViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    tracksRepository: TracksRepository,
) : ViewModel() {
    val stateFlow =
        tracksRepository
            .getTrack(
                savedStateHandle.toRoute<HomeRouteTrackDetailsMap>().trackId
            ).map { track ->
                TrackDetailsMapState.Data(track.toCompletedTrack())
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(stopTimeoutMillis = AppProps.STOP_TIMEOUT_MILLISECONDS),
                TrackDetailsMapState.Pending
            )
}

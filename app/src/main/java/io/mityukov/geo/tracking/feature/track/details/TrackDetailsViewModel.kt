package io.mityukov.geo.tracking.feature.track.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.core.model.track.Track
import io.mityukov.geo.tracking.feature.home.HomeRouteTrackDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TrackDetailsEvent {
    data object Delete : TrackDetailsEvent
}

sealed interface TrackDetailsState {
    data object Pending : TrackDetailsState
    data class Data(val data: Track) : TrackDetailsState
    data object DeleteCompleted : TrackDetailsState
}

@HiltViewModel
class TrackDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tracksRepository: TracksRepository,
) : ViewModel() {
    private val routeTrackDetails = savedStateHandle.toRoute<HomeRouteTrackDetails>()
    private val mutableStateFlow = MutableStateFlow<TrackDetailsState>(TrackDetailsState.Pending)
    val stateFlow = mutableStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            val track = tracksRepository.getTrack(routeTrackDetails.trackId).first()
            mutableStateFlow.update {
                TrackDetailsState.Data(track)
            }
        }
    }

    fun add(event: TrackDetailsEvent) {
        when (event) {
            TrackDetailsEvent.Delete -> {
                viewModelScope.launch {
                    tracksRepository.deleteTrack(routeTrackDetails.trackId)
                    mutableStateFlow.update {
                        TrackDetailsState.DeleteCompleted
                    }
                }
            }
        }
    }
}
package io.mityukov.geo.tracking.feature.track.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.core.model.track.DetailedTrack
import io.mityukov.geo.tracking.core.sharing.TrackShareService
import io.mityukov.geo.tracking.feature.track.details.navigation.TrackDetailsRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TrackDetailsEvent {
    data object Delete : TrackDetailsEvent
    data object Share : TrackDetailsEvent
    data object ConsumeShare : TrackDetailsEvent
}

sealed interface TrackDetailsState {
    data object Pending : TrackDetailsState
    data class Data(val detailedTrack: DetailedTrack) : TrackDetailsState
    data object DeleteCompleted : TrackDetailsState
}

@HiltViewModel
class TrackDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tracksRepository: TracksRepository,
    private val trackShareService: TrackShareService,
) : ViewModel() {
    private val routeTrackDetails = savedStateHandle.toRoute<TrackDetailsRoute>()
    private val mutableStateFlow = MutableStateFlow<TrackDetailsState>(TrackDetailsState.Pending)
    val stateFlow = mutableStateFlow.asStateFlow()

    private val sharingMutableStateFlow = MutableStateFlow<String?>(null)
    val sharingStateFlow: StateFlow<String?> = sharingMutableStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            val completedTrack = tracksRepository.getDetailedTrack(routeTrackDetails.trackId)
            mutableStateFlow.update {
                TrackDetailsState.Data(completedTrack)
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

            TrackDetailsEvent.Share -> {
                viewModelScope.launch {
                    val track = tracksRepository.getTrack(routeTrackDetails.trackId)
                    val path = trackShareService.prepareTrackFile(track)
                    sharingMutableStateFlow.update {
                        path
                    }
                }
            }

            TrackDetailsEvent.ConsumeShare -> {
                sharingMutableStateFlow.update {
                    null
                }
            }
        }
    }
}

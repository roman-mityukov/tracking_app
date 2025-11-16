package io.mityukov.geo.tracking.feature.track.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.core.model.track.DetailedTrack
import io.mityukov.geo.tracking.core.sharing.TrackShareService
import io.mityukov.geo.tracking.feature.track.details.navigation.TrackDetailsRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal sealed interface TrackDetailsEvent {
    data object Delete : TrackDetailsEvent
    data object Share : TrackDetailsEvent
    data object ConsumeShare : TrackDetailsEvent
}

internal sealed interface TrackDetailsState {
    data object Pending : TrackDetailsState
    data class Data(val detailedTrack: DetailedTrack) : TrackDetailsState
    data object Failure : TrackDetailsState
    data object DeleteCompleted : TrackDetailsState
    data object DeleteFailed : TrackDetailsState
}

@HiltViewModel(assistedFactory = TrackDetailsViewModel.Factory::class)
internal class TrackDetailsViewModel @AssistedInject constructor(
    @Assisted private val route: TrackDetailsRoute,
    private val tracksRepository: TracksRepository,
    private val trackShareService: TrackShareService,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(navKey: TrackDetailsRoute): TrackDetailsViewModel
    }
    private val mutableStateFlow = MutableStateFlow<TrackDetailsState>(TrackDetailsState.Pending)
    val stateFlow = mutableStateFlow.asStateFlow()

    private val sharingMutableStateFlow = MutableStateFlow<String?>(null)
    val sharingStateFlow: StateFlow<String?> = sharingMutableStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            tracksRepository.readTrack(route.trackId).collect {
                tracksRepository.readDetailedTrack(route.trackId)
                    .onSuccess { detailedTrack ->
                        mutableStateFlow.update {
                            TrackDetailsState.Data(detailedTrack)
                        }
                    }.onFailure {
                        mutableStateFlow.update {
                            TrackDetailsState.Failure
                        }
                    }
            }
        }
    }

    fun add(event: TrackDetailsEvent) {
        when (event) {
            TrackDetailsEvent.Delete -> {
                viewModelScope.launch {
                    tracksRepository.deleteTrack(route.trackId)
                        .onSuccess {
                            mutableStateFlow.update {
                                TrackDetailsState.DeleteCompleted
                            }
                        }
                        .onFailure {
                            mutableStateFlow.update {
                                TrackDetailsState.DeleteFailed
                            }
                        }

                }
            }

            TrackDetailsEvent.Share -> {
                viewModelScope.launch {
                    val track = tracksRepository.readTrack(route.trackId).first()
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

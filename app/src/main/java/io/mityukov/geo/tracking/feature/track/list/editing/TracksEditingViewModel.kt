package io.mityukov.geo.tracking.feature.track.list.editing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.core.model.track.Track
import io.mityukov.geo.tracking.feature.home.HomeRouteTracksEditing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TracksEditingEvent {
    data object Delete : TracksEditingEvent
    data class ChangeSelection(val trackId: String) : TracksEditingEvent
}

sealed interface TracksEditingState {
    data class Data(val allTracks: List<Track>, val selectedTracks: List<Track>) :
        TracksEditingState

    data object DeletionComplete : TracksEditingState
}

@HiltViewModel
class TracksEditingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tracksRepository: TracksRepository
) :
    ViewModel() {
    private val routeTracksEditing = savedStateHandle.toRoute<HomeRouteTracksEditing>()
    private val selectedTracks = mutableListOf<String>(routeTracksEditing.trackId)
    private val mutableStateFlow = MutableStateFlow<TracksEditingState>(
        TracksEditingState.Data(
            listOf(), listOf()
        )
    )
    val stateFlow = mutableStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            val tracks = tracksRepository.getTracks().first()
            mutableStateFlow.update {
                TracksEditingState.Data(
                    allTracks = tracks,
                    selectedTracks = tracks.filter { track ->
                        selectedTracks.contains(track.id)
                    }
                )
            }
        }
    }

    fun add(event: TracksEditingEvent) {
        when (event) {
            is TracksEditingEvent.ChangeSelection -> {
                viewModelScope.launch {
                    if (selectedTracks.contains(event.trackId)) {
                        selectedTracks.remove(event.trackId)
                    } else {
                        selectedTracks.add(event.trackId)
                    }

                    val tracks = tracksRepository.getTracks().first()
                    mutableStateFlow.update {
                        TracksEditingState.Data(
                            allTracks = tracks,
                            selectedTracks = tracks.filter { track ->
                                selectedTracks.any { selectedTrackId ->
                                    selectedTrackId == track.id
                                }
                            }
                        )
                    }
                }
            }

            TracksEditingEvent.Delete -> {
                viewModelScope.launch {
                    selectedTracks.forEach {
                        tracksRepository.deleteTrack(it)
                    }

                    mutableStateFlow.update {
                        TracksEditingState.DeletionComplete
                    }
                }
            }
        }
    }
}
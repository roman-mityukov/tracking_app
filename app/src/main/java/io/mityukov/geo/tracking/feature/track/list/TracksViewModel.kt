package io.mityukov.geo.tracking.feature.track.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.core.model.track.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TracksState {
    data object Pending : TracksState
    data class Data(val tracks: List<Track>) : TracksState
}

@HiltViewModel
class TracksViewModel @Inject constructor(private val tracksRepository: TracksRepository) :
    ViewModel() {
    private val mutableStateFlow =
        MutableStateFlow<TracksState>(TracksState.Pending)
    val stateFlow = mutableStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            tracksRepository.refreshTracks().collect { tracks ->
                mutableStateFlow.update {
                    TracksState.Data(tracks = tracks)
                }
            }
        }
    }
}
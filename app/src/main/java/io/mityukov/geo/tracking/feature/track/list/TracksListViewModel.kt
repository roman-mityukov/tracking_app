package io.mityukov.geo.tracking.feature.track.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.core.model.track.Track
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface TrackListEvent {}

sealed interface TrackListState {
    data class Data(val tracks: List<Track>)
}

@HiltViewModel
class TracksListViewModel @Inject constructor(private val tracksRepository: TracksRepository) :
    ViewModel() {
    val stateFlow = tracksRepository.getTracks().map {
        TrackListState.Data(it)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
        TrackListState.Data(listOf())
    )
}
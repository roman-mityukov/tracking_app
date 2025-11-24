package io.mityukov.geo.tracking.feature.track.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.core.common.CommonAppProps
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.core.model.track.DetailedTrack
import io.mityukov.geo.tracking.feature.track.details.navigation.TrackDetailsMapRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

internal sealed interface TrackDetailsMapState {
    data object Pending : TrackDetailsMapState
    data class Data(val data: DetailedTrack) : TrackDetailsMapState
    data object Failure : TrackDetailsMapState
}

@HiltViewModel(assistedFactory = TrackDetailsMapViewModel.Factory::class)
internal class TrackDetailsMapViewModel @AssistedInject constructor(
    @Assisted route: TrackDetailsMapRoute,
    tracksRepository: TracksRepository,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(navKey: TrackDetailsMapRoute): TrackDetailsMapViewModel
    }

    val stateFlow =
        flow<TrackDetailsMapState> {
            tracksRepository
                .readDetailedTrack(
                    route.trackId
                ).onSuccess {
                    emit(TrackDetailsMapState.Data(it))
                }.onFailure {
                    emit(TrackDetailsMapState.Failure)
                }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(stopTimeoutMillis = CommonAppProps.STOP_TIMEOUT_MILLISECONDS),
            TrackDetailsMapState.Pending
        )
}

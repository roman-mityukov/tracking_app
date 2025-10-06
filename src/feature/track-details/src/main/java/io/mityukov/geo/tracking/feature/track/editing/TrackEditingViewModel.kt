package io.mityukov.geo.tracking.feature.track.editing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.core.data.validation.TrackValidationResult
import io.mityukov.geo.tracking.core.data.validation.TrackValidator
import io.mityukov.geo.tracking.core.model.track.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TrackEditingEvent {
    data class Save(val track: Track) : TrackEditingEvent
    data object ConsumeSaveCompleted : TrackEditingEvent
}

sealed interface TrackEditingState {
    data object Initial : TrackEditingState
    data object SaveCompleted : TrackEditingState
    data class SaveFailed(val trackValidationResult: TrackValidationResult.Invalid) :
        TrackEditingState
}

@HiltViewModel
class TrackEditingViewModel @Inject constructor(
    private val trackValidator: TrackValidator,
    private val tracksRepository: TracksRepository,
) : ViewModel() {
    private val mutableStateFlow = MutableStateFlow<TrackEditingState>(TrackEditingState.Initial)
    val stateFlow = mutableStateFlow.asStateFlow()

    fun add(event: TrackEditingEvent) {
        when (event) {
            is TrackEditingEvent.Save -> {
                viewModelScope.launch {
                    val trackValidationResult = trackValidator.validate(event.track)

                    if (trackValidationResult is TrackValidationResult.Valid) {
                        tracksRepository.updateTrack(track = event.track)
                        mutableStateFlow.update {
                            TrackEditingState.SaveCompleted
                        }
                    } else {
                        mutableStateFlow.update {
                            TrackEditingState.SaveFailed(trackValidationResult as TrackValidationResult.Invalid)
                        }
                    }
                }
            }

            TrackEditingEvent.ConsumeSaveCompleted -> {
                mutableStateFlow.update {
                    TrackEditingState.Initial
                }
            }
        }
    }
}

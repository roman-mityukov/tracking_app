package io.mityukov.geo.tracking.feature.track.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.app.AppProps
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.core.model.track.Track
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

//fun Track.toCompletedTrack(): CompletedTrack {
//    val duration = if (actions.size > 2) {
//        var pausedTimeStamp = ""
//        val pausedDuration = actions.fold(0.seconds) { value: Duration, action: TrackAction ->
//            logd("timestamp ${action.timestamp}")
//            when (action.type) {
//                TrackActionType.Pause -> {
//                    pausedTimeStamp = action.timestamp
//                    value
//                }
//
//                TrackActionType.Resume -> {
//                    val newValue = value + TimeUtils.durationBetween(
//                        pausedTimeStamp,
//                        action.timestamp
//                    )
//                    pausedTimeStamp = ""
//                    newValue
//                }
//
//                TrackActionType.Stop -> {
//                    if (pausedTimeStamp.isNotEmpty()) {
//                        val newValue = value + TimeUtils.durationBetween(
//                            pausedTimeStamp,
//                            action.timestamp
//                        )
//                        newValue
//                    } else {
//                        value
//                    }
//                }
//
//                else -> {
//                    value
//                }
//            }
//        }
//        logd("pausedDuration $pausedDuration")
//        TimeUtils.durationBetween(start, end) - pausedDuration
//    } else {
//        TimeUtils.durationBetween(start, end)
//    }
//
//    return CompletedTrack(
//        id = id,
//        name = name,
//        start = start,
//        end = end,
//        distance = distance,
//        altitudeUp = altitudeUp,
//        altitudeDown = altitudeDown,
//        points = points,
//        duration = duration
//    )
//}

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
                SharingStarted.WhileSubscribed(stopTimeoutMillis = AppProps.STOP_TIMEOUT_MILLISECONDS),
                TracksState.Pending
            )
}

package io.mityukov.geo.tracking.feature.track.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.app.AppProps
import io.mityukov.geo.tracking.core.data.repository.track.TrackCaptureController
import io.mityukov.geo.tracking.core.data.repository.track.TrackCaptureStatus
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.core.model.track.Track
import io.mityukov.geo.tracking.core.model.track.TrackAction
import io.mityukov.geo.tracking.core.model.track.TrackActionType
import io.mityukov.geo.tracking.core.model.track.TrackPoint
import io.mityukov.geo.tracking.utils.log.logd
import io.mityukov.geo.tracking.utils.time.TimeUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun Track.toCompletedTrack(): CompletedTrack {
    val duration = if (actions.size > 2) {
        var pausedTimeStamp = ""
        val pausedDuration = actions.fold(0.seconds) { value: Duration, action: TrackAction ->
            logd("timestamp ${action.timestamp}" )
            when (action.type) {
                TrackActionType.Pause -> {
                    pausedTimeStamp = action.timestamp
                    value
                }

                TrackActionType.Resume -> {
                    val newValue = value + TimeUtils.durationBetween(
                        pausedTimeStamp,
                        action.timestamp
                    )
                    pausedTimeStamp = ""
                    newValue
                }

                TrackActionType.Stop -> {
                    if (pausedTimeStamp.isNotEmpty()) {
                        val newValue = value + TimeUtils.durationBetween(
                            pausedTimeStamp,
                            action.timestamp
                        )
                        newValue
                    } else {
                        value
                    }
                }

                else -> {
                    value
                }
            }
        }
        logd("pausedDuration $pausedDuration")
        TimeUtils.durationBetween(start, end) - pausedDuration
    } else {
        TimeUtils.durationBetween(start, end)
    }

    return CompletedTrack(
        id = id,
        start = start,
        end = end,
        distance = distance,
        altitudeUp = altitudeUp,
        altitudeDown = altitudeDown,
        points = points,
        duration = duration
    )
}

data class CompletedTrack(
    val id: String,
    val start: String,
    val end: String,
    val distance: Int,
    val altitudeUp: Int,
    val altitudeDown: Int,
    val points: List<TrackPoint>,
    val duration: Duration
)

sealed interface TracksState {
    data object Pending : TracksState
    data class Data(
        val tracks: List<CompletedTrack>,
        val capturedTrackId: String?,
        val paused: Boolean
    ) :
        TracksState
}

@HiltViewModel
class TracksViewModel @Inject constructor(
    tracksRepository: TracksRepository,
    trackCaptureController: TrackCaptureController,
) : ViewModel() {
    val stateFlow =
        tracksRepository.tracks
            .combine(trackCaptureController.status) { tracks, status ->
                TracksState.Data(
                    tracks = tracks.filter { it.isCompleted }.map { it.toCompletedTrack() },
                    capturedTrackId = (status as? TrackCaptureStatus.Run)?.track?.id,
                    paused = (status as? TrackCaptureStatus.Run)?.paused ?: false
                )
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(stopTimeoutMillis = AppProps.STOP_TIMEOUT_MILLISECONDS),
                TracksState.Pending
            )
}

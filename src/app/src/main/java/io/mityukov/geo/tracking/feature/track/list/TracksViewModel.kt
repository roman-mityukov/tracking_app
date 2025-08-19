package io.mityukov.geo.tracking.feature.track.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.app.AppProps
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.core.model.track.Track
import io.mityukov.geo.tracking.core.model.track.TrackAction
import io.mityukov.geo.tracking.core.model.track.TrackActionType
import io.mityukov.geo.tracking.core.model.track.TrackPoint
import io.mityukov.geo.tracking.utils.geolocation.distanceTo
import io.mityukov.geo.tracking.utils.log.logd
import io.mityukov.geo.tracking.utils.time.TimeUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun Track.toCompletedTrack(): CompletedTrack {
    val duration = if (actions.size > 2) {
        var pausedTimeStamp = ""
        val pausedDuration = actions.fold(0.seconds) { value: Duration, action: TrackAction ->
            logd("timestamp ${action.timestamp}")
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
) {
    val altitudeByDistance: List<Pair<Int, Int>> by lazy {
        val result = mutableListOf<Pair<Int, Int>>()
        var currentDistance = 0
        points.forEachIndexed { index, point ->
            if (index > 0) {
                val firstPoint = points[index - 1].geolocation
                val secondPoint = points[index].geolocation
                currentDistance += distanceTo(
                    firstPoint.latitude,
                    firstPoint.longitude,
                    firstPoint.altitude,
                    secondPoint.latitude,
                    secondPoint.longitude,
                    secondPoint.altitude
                )
                result.add(Pair(secondPoint.altitude.toInt(), currentDistance))
            } else {
                result.add(Pair(points[index].geolocation.altitude.toInt(), 0))
            }
        }
        result.toList()
    }
}

sealed interface TracksState {
    data object Pending : TracksState
    data class Data(
        val tracks: List<CompletedTrack>,
    ) :
        TracksState
}

@HiltViewModel
class TracksViewModel @Inject constructor(tracksRepository: TracksRepository) : ViewModel() {
    val stateFlow =
        tracksRepository.tracks.map { tracks ->
            TracksState.Data(
                tracks = tracks.filter { it.isCompleted }.map { it.toCompletedTrack() },
            )
        }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(stopTimeoutMillis = AppProps.STOP_TIMEOUT_MILLISECONDS),
                TracksState.Pending
            )
}

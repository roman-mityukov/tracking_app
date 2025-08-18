package io.mityukov.geo.tracking.feature.map

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.app.AppProps
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationUpdateException
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationUpdatesRepository
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackCaptureStatus
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackCapturerController
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.core.model.track.Track
import io.mityukov.geo.tracking.core.model.track.TrackAction
import io.mityukov.geo.tracking.core.model.track.TrackActionType
import io.mityukov.geo.tracking.core.model.track.TrackPoint
import io.mityukov.geo.tracking.utils.PausableTimer
import io.mityukov.geo.tracking.utils.time.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun Track.toTrackInProgress(currentTime: String): TrackInProgress {
    val duration = if (actions.size > 1) {
        var pausedTimeStamp = ""
        val pausedDuration = actions.fold(0.seconds) { value: Duration, action: TrackAction ->
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

                else -> {
                    value
                }
            }
        }

        val considerLastPause = if (actions.last().type == TrackActionType.Pause) {
            pausedDuration + TimeUtils.durationBetween(
                actions.last().timestamp,
                currentTime
            )
        } else {
            pausedDuration
        }

        TimeUtils.durationBetween(start, currentTime) - considerLastPause
    } else {
        TimeUtils.durationBetween(start, currentTime)
    }

    return TrackInProgress(
        id = id,
        start = start,
        end = end,
        distance = distance,
        altitudeUp = altitudeUp,
        altitudeDown = altitudeDown,
        points = points,
        duration = duration,
    )
}

data class TrackInProgress(
    val id: String,
    val start: String,
    val end: String,
    val distance: Int,
    val altitudeUp: Int,
    val altitudeDown: Int,
    val points: List<TrackPoint>,
    val duration: Duration
)

sealed interface MapEvent {
    data object PauseCurrentLocationUpdate : MapEvent
    data object ResumeCurrentLocationUpdate : MapEvent
    data object GetCurrentLocation : MapEvent
    data object CurrentLocationConsumed : MapEvent
}

sealed interface MapState {
    data object PendingLocationUpdates : MapState
    data class CurrentLocation(
        val data: Geolocation,
        val timestamp: Long = System.currentTimeMillis()
    ) : MapState

    data class CurrentTrack(
        val track: TrackInProgress,
        val status: TrackCaptureStatus.Run,
        val currentLocation: Geolocation?,
        val timestamp: Long = System.currentTimeMillis()
    ) : MapState

    data object CurrentTrackError : MapState

    data class NoLocation(val cause: GeolocationUpdateException?) : MapState
}

@HiltViewModel
class MapViewModel @Inject constructor(
    private val geolocationUpdatesRepository: GeolocationUpdatesRepository,
    private val trackCapturerController: TrackCapturerController,
) :
    ViewModel() {
    private var lastKnownLocation: Geolocation? = null

    private val mutableStateFlow = MutableStateFlow<MapState?>(null)
    private val timer = PausableTimer(coroutineScope = viewModelScope)

    init {
        timer.start()
    }

    val stateFlow: StateFlow<MapState> = trackCapturerController.status
        .combine(mutableStateFlow) { trackCaptureStatus, mutableState ->
            Pair(trackCaptureStatus, mutableState)
        }
        .combine(timer.events) { pair, timerEvent ->
            Triple(pair.first, pair.second, timerEvent)
        }
        .combine(
            geolocationUpdatesRepository.currentLocation
        ) { triple, currentLocation ->
            val (trackCaptureStatus, mutableState, timerEvent) = triple

            if (currentLocation.geolocation != null) {
                lastKnownLocation = currentLocation.geolocation
            }

            val state = mutableState
                ?: if (trackCaptureStatus is TrackCaptureStatus.Run) {
                    if (trackCaptureStatus.paused) {
                        timer.pause()
                    } else {
                        timer.resume()
                    }
                    MapState.CurrentTrack(
                        trackCaptureStatus.track.toTrackInProgress(TimeUtils.getCurrentUtcTime()),
                        trackCaptureStatus,
                        currentLocation.geolocation
                    )
                } else if (trackCaptureStatus is TrackCaptureStatus.Error) {
                    MapState.CurrentTrackError
                } else {
                    if (currentLocation.geolocation != null) {
                        MapState.CurrentLocation(data = currentLocation.geolocation)
                    } else {
                        MapState.NoLocation(cause = currentLocation.error)
                    }
                }
            state
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = AppProps.STOP_TIMEOUT_MILLISECONDS),
            initialValue = MapState.PendingLocationUpdates
        )


    @SuppressLint("MissingPermission")
    // Пермишены на локацию проверяются в MapScreen
    fun add(event: MapEvent) {
        when (event) {
            MapEvent.GetCurrentLocation -> {
                lastKnownLocation?.let { geolocation ->
                    mutableStateFlow.update {
                        MapState.CurrentLocation(data = geolocation)
                    }
                }
            }

            MapEvent.CurrentLocationConsumed -> {
                mutableStateFlow.update {
                    null
                }
            }

            MapEvent.PauseCurrentLocationUpdate -> {
                viewModelScope.launch {
                    geolocationUpdatesRepository.stop()
                    timer.stop()
                }
            }

            MapEvent.ResumeCurrentLocationUpdate -> {
                viewModelScope.launch {
                    geolocationUpdatesRepository.start()
                    if (trackCapturerController.status.first() is TrackCaptureStatus.Error) {
                        trackCapturerController.bind()
                    }
                    timer.start()
                }
            }
        }
    }
}

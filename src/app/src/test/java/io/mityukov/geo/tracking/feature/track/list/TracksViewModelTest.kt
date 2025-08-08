package io.mityukov.geo.tracking.feature.track.list

import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.core.model.track.Track
import io.mityukov.geo.tracking.core.model.track.TrackAction
import io.mityukov.geo.tracking.core.model.track.TrackActionType
import io.mityukov.geo.tracking.core.model.track.TrackPoint
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class TracksViewModelTest {
    val track = Track(
        id = "someTrackId",
        start = "2025-08-08T06:07:11.393356Z",
        end = "2025-08-08T06:07:31.393356Z",
        distance = 123,
        altitudeUp = 3,
        altitudeDown = 2,
        points = listOf(
            TrackPoint(
                id = "someTrackPointId1",
                geolocation = Geolocation(
                    latitude = 53.696453,
                    longitude = 87.439633,
                    altitude = 391.0,
                    time = System.currentTimeMillis()
                )
            ),
            TrackPoint(
                id = "someTrackPointId2",
                geolocation = Geolocation(
                    latitude = 53.696453,
                    longitude = 87.439633,
                    altitude = 391.0,
                    time = System.currentTimeMillis()
                )
            )
        ),
        actions = listOf(
            TrackAction(
                id = "someTrackActionId1",
                trackId = "someId",
                type = TrackActionType.Start,
                timestamp = "2025-08-08T06:07:11.393356Z"
            ),
            TrackAction(
                id = "someTrackActionId1",
                trackId = "someId",
                type = TrackActionType.Stop,
                timestamp = "2025-08-08T06:07:31.393356Z"
            )
        )
    )

    @Test
    fun `toCompletedTrack mapping`() {
        val completedTrack = track.toCompletedTrack()
        assert(track.id == completedTrack.id)
        assert(track.start == completedTrack.start)
        assert(track.end == completedTrack.end)
        assert(track.altitudeDown == completedTrack.altitudeDown)
        assert(track.altitudeUp == completedTrack.altitudeUp)
        assert(track.distance == completedTrack.distance)
        assert(track.points == completedTrack.points)
        assert(completedTrack.duration == 20.seconds)
    }

    @Test
    fun `duration with pause resume actions`() {
        val newTrack = buildTrack(
            track, listOf(
                buildTrackAction(
                    type = TrackActionType.Start,
                    timestamp = "2025-08-08T06:07:11.393356Z"
                ),
                buildTrackAction(
                    type = TrackActionType.Pause,
                    timestamp = "2025-08-08T06:07:15.393356Z"
                ),
                buildTrackAction(
                    type = TrackActionType.Resume,
                    timestamp = "2025-08-08T06:07:18.393356Z"
                ),
                buildTrackAction(
                    type = TrackActionType.Pause,
                    timestamp = "2025-08-08T06:07:21.393356Z"
                ),
                buildTrackAction(
                    type = TrackActionType.Resume,
                    timestamp = "2025-08-08T06:07:28.393356Z"
                ),
                buildTrackAction(
                    type = TrackActionType.Stop,
                    timestamp = "2025-08-08T06:07:31.393356Z"
                ),
            )
        )
        val completedTrack = newTrack.toCompletedTrack()
        assert(completedTrack.duration == 10.seconds)
    }

    private fun buildTrack(track: Track, actions: List<TrackAction>): Track {
        return track.copy(actions = actions)
    }

    private fun buildTrackAction(type: TrackActionType, timestamp: String): TrackAction {
        return TrackAction(
            id = "someTrackActionId",
            trackId = "someId",
            type = type,
            timestamp = timestamp,
        )
    }
}

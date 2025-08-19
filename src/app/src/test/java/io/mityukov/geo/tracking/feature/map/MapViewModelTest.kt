package io.mityukov.geo.tracking.feature.map

import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.core.model.track.Track
import io.mityukov.geo.tracking.core.model.track.TrackAction
import io.mityukov.geo.tracking.core.model.track.TrackActionType
import io.mityukov.geo.tracking.core.model.track.TrackPoint
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class MapViewModelTest {
    val track = Track(
        id = "someTrackId",
        name = "2025-08-08T06:07:11.393356Z",
        start = "2025-08-08T06:07:11.393356Z",
        end = "",
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
                    speed = 1f,
                    time = System.currentTimeMillis()
                )
            ),
            TrackPoint(
                id = "someTrackPointId2",
                geolocation = Geolocation(
                    latitude = 53.696453,
                    longitude = 87.439633,
                    altitude = 391.0,
                    speed = 1f,
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
            )
        )
    )

    @Test
    fun `toTrackInProgress mapping`() {
        val trackInProgress = track.toTrackInProgress("2025-08-08T06:07:13.393356Z")
        assert(track.id == trackInProgress.id)
        assert(track.start == trackInProgress.start)
        assert(track.end == trackInProgress.end)
        assert(track.altitudeDown == trackInProgress.altitudeDown)
        assert(track.altitudeUp == trackInProgress.altitudeUp)
        assert(track.distance == trackInProgress.distance)
        assert(track.points == trackInProgress.points)
        assert(trackInProgress.duration == 2.seconds)
    }

    @Test
    fun `duration with latest pause action`() {
        val newTrack = buildTrack(
            track, listOf(
                TrackAction(
                    id = "someTrackActionId1",
                    trackId = "someId",
                    type = TrackActionType.Start,
                    timestamp = "2025-08-08T06:07:11.393356Z"
                ),
                TrackAction(
                    id = "someTrackActionId1",
                    trackId = "someId",
                    type = TrackActionType.Pause,
                    timestamp = "2025-08-08T06:07:15.393356Z"
                ),
            )
        )
        val trackInProgress = newTrack.toTrackInProgress("2025-08-08T06:07:18.393356Z")
        assert(trackInProgress.duration == 4.seconds)
    }

    @Test
    fun `duration with pause resume actions`() {
        val newTrack = buildTrack(
            track, listOf(
                TrackAction(
                    id = "someTrackActionId1",
                    trackId = "someId",
                    type = TrackActionType.Start,
                    timestamp = "2025-08-08T06:07:11.393356Z"
                ),
                TrackAction(
                    id = "someTrackActionId2",
                    trackId = "someId",
                    type = TrackActionType.Pause,
                    timestamp = "2025-08-08T06:07:15.393356Z"
                ),
                TrackAction(
                    id = "someTrackActionId3",
                    trackId = "someId",
                    type = TrackActionType.Resume,
                    timestamp = "2025-08-08T06:07:18.393356Z"
                ),
                TrackAction(
                    id = "someTrackActionId4",
                    trackId = "someId",
                    type = TrackActionType.Pause,
                    timestamp = "2025-08-08T06:07:21.393356Z"
                ),
                TrackAction(
                    id = "someTrackActionId5",
                    trackId = "someId",
                    type = TrackActionType.Resume,
                    timestamp = "2025-08-08T06:07:28.393356Z"
                ),
            )
        )
        val trackInProgress = newTrack.toTrackInProgress("2025-08-08T06:07:31.393356Z")
        assert(trackInProgress.duration == 10.seconds)
    }

    private fun buildTrack(track: Track, actions: List<TrackAction>): Track {
        return track.copy(actions = actions)
    }
}

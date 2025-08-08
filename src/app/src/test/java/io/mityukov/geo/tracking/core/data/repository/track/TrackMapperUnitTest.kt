package io.mityukov.geo.tracking.core.data.repository.track

import io.mityukov.geo.tracking.core.database.model.TrackActionEntity
import io.mityukov.geo.tracking.core.database.model.TrackEntity
import io.mityukov.geo.tracking.core.database.model.TrackPointEntity
import io.mityukov.geo.tracking.core.database.model.TrackWithPoints
import io.mityukov.geo.tracking.core.model.track.TrackActionType
import org.junit.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TrackMapperUnitTest {
    private val trackId = "trackId"
    private val trackName = "trackName"

    @OptIn(ExperimentalUuidApi::class)
    private val trackWithPoints = TrackWithPoints(
        track = TrackEntity(
            id = trackId,
            name = trackName,
            start = "",
            end = ""
        ),
        points = listOf(
            TrackPointEntity(
                id = "pointId1",
                trackId = trackId,
                latitude = 53.696453,
                longitude = 87.439633,
                altitude = 391.0,
                time = 1000L,
            ),
            TrackPointEntity(
                id = "pointId2",
                trackId = trackId,
                latitude = 53.695841,
                longitude = 87.450213,
                altitude = 311.0,
                time = 1010L,
            ),
            TrackPointEntity(
                id = "pointId3",
                trackId = trackId,
                latitude = 53.694120,
                longitude = 87.458698,
                altitude = 354.0,
                time = 1010L,
            ),
            TrackPointEntity(
                id = "pointId4",
                trackId = trackId,
                latitude = 53.697462,
                longitude = 87.464610,
                altitude = 331.0,
                time = 1010L,
            ),
            TrackPointEntity(
                id = "pointId5",
                trackId = trackId,
                latitude = 53.691241,
                longitude = 87.482451,
                altitude = 351.0,
                time = 1010L,
            ),
        ),
        actions = listOf(
            TrackActionEntity(
                id = Uuid.random().toString(),
                trackId = "someId",
                action = TrackActionType.Start.toString(),
                timestamp = "2025-08-08T06:07:11.393356Z"
            )
        )
    )

    @Test
    fun `track entity to domain mapping`() {
        val track = TrackMapper().trackWithPointsEntityToDomain(trackWithPoints)
        assert(track.id == trackWithPoints.track.id)
        assert(track.distance == 3197)
        assert(track.altitudeUp == 63)
        assert(track.altitudeDown == 103)
        assert(track.start == trackWithPoints.track.start)
        assert(track.end == trackWithPoints.track.end)
        assert(track.points.size == trackWithPoints.points.size)
        assert(track.actions.size == trackWithPoints.actions.size)
    }

    @Test
    fun `track point entity to domain mapping`() {
        val entity = TrackPointEntity(
            id = "pointId1",
            trackId = trackId,
            latitude = 53.696453,
            longitude = 87.439633,
            altitude = 391.0,
            time = 1000L,
        )
        val trackPoint = TrackMapper().trackPointEntityToDomain(entity)
        assert(trackPoint.id == entity.id)
        assert(trackPoint.geolocation.latitude == entity.latitude)
        assert(trackPoint.geolocation.longitude == entity.longitude)
        assert(trackPoint.geolocation.altitude == entity.altitude)
        assert(trackPoint.geolocation.time == entity.time)
    }

    @Test
    fun `track action entity to domain mapping`() {
        val entity = TrackActionEntity(
            id = "someActionId",
            trackId = "someTrackId",
            action = "Start",
            timestamp = "2025-08-08T06:07:11.393356Z",
        )
        val trackAction = TrackMapper().trackActionEntityToDomain(entity)
        assert(trackAction.id == entity.id)
        assert(trackAction.trackId == entity.trackId)
        assert(trackAction.type == TrackActionType.Start)
        assert(trackAction.timestamp == entity.timestamp)
    }
}

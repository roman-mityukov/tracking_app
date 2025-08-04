package io.mityukov.geo.tracking.core.data.repository.track

import io.mityukov.geo.tracking.core.database.model.TrackEntity
import io.mityukov.geo.tracking.core.database.model.TrackPointEntity
import io.mityukov.geo.tracking.core.database.model.TrackWithPoints
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

class TrackMapperUnitTest {
    private val trackId = "trackId"
    private val trackName = "trackName"

    private val trackWithPoints = TrackWithPoints(
        track = TrackEntity(
            id = trackId,
            name = trackName,
            duration = 1000L,
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

        )
    )

    @Test
    fun `track entity to domain mapping`() {
        val track = TrackMapper().trackWithPointsEntityToDomain(trackWithPoints)
        assert(track.id == trackWithPoints.track.id)
        assert(track.distance == 3197)
        assert(track.altitudeUp == 63)
        assert(track.altitudeDown == 103)
        assert(track.duration == trackWithPoints.track.duration.milliseconds)
        assert(track.points.size == trackWithPoints.points.size)
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
}
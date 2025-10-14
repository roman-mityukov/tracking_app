package io.mityukov.geo.tracking.core.data.repository.track

import io.mityukov.geo.tracking.core.database.model.TrackEntity
import io.mityukov.geo.tracking.core.model.track.Track
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.time.Duration.Companion.seconds

@RunWith(RobolectricTestRunner::class)
class TrackMapperTest {
    private val trackMapper = TrackMapper()
    private val trackEntity = TrackEntity(
        id = "trackEntityId",
        description = "description",
        name = "trackEntityName",
        start = 123,
        end = 456,
        distance = 789.3f,
        altitudeUp = 3.3f,
        altitudeDown = 4.1f,
        duration = 123L,
        sumSpeed = 24.5f,
        minSpeed = 12.1f,
        maxSpeed = 13.2f,
        geolocationCount = 3,
        filePath = "trackEntityFilePath",
    )
    private val trackDomain = Track(
        id = "trackDomainId",
        description = "trackDomainDescription",
        name = "trackDomainName",
        start = 123,
        end = 456,
        distance = 789.3f,
        altitudeUp = 3.3f,
        altitudeDown = 4.1f,
        duration = 123L.seconds,
        sumSpeed = 24.5f,
        minSpeed = 12.1f,
        maxSpeed = 13.2f,
        geolocationCount = 3,
        filePath = "trackDomainFilePath",
    )

    @Test
    fun `mapping track entity to domain`() {
        val domain = trackMapper.trackEntityToDomain(trackEntity)
        assert(domain.id == trackEntity.id)
        assert(domain.description == trackEntity.description)
        assert(domain.name == trackEntity.name)
        assert(domain.start == trackEntity.start)
        assert(domain.end == trackEntity.end)
        assert(domain.distance == trackEntity.distance)
        assert(domain.altitudeUp == trackEntity.altitudeUp)
        assert(domain.altitudeDown == trackEntity.altitudeDown)
        assert(domain.duration.inWholeSeconds == trackEntity.duration)
        assert(domain.sumSpeed == trackEntity.sumSpeed)
        assert(domain.minSpeed == trackEntity.minSpeed)
        assert(domain.maxSpeed == trackEntity.maxSpeed)
        assert(domain.geolocationCount == trackEntity.geolocationCount)
        assert(domain.filePath == trackEntity.filePath)
    }

    @Test
    fun `mapping domain to track entity`() {
        val entity = trackMapper.trackDomainToEntity(trackDomain)
        assert(entity.id == trackDomain.id)
        assert(entity.description == trackDomain.description)
        assert(entity.name == trackDomain.name)
        assert(entity.start == trackDomain.start)
        assert(entity.end == trackDomain.end)
        assert(entity.distance == trackDomain.distance)
        assert(entity.altitudeUp == trackDomain.altitudeUp)
        assert(entity.altitudeDown == trackDomain.altitudeDown)
        assert(entity.duration == trackDomain.duration.inWholeSeconds)
        assert(entity.sumSpeed == trackDomain.sumSpeed)
        assert(entity.minSpeed == trackDomain.minSpeed)
        assert(entity.maxSpeed == trackDomain.maxSpeed)
        assert(entity.geolocationCount == trackDomain.geolocationCount)
        assert(entity.filePath == trackDomain.filePath)
    }
}

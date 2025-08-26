package io.mityukov.geo.tracking.core.data.repository.track.capture

import android.location.Location
import android.os.Build
import com.google.protobuf.ByteString
import io.mityukov.geo.tracking.core.data.datastore.proto.ProtoLocalTrackCaptureStatus
import io.mityukov.geo.tracking.core.data.repository.track.TrackMapper
import io.mityukov.geo.tracking.core.database.model.TrackEntity
import io.mityukov.geo.tracking.utils.geolocation.locationFromByteArray
import io.mityukov.geo.tracking.utils.geolocation.toByteArray
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Duration.Companion.seconds

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.VANILLA_ICE_CREAM])
class TrackMapperTest {
    private val trackMapper = TrackMapper()
    private val trackEntity = TrackEntity(
        id = "trackEntityId",
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
    private val location: Location = Location("GPS").apply {
        latitude = 0.0
        longitude = 0.0
        time = 123
    }
    private val trackCaptureStatusRun = TrackCaptureStatus.Run(
        trackInProgress = TrackInProgress(
            start = 123,
            distance = 789.3f,
            altitudeUp = 3.3f,
            altitudeDown = 4.1f,
            duration = 123L.seconds,
            sumSpeed = 24.5f,
            minSpeed = 12.1f,
            maxSpeed = 13.2f,
            geolocationCount = 3,
            paused = false,
            lastLocation = location,
        )
    )
    private val trackCaptureStatusIdle = TrackCaptureStatus.Idle
    private val trackCaptureStatusError = TrackCaptureStatus.Error
    private val protoTrackCaptureStatusRun = ProtoLocalTrackCaptureStatus
        .newBuilder()
        .setTrackCaptureEnabled(true)
        .setStart(123)
        .setDurationInSeconds(321)
        .setDistance(789.3f)
        .setAltitudeUp(3.3f)
        .setAltitudeDown(4.1f)
        .setSumSpeed(24.5f)
        .setMinSpeed(12.1f)
        .setMaxSpeed(13.2f)
        .setLastLocationBytes(ByteString.copyFrom(location.toByteArray()))
        .setGeolocationCount(11)
        .setPaused(false)
        .build()
    private val protoTrackCaptureStatusIdle = ProtoLocalTrackCaptureStatus
        .newBuilder()
        .setTrackCaptureEnabled(false)
        .setStart(123)
        .setDurationInSeconds(321)
        .setDistance(789.3f)
        .setAltitudeUp(3.3f)
        .setAltitudeDown(4.1f)
        .setSumSpeed(24.5f)
        .setMinSpeed(12.1f)
        .setMaxSpeed(13.2f)
        .setLastLocationBytes(ByteString.empty())
        .setGeolocationCount(11)
        .setPaused(false)
        .build()

    @Test
    fun `mapping track entity to domain`() {
        val domain = trackMapper.trackEntityToDomain(trackEntity)
        assert(domain.id == trackEntity.id)
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
    fun `mapping trackCaptureStatus Run to proto`() {
        val proto = trackMapper.trackCaptureStatusDomainToProto(trackCaptureStatusRun)
        assert(proto.trackCaptureEnabled)
        assert(proto.start == trackCaptureStatusRun.trackInProgress.start)
        assert(proto.durationInSeconds == trackCaptureStatusRun.trackInProgress.duration.inWholeSeconds)
        assert(proto.distance == trackCaptureStatusRun.trackInProgress.distance)
        assert(proto.altitudeUp == trackCaptureStatusRun.trackInProgress.altitudeUp)
        assert(proto.altitudeDown == trackCaptureStatusRun.trackInProgress.altitudeDown)
        assert(proto.sumSpeed == trackCaptureStatusRun.trackInProgress.sumSpeed)
        assert(proto.minSpeed == trackCaptureStatusRun.trackInProgress.minSpeed)
        assert(proto.maxSpeed == trackCaptureStatusRun.trackInProgress.maxSpeed)
        assert(proto.lastLocationBytes == ByteString.copyFrom(location.toByteArray()))
        assert(proto.geolocationCount == trackCaptureStatusRun.trackInProgress.geolocationCount)
        assert(proto.paused == trackCaptureStatusRun.trackInProgress.paused)
    }

    @Test
    fun `mapping trackCaptureStatus Idle to proto`() {
        val proto = trackMapper.trackCaptureStatusDomainToProto(trackCaptureStatusIdle)
        assert(proto.trackCaptureEnabled.not())
        assert(proto.start == 0L)
        assert(proto.durationInSeconds == 0L)
        assert(proto.distance == 0f)
        assert(proto.altitudeUp == 0f)
        assert(proto.altitudeDown == 0f)
        assert(proto.sumSpeed == 0f)
        assert(proto.minSpeed == 0f)
        assert(proto.maxSpeed == 0f)
        assert(proto.lastLocationBytes == ByteString.empty())
        assert(proto.geolocationCount == 0)
        assert(proto.paused.not())
    }

    @Test
    fun `mapping trackCaptureStatus Error to proto`() {
        val proto = trackMapper.trackCaptureStatusDomainToProto(trackCaptureStatusError)
        assert(proto.trackCaptureEnabled.not())
        assert(proto.start == 0L)
        assert(proto.durationInSeconds == 0L)
        assert(proto.distance == 0f)
        assert(proto.altitudeUp == 0f)
        assert(proto.altitudeDown == 0f)
        assert(proto.sumSpeed == 0f)
        assert(proto.minSpeed == 0f)
        assert(proto.maxSpeed == 0f)
        assert(proto.lastLocationBytes == ByteString.empty())
        assert(proto.geolocationCount == 0)
        assert(proto.paused.not())
    }

    @Test
    fun `mapping proto trackCaptureStatus Run to domain`() {
        val domain = trackMapper.trackCaptureStatusProtoToDomain(protoTrackCaptureStatusRun)
        assert(domain is TrackCaptureStatus.Run)
        val domainRun = domain as TrackCaptureStatus.Run
        assert(domainRun.trackInProgress.start == protoTrackCaptureStatusRun.start)
        assert(domainRun.trackInProgress.duration.inWholeSeconds == protoTrackCaptureStatusRun.durationInSeconds)
        assert(domainRun.trackInProgress.distance == protoTrackCaptureStatusRun.distance)
        assert(domainRun.trackInProgress.altitudeUp == protoTrackCaptureStatusRun.altitudeUp)
        assert(domainRun.trackInProgress.altitudeDown == protoTrackCaptureStatusRun.altitudeDown)
        assert(domainRun.trackInProgress.sumSpeed == protoTrackCaptureStatusRun.sumSpeed)
        assert(domainRun.trackInProgress.minSpeed == protoTrackCaptureStatusRun.minSpeed)
        assert(domainRun.trackInProgress.maxSpeed == protoTrackCaptureStatusRun.maxSpeed)
        assert(
            domainRun.trackInProgress.lastLocation == locationFromByteArray(
                protoTrackCaptureStatusRun.lastLocationBytes.toByteArray()
            )
        )
        assert(domainRun.trackInProgress.geolocationCount == protoTrackCaptureStatusRun.geolocationCount)
        assert(domainRun.trackInProgress.paused == protoTrackCaptureStatusRun.paused)
    }

    @Test
    fun `mapping proto trackCaptureStatus Idle to domain`() {
        val domain = trackMapper.trackCaptureStatusProtoToDomain(protoTrackCaptureStatusIdle)
        assert(domain is TrackCaptureStatus.Idle)
    }
}

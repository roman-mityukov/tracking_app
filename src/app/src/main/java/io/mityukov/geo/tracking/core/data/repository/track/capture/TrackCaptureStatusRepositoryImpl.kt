package io.mityukov.geo.tracking.core.data.repository.track.capture

import androidx.datastore.core.DataStore
import com.google.protobuf.ByteString
import io.mityukov.geo.tracking.core.data.datastore.proto.ProtoLocalTrackCaptureStatus
import io.mityukov.geo.tracking.di.TrackCaptureStatusDataStore
import io.mityukov.geo.tracking.utils.geolocation.locationFromByteArray
import io.mityukov.geo.tracking.utils.geolocation.toByteArray
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds


class TrackCaptureStatusRepositoryImpl @Inject constructor(
    @param:TrackCaptureStatusDataStore private val dataStore: DataStore<ProtoLocalTrackCaptureStatus>,
) : TrackCaptureStatusRepository {
    override val status: Flow<TrackCaptureStatus> = dataStore.data.map { proto ->
        proto.toTrackCaptureStatus()
    }

    override suspend fun update(status: TrackCaptureStatus) {
        val newTrackCaptureStatus = when (status) {
            TrackCaptureStatus.Idle, TrackCaptureStatus.Error -> {
                ProtoLocalTrackCaptureStatus
                    .newBuilder()
                    .setTrackCaptureEnabled(false)
                    .setStart(0)
                    .setDurationInSeconds(0)
                    .setDistance(0f)
                    .setAltitudeUp(0f)
                    .setAltitudeDown(0f)
                    .setAverageSpeed(0f)
                    .setMinSpeed(0f)
                    .setMaxSpeed(0f)
                    .setLastLocationBytes(ByteString.empty())
                    .setGeolocationCount(0)
                    .setPaused(false)
                    .build()
            }

            is TrackCaptureStatus.Run -> {
                val trackInProgress = status.trackInProgress
                val location = status.trackInProgress.lastLocation
                val byteString = if (location != null) {
                    ByteString.copyFrom(location.toByteArray())
                } else {
                    ByteString.empty()
                }

                ProtoLocalTrackCaptureStatus
                    .newBuilder()
                    .setTrackCaptureEnabled(true)
                    .setStart(trackInProgress.start)
                    .setDurationInSeconds(trackInProgress.duration.inWholeSeconds)
                    .setDistance(trackInProgress.distance)
                    .setAltitudeUp(trackInProgress.altitudeUp)
                    .setAltitudeDown(trackInProgress.altitudeDown)
                    .setAverageSpeed(trackInProgress.averageSpeed)
                    .setMinSpeed(trackInProgress.minSpeed)
                    .setMaxSpeed(trackInProgress.maxSpeed)
                    .setLastLocationBytes(byteString)
                    .setGeolocationCount(trackInProgress.geolocationCount)
                    .setPaused(trackInProgress.paused)
                    .build()
            }
        }
        dataStore.updateData {
            newTrackCaptureStatus
        }
    }
}

private fun ProtoLocalTrackCaptureStatus.toTrackCaptureStatus(): TrackCaptureStatus {
    return if (trackCaptureEnabled) {
        val location = if (lastLocationBytes.isEmpty) {
            null
        } else {
            val bytes = lastLocationBytes.toByteArray()
            locationFromByteArray(bytes)
        }

        TrackCaptureStatus.Run(
            trackInProgress = TrackInProgress(
                start = start,
                duration = durationInSeconds.seconds,
                distance = distance,
                altitudeUp = altitudeUp,
                altitudeDown = altitudeDown,
                averageSpeed = averageSpeed,
                maxSpeed = maxSpeed,
                minSpeed = minSpeed,
                lastLocation = location,
                geolocationCount = geolocationCount,
                paused = paused
            )
        )
    } else {
        TrackCaptureStatus.Idle
    }
}

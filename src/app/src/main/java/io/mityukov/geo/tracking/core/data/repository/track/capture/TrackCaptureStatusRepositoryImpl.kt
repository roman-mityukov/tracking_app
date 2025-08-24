package io.mityukov.geo.tracking.core.data.repository.track.capture

import androidx.datastore.core.DataStore
import io.mityukov.geo.tracking.core.data.datastore.proto.ProtoGeolocation
import io.mityukov.geo.tracking.core.data.datastore.proto.ProtoLocalTrackCaptureStatus
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.di.TrackCaptureStatusDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class TrackCaptureStatusRepositoryImpl @Inject constructor(
    @param:TrackCaptureStatusDataStore private val dataStore: DataStore<ProtoLocalTrackCaptureStatus>,
) : TrackCaptureStatusRepository {
    override val status: Flow<LocalTrackCaptureStatus> = dataStore.data.map { proto ->
        proto.toLocalTrackCaptureStatus()
    }

    override suspend fun update(localTrackCaptureStatus: LocalTrackCaptureStatus) {
        val newTrackCaptureStatus = when (localTrackCaptureStatus) {
            LocalTrackCaptureStatus.Disabled -> {
                ProtoLocalTrackCaptureStatus
                    .newBuilder()
                    .setTrackCaptureEnabled(false)
                    .setStart(0)
                    .setDurationInSeconds(0)
                    .setDistance(0)
                    .setAltitudeUp(0)
                    .setAltitudeDown(0)
                    .setAverageSpeed(0f)
                    .setMinSpeed(0f)
                    .setMaxSpeed(0f)
                    .setLastGeolocation(ProtoGeolocation.getDefaultInstance())
                    .setGeolocationCount(0)
                    .setPaused(false)
                    .build()
            }

            is LocalTrackCaptureStatus.Enabled -> {
                val trackInProgress = localTrackCaptureStatus.trackInProgress
                val geolocation = localTrackCaptureStatus.trackInProgress.lastLocation

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
                    .setLastGeolocation(
                        if (geolocation != null) {
                            ProtoGeolocation.newBuilder()
                                .setLatitude(geolocation.latitude)
                                .setLongitude(geolocation.longitude)
                                .setAltitude(geolocation.altitude)
                                .setSpeed(geolocation.speed)
                                .setTimestamp(geolocation.time)
                                .build()
                        } else {
                            ProtoGeolocation.getDefaultInstance()
                        }
                    )
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

private fun ProtoLocalTrackCaptureStatus.toLocalTrackCaptureStatus(): LocalTrackCaptureStatus {
    return if (trackCaptureEnabled) {
        LocalTrackCaptureStatus.Enabled(
            trackInProgress = TrackInProgress(
                start = start,
                duration = durationInSeconds.seconds,
                distance = distance,
                altitudeUp = altitudeUp,
                altitudeDown = altitudeDown,
                averageSpeed = averageSpeed,
                maxSpeed = maxSpeed,
                minSpeed = minSpeed,
                lastLocation = if (lastGeolocation.timestamp > 0) {
                    Geolocation(
                        lastGeolocation.latitude,
                        lastGeolocation.longitude,
                        lastGeolocation.altitude,
                        lastGeolocation.speed,
                        lastGeolocation.timestamp,
                    )
                } else {
                    null
                },
                geolocationCount = geolocationCount,
                paused = paused
            )
        )
    } else {
        LocalTrackCaptureStatus.Disabled
    }
}

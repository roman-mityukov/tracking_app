package io.mityukov.geo.tracking.core.data.repository.track.capture

import android.location.Location
import android.os.Parcel
import androidx.annotation.VisibleForTesting
import com.google.protobuf.ByteString
import io.mityukov.geo.tracking.core.datastore.proto.ProtoLocalTrackCaptureStatus
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

internal class TrackCaptureStatusMapper @Inject constructor() {
    fun trackCaptureStatusProtoToDomain(proto: ProtoLocalTrackCaptureStatus): TrackCaptureStatus {
        return if (proto.trackCaptureEnabled) {
            val location = if (proto.lastLocationBytes.isEmpty) {
                null
            } else {
                val bytes = proto.lastLocationBytes.toByteArray()
                locationFromByteArray(bytes)
            }

            TrackCaptureStatus.Run(
                trackInProgress = TrackInProgress(
                    start = proto.start,
                    duration = proto.durationInSeconds.seconds,
                    distance = proto.distance,
                    altitudeUp = proto.altitudeUp,
                    altitudeDown = proto.altitudeDown,
                    sumSpeed = proto.sumSpeed,
                    maxSpeed = proto.maxSpeed,
                    minSpeed = proto.minSpeed,
                    lastLocation = location,
                    geolocationCount = proto.geolocationCount,
                    paused = proto.paused
                )
            )
        } else {
            TrackCaptureStatus.Idle
        }
    }

    fun trackCaptureStatusDomainToProto(status: TrackCaptureStatus): ProtoLocalTrackCaptureStatus {
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
                    .setSumSpeed(0f)
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
                    .setSumSpeed(trackInProgress.sumSpeed)
                    .setMinSpeed(trackInProgress.minSpeed)
                    .setMaxSpeed(trackInProgress.maxSpeed)
                    .setLastLocationBytes(byteString)
                    .setGeolocationCount(trackInProgress.geolocationCount)
                    .setPaused(trackInProgress.paused)
                    .build()
            }
        }
        return newTrackCaptureStatus
    }
}

internal fun locationFromByteArray(bytes: ByteArray): Location {
    val parcel = Parcel.obtain()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    val location: Location = Location.CREATOR.createFromParcel(parcel)
    parcel.recycle()

    return location
}

@VisibleForTesting
internal fun Location.toByteArray(): ByteArray {
    val parcel = Parcel.obtain()
    writeToParcel(parcel, 0)
    val bytes = parcel.marshall()
    parcel.recycle()
    return bytes
}

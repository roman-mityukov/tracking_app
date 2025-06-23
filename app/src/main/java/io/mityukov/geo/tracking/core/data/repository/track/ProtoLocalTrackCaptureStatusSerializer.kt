package io.mityukov.geo.tracking.core.data.repository.track

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import io.mityukov.geo.tracking.core.data.repository.settings.app.proto.ProtoLocalTrackCaptureStatus
import java.io.InputStream
import java.io.OutputStream

object ProtoLocalTrackCaptureStatusSerializer : Serializer<ProtoLocalTrackCaptureStatus> {
    override val defaultValue: ProtoLocalTrackCaptureStatus = ProtoLocalTrackCaptureStatus.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): ProtoLocalTrackCaptureStatus {
        try {
            return ProtoLocalTrackCaptureStatus.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: ProtoLocalTrackCaptureStatus, output: OutputStream) = t.writeTo(output)
}
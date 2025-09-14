package io.mityukov.geo.tracking.core.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import io.mityukov.geo.tracking.core.datastore.proto.ProtoLocalAppSettings
import java.io.InputStream
import java.io.OutputStream

internal object ProtoLocalAppSettingsSerializer : Serializer<ProtoLocalAppSettings> {
    override val defaultValue: ProtoLocalAppSettings = ProtoLocalAppSettings.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): ProtoLocalAppSettings {
        try {
            return ProtoLocalAppSettings.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: ProtoLocalAppSettings, output: OutputStream) = t.writeTo(output)
}

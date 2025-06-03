package io.rm.test.geo.core.data.repository.settings.app

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import io.rm.test.geo.core.data.repository.settings.app.proto.ProtoLocalAppSettings
import java.io.InputStream
import java.io.OutputStream

object ProtoLocalAppSettingsSerializer : Serializer<ProtoLocalAppSettings> {
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
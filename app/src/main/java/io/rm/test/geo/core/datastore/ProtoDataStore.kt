package io.rm.workorder.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import io.rm.test.geo.core.data.repository.settings.app.ProtoLocalAppSettingsSerializer
import io.rm.test.geo.core.data.repository.settings.app.proto.ProtoLocalAppSettings

val Context.appSettingsDataStore: DataStore<ProtoLocalAppSettings> by dataStore(
    fileName = "local_app_settings.proto",
    serializer = ProtoLocalAppSettingsSerializer
)
package io.mityukov.geo.tracking.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import io.mityukov.geo.tracking.core.datastore.proto.ProtoLocalAppSettings
import io.mityukov.geo.tracking.core.datastore.proto.ProtoLocalTrackCaptureStatus

val Context.appSettingsDataStore: DataStore<ProtoLocalAppSettings> by dataStore(
    fileName = "local_app_settings.proto",
    serializer = ProtoLocalAppSettingsSerializer
)

val Context.trackCaptureStatusDataStore: DataStore<ProtoLocalTrackCaptureStatus> by dataStore(
    fileName = "local_track_capture_status.proto",
    serializer = ProtoLocalTrackCaptureStatusSerializer,
)



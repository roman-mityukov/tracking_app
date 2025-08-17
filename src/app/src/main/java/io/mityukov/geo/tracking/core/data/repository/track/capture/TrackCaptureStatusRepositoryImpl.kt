package io.mityukov.geo.tracking.core.data.repository.track.capture

import androidx.datastore.core.DataStore
import io.mityukov.geo.tracking.core.data.datastore.proto.ProtoLocalTrackCaptureStatus
import io.mityukov.geo.tracking.di.TrackCaptureStatusDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TrackCaptureStatusRepositoryImpl @Inject constructor(
    @param:TrackCaptureStatusDataStore private val dataStore: DataStore<ProtoLocalTrackCaptureStatus>,
) : TrackCaptureStatusRepository {
    override val status: Flow<LocalTrackCaptureStatus> = dataStore.data.map { proto ->
        if (proto.trackCaptureEnabled) {
            LocalTrackCaptureStatus.Enabled(trackId = proto.trackId, paused = proto.paused)
        } else {
            LocalTrackCaptureStatus.Disabled
        }
    }

    override suspend fun update(localTrackCaptureStatus: LocalTrackCaptureStatus) {
        val newTrackCaptureStatus = when(localTrackCaptureStatus) {
            LocalTrackCaptureStatus.Disabled -> {
                ProtoLocalTrackCaptureStatus
                    .newBuilder()
                    .setTrackId("")
                    .setTrackCaptureEnabled(false)
                    .setPaused(false)
                    .build()
            }
            is LocalTrackCaptureStatus.Enabled -> {
                ProtoLocalTrackCaptureStatus
                    .newBuilder()
                    .setTrackId(localTrackCaptureStatus.trackId)
                    .setTrackCaptureEnabled(true)
                    .setPaused(localTrackCaptureStatus.paused)
                    .build()
            }
        }
        dataStore.updateData {
            newTrackCaptureStatus
        }
    }
}

package io.mityukov.geo.tracking.core.data.repository.track.capture

import androidx.datastore.core.DataStore
import io.mityukov.geo.tracking.core.datastore.di.TrackCaptureStatusDataStore
import io.mityukov.geo.tracking.core.datastore.proto.ProtoLocalTrackCaptureStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


internal class TrackCaptureStatusRepositoryImpl @Inject constructor(
    private val trackCaptureStatusMapper: TrackCaptureStatusMapper,
    @param:TrackCaptureStatusDataStore private val dataStore: DataStore<ProtoLocalTrackCaptureStatus>,
) : TrackCaptureStatusRepository {
    override val status: Flow<TrackCaptureStatus> = dataStore.data.map { proto ->
        trackCaptureStatusMapper.trackCaptureStatusProtoToDomain(proto)
    }

    override suspend fun update(status: TrackCaptureStatus) {
        val newTrackCaptureStatus = trackCaptureStatusMapper.trackCaptureStatusDomainToProto(status)
        dataStore.updateData {
            newTrackCaptureStatus
        }
    }
}

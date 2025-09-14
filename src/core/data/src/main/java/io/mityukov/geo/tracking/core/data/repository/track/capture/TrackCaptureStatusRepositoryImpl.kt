package io.mityukov.geo.tracking.core.data.repository.track.capture

import androidx.datastore.core.DataStore
import io.mityukov.geo.tracking.core.datastore.di.TrackCaptureStatusDataStore
import io.mityukov.geo.tracking.core.data.repository.track.TrackMapper
import io.mityukov.geo.tracking.core.datastore.proto.ProtoLocalTrackCaptureStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


internal class TrackCaptureStatusRepositoryImpl @Inject constructor(
    private val trackMapper: TrackMapper,
    @param:TrackCaptureStatusDataStore private val dataStore: DataStore<ProtoLocalTrackCaptureStatus>,
) : TrackCaptureStatusRepository {
    override val status: Flow<TrackCaptureStatus> = dataStore.data.map { proto ->
        trackMapper.trackCaptureStatusProtoToDomain(proto)
    }

    override suspend fun update(status: TrackCaptureStatus) {
        val newTrackCaptureStatus = trackMapper.trackCaptureStatusDomainToProto(status)
        dataStore.updateData {
            newTrackCaptureStatus
        }
    }
}

package io.mityukov.geo.tracking.core.data.repository.track.capture

import androidx.datastore.core.DataStore
import io.mityukov.geo.tracking.core.data.datastore.proto.ProtoLocalTrackCaptureStatus
import io.mityukov.geo.tracking.core.data.repository.track.TrackMapper
import io.mityukov.geo.tracking.di.TrackCaptureStatusDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class TrackCaptureStatusRepositoryImpl @Inject constructor(
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

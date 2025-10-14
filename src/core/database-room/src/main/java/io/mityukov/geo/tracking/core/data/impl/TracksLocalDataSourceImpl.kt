package io.mityukov.geo.tracking.core.data.impl

import io.mityukov.geo.tracking.core.data.repository.track.TracksLocalDataSource
import io.mityukov.geo.tracking.core.database.dao.TrackDao
import io.mityukov.geo.tracking.core.model.track.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class TracksLocalDataSourceImpl @Inject constructor(
    private val trackDao: TrackDao,
    private val trackMapper: TrackMapper,
) : TracksLocalDataSource {
    override fun getAllTracks(): Flow<List<Track>> {
        return trackDao.getAllTracks().map {
            it.map {
                trackMapper.trackEntityToDomain(it)
            }
        }
    }

    override fun getTrack(trackId: String): Track {
        return trackMapper.trackEntityToDomain(trackDao.getTrack(trackId))
    }

    override fun getTrackUpdates(trackId: String): Flow<Track?> = trackDao.getTrackUpdates(trackId)
        .map { entity ->
            if (entity != null) {
                trackMapper.trackEntityToDomain(entity)
            } else {
                null
            }
        }

    override fun insertTrack(track: Track) {
        val trackEntity = trackMapper.trackDomainToEntity(track)
        trackDao.insertTrack(trackEntity)
    }

    override fun deleteTrack(trackId: String) {
        trackDao.deleteTrack(trackId)
    }

    override fun updateTrack(track: Track) {
        val trackEntity = trackMapper.trackDomainToEntity(track)
        trackDao.updateTrack(trackEntity)
    }
}

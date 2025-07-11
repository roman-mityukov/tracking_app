package io.mityukov.geo.tracking.core.data.repository.track

import io.mityukov.geo.tracking.core.database.dao.TrackDao
import io.mityukov.geo.tracking.core.model.track.Track
import io.mityukov.geo.tracking.di.DispatcherIO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TracksRepositoryImpl @Inject constructor(
    private val trackDao: TrackDao,
    private val trackMapper: TrackMapper,
    @DispatcherIO private val coroutineDispatcher: CoroutineDispatcher,
) : TracksRepository {
    private val mutableStateFlow = MutableStateFlow<List<Track>>(listOf())


    override fun refreshTracks(): Flow<List<Track>> {
        return trackDao.getAllTracksWithPoints().map {
            val tracks = it.map {
                trackMapper.trackWithPointsEntityToDomain(it)
            }
            mutableStateFlow.update {
                tracks
            }
            tracks
        }
    }

    override fun getTracks(): Flow<List<Track>> {
        return mutableStateFlow
    }

    override fun getTrack(trackId: String): Flow<Track> {
        return trackDao.getTrackWithPoints(trackId).map {
            trackMapper.trackWithPointsEntityToDomain(it)
        }
    }

    override suspend fun deleteTrack(trackId: String) = withContext(coroutineDispatcher) {
        trackDao.deleteTrack(trackId)
        trackDao.deleteTrackPoints(trackId)
    }
}
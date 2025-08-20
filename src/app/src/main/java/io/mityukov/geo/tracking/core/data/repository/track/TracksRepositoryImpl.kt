package io.mityukov.geo.tracking.core.data.repository.track

import io.mityukov.geo.tracking.core.database.dao.TrackDao
import io.mityukov.geo.tracking.core.database.model.TrackActionEntity
import io.mityukov.geo.tracking.core.database.model.TrackEntity
import io.mityukov.geo.tracking.core.database.model.TrackPointEntity
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.core.model.track.Track
import io.mityukov.geo.tracking.core.model.track.TrackActionType
import io.mityukov.geo.tracking.di.DispatcherIO
import io.mityukov.geo.tracking.utils.time.TimeUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TracksRepositoryImpl @Inject constructor(
    private val trackDao: TrackDao,
    private val trackMapper: TrackMapper,
    @param:DispatcherIO private val coroutineDispatcher: CoroutineDispatcher,
) : TracksRepository {
    override val tracks: Flow<List<Track>> = trackDao.getAllTracksWithPoints().map {
        it.map {
            trackMapper.trackWithPointsEntityToDomain(it)
        }
    }

    override fun getTrack(trackId: String): Flow<Track> = trackDao.getTrackWithPoints(trackId).map {
        trackMapper.trackWithPointsEntityToDomain(it)
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun insertTrack(): String {
        val startTime = TimeUtils.getCurrentUtcTime()
        val trackEntity =
            TrackEntity(
                id = Uuid.random().toString(),
                name = startTime,
                start = startTime,
                end = ""
            )
        trackDao.insertTrack(trackEntity)
        return trackEntity.id
    }

    override suspend fun deleteTrack(trackId: String) = withContext(coroutineDispatcher) {
        trackDao.deleteAllTrackData(trackId)
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun insertTrackAction(trackId: String, action: TrackActionType) {
        trackDao.insertTrackAction(
            TrackActionEntity(
                id = Uuid.random().toString(),
                trackId = trackId,
                timestamp = TimeUtils.getCurrentUtcTime(),
                action = action.toString(),
            )
        )

        if (action == TrackActionType.Stop) {
            val currentTrack = trackDao.getTrack(trackId)
            val track = TrackEntity(
                id = currentTrack.id,
                name = currentTrack.name,
                start = currentTrack.start,
                end = TimeUtils.getCurrentUtcTime()
            )
            trackDao.insertTrack(track)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun insertTrackPoint(trackId: String, geolocation: Geolocation) {
        trackDao.insertTrackPoint(
            TrackPointEntity(
                id = Uuid.Companion.random().toString(),
                trackId = trackId,
                latitude = geolocation.latitude,
                longitude = geolocation.longitude,
                altitude = geolocation.altitude,
                speed = geolocation.speed,
                time = geolocation.time,
            )
        )
    }
}

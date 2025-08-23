package io.mityukov.geo.tracking.core.data.repository.track

import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.core.model.track.DetailedTrack
import io.mityukov.geo.tracking.core.model.track.Track
import io.mityukov.geo.tracking.core.model.track.TrackActionType
import kotlinx.coroutines.flow.Flow

interface TracksRepository {
    val tracks: Flow<List<Track>>
    suspend fun getTrack(trackId: String) : Track
    suspend fun getDetailedTrack(trackId: String) : DetailedTrack
    suspend fun deleteTrack(trackId: String)
    suspend fun insertTrackAction(action: TrackActionType)
    suspend fun insertTrackPoint(geolocation: Geolocation)
}

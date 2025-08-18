package io.mityukov.geo.tracking.core.data.repository.track

import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.core.model.track.Track
import io.mityukov.geo.tracking.core.model.track.TrackActionType
import kotlinx.coroutines.flow.Flow

interface TracksRepository {
    val tracks: Flow<List<Track>>
    fun getTrack(trackId: String) : Flow<Track>
    suspend fun insertTrack(): String
    suspend fun deleteTrack(trackId: String)
    suspend fun insertTrackAction(trackId: String, action: TrackActionType)
    suspend fun insertTrackPoint(trackId: String, geolocation: Geolocation)
}

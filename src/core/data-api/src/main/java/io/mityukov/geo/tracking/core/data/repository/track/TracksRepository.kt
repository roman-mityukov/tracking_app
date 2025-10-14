package io.mityukov.geo.tracking.core.data.repository.track

import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackInProgress
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.core.model.track.DetailedTrack
import io.mityukov.geo.tracking.core.model.track.Track
import kotlinx.coroutines.flow.Flow

interface TracksRepository {
    fun readAllTracks(): Flow<List<Track>>
    fun readTrack(trackId: String): Flow<Track>
    suspend fun readDetailedTrack(trackId: String): DetailedTrack
    suspend fun readCapturedTrackGeolocations(): List<Geolocation>
    suspend fun deleteTrack(trackId: String)
    suspend fun createTrack(trackInProgress: TrackInProgress)
    suspend fun createTrackPoint(location: Geolocation)
    suspend fun updateTrack(track: Track)
}

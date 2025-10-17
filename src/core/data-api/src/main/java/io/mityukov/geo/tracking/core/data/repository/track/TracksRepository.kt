package io.mityukov.geo.tracking.core.data.repository.track

import io.mityukov.geo.tracking.core.data.repository.RepositoryResult
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackInProgress
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.core.model.track.DetailedTrack
import io.mityukov.geo.tracking.core.model.track.Track
import kotlinx.coroutines.flow.Flow

interface TracksRepository {
    fun readAllTracks(): Flow<List<Track>>
    fun readTrack(trackId: String): Flow<Track>
    suspend fun readDetailedTrack(trackId: String): RepositoryResult<DetailedTrack>
    suspend fun readCapturedTrackGeolocations(): RepositoryResult<List<Geolocation>>
    suspend fun deleteTrack(trackId: String): RepositoryResult<Unit>
    suspend fun createTrack(trackInProgress: TrackInProgress): RepositoryResult<Unit>
    suspend fun createTrackPoint(geolocation: Geolocation): RepositoryResult<Unit>
    suspend fun updateTrack(track: Track): RepositoryResult<Unit>
}

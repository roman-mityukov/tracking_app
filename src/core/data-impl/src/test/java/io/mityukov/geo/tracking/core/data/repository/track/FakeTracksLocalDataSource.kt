package io.mityukov.geo.tracking.core.data.repository.track

import io.mityukov.geo.tracking.core.model.track.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeTracksLocalDataSource : TracksLocalDataSource {
    private val tracks = mutableListOf<Track>()

    override fun getAllTracks(): Flow<List<Track>> {
        return flowOf(tracks)
    }

    override fun getTrack(trackId: String): Track {
        return tracks.first { it.id == trackId }
    }

    override fun getTrackUpdates(trackId: String): Flow<Track?> {
        TODO("Not yet implemented")
    }

    override fun insertTrack(track: Track) {
        tracks.add(track)
    }

    override fun deleteTrack(trackId: String) {
        tracks.removeIf { it.id == trackId }
    }

    override fun updateTrack(track: Track) {
        tracks[tracks.indexOfFirst { it.id == track.id }] = track
    }
}

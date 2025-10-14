package io.mityukov.geo.tracking.core.data.repository.track

import io.mityukov.geo.tracking.core.model.track.Track
import kotlinx.coroutines.flow.Flow

class FakeTracksLocalDataSource : TracksLocalDataSource {


    override fun getAllTracks(): Flow<List<Track>> {
        TODO("Not yet implemented")
    }

    override fun getTrack(trackId: String): Track {
        TODO("Not yet implemented")
    }

    override fun getTrackUpdates(trackId: String): Flow<Track?> {
        TODO("Not yet implemented")
    }

    override fun insertTrack(track: Track) {
        TODO("Not yet implemented")
    }

    override fun deleteTrack(trackId: String) {
        TODO("Not yet implemented")
    }

    override fun updateTrack(track: Track) {
        TODO("Not yet implemented")
    }
}

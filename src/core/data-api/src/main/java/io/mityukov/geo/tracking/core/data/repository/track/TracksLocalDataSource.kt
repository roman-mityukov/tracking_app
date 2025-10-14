package io.mityukov.geo.tracking.core.data.repository.track

import io.mityukov.geo.tracking.core.model.track.Track
import kotlinx.coroutines.flow.Flow

interface TracksLocalDataSource {
    fun getAllTracks(): Flow<List<Track>>

    fun getTrack(trackId: String): Track

    fun getTrackUpdates(trackId: String): Flow<Track?>

    fun insertTrack(track: Track)

    fun deleteTrack(trackId: String)

    fun updateTrack(track: Track)
}

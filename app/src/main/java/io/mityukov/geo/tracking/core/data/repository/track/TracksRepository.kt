package io.mityukov.geo.tracking.core.data.repository.track

import io.mityukov.geo.tracking.core.model.track.Track
import kotlinx.coroutines.flow.Flow

interface TracksRepository {
    fun getTracks(): Flow<List<Track>>
    fun getTrack(trackId: String) : Flow<Track>
    suspend fun deleteTrack(trackId: String)
}
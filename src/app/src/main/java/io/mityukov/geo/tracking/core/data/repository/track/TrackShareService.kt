package io.mityukov.geo.tracking.core.data.repository.track

import io.mityukov.geo.tracking.core.model.track.Track

interface TrackShareService {
    suspend fun prepareTrackFile(track: Track): String
}

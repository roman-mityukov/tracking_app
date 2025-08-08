package io.mityukov.geo.tracking.feature.map

import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationUpdateResult
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationUpdatesRepository
import io.mityukov.geo.tracking.core.data.repository.track.TrackCaptureController
import io.mityukov.geo.tracking.core.data.repository.track.TrackCaptureStatus
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FakeGeolocationUpdatesRepository: GeolocationUpdatesRepository {
    override val currentLocation: Flow<GeolocationUpdateResult> = flow {
        emit(GeolocationUpdateResult(
            geolocation = Geolocation(0.0, 0.0, 0.0, 0L),
            error = null,
        ))
    }

    override suspend fun start() {
        TODO("Not yet implemented")
    }

    override suspend fun stop() {
        TODO("Not yet implemented")
    }
}

class FakeTrackCaptureController : TrackCaptureController {
    override val status: Flow<TrackCaptureStatus>
        get() = TODO("Not yet implemented")

    override suspend fun start() {
        TODO("Not yet implemented")
    }

    override suspend fun resume() {
        TODO("Not yet implemented")
    }

    override suspend fun pause() {
        TODO("Not yet implemented")
    }

    override suspend fun stop() {
        TODO("Not yet implemented")
    }

    override suspend fun bind() {
        TODO("Not yet implemented")
    }

}

class MapViewModelTest {
    @Test
    fun test() = runTest {

    }
}

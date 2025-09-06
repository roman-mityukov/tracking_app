package io.mityukov.geo.tracking.feature.track.capture

import app.cash.turbine.test
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackCaptureStatus
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackCapturerController
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackInProgress
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.core.model.track.DetailedTrack
import io.mityukov.geo.tracking.core.model.track.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

private val trackInProgressEmpty = TrackInProgress.Companion.empty()

private class TestTrackCaptureController : TrackCapturerController {
    private val mutableStateFlow = MutableStateFlow<TrackCaptureStatus>(TrackCaptureStatus.Idle)
    override val status: Flow<TrackCaptureStatus> = mutableStateFlow.asStateFlow()

    override suspend fun start() {
        mutableStateFlow.update {
            TrackCaptureStatus.Run(trackInProgressEmpty)
        }
    }

    override suspend fun resume() {
        mutableStateFlow.update {
            TrackCaptureStatus.Run(trackInProgressEmpty)
        }
    }

    override suspend fun pause() {
        mutableStateFlow.update {
            TrackCaptureStatus.Run(trackInProgressEmpty.copy(paused = true))
        }
    }

    override suspend fun stop() {
        mutableStateFlow.update {
            TrackCaptureStatus.Idle
        }
    }

    override suspend fun bind() {
        mutableStateFlow.update {
            TrackCaptureStatus.Run(trackInProgressEmpty.copy(paused = true))
        }
    }
}

private class TestTrackRepository : TracksRepository {
    override val tracks: Flow<List<Track>>
        get() = TODO("Not yet implemented")

    override suspend fun getTrack(trackId: String): Track {
        TODO("Not yet implemented")
    }

    override suspend fun getDetailedTrack(trackId: String): DetailedTrack {
        TODO("Not yet implemented")
    }

    override suspend fun getCapturedTrackGeolocations(): List<Geolocation> {
        return listOf()
    }

    override suspend fun deleteTrack(trackId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun insertTrack(trackInProgress: TrackInProgress) {
        TODO("Not yet implemented")
    }

    override suspend fun insertTrackPoint(location: Geolocation) {
        TODO("Not yet implemented")
    }
}

class TrackCaptureViewModelTest {
    private lateinit var viewModel: TrackCaptureViewModel
    private lateinit var trackCapturerController: TrackCapturerController
    private lateinit var tracksRepository: TracksRepository

    @Before
    fun setUp() {
        trackCapturerController = TestTrackCaptureController()
        tracksRepository = TestTrackRepository()
        viewModel = TrackCaptureViewModel(trackCapturerController, tracksRepository)
    }

    @Test
    fun `create viewModel initial state is TrackCaptureStatus Idle`() = runTest {
        assertEquals(TrackCaptureState(TrackCaptureStatus.Idle), viewModel.stateFlow.value)
    }

    @Test
    fun `create viewModel bind is called`() = runTest {
        viewModel.stateFlow.test {
            assertEquals(TrackCaptureState(TrackCaptureStatus.Idle), awaitItem())
            assertEquals(
                TrackCaptureState(
                    TrackCaptureStatus.Run(
                        trackInProgressEmpty.copy(paused = true)
                    )
                ), awaitItem()
            )
        }
    }

    @Test
    fun `viewModel maps controller states properly`() = runTest {
        viewModel.stateFlow.test {
            assertEquals(TrackCaptureState(TrackCaptureStatus.Idle), awaitItem())
            assertEquals(
                TrackCaptureState(
                    TrackCaptureStatus.Run(
                        trackInProgressEmpty.copy(paused = true)
                    )
                ), awaitItem()
            )
            viewModel.add(TrackCaptureEvent.PlayCapture)
            assertEquals(
                TrackCaptureState(
                    TrackCaptureStatus.Run(
                        trackInProgressEmpty
                    )
                ), awaitItem()
            )
            viewModel.add(TrackCaptureEvent.PauseCapture)
            assertEquals(
                TrackCaptureState(
                    TrackCaptureStatus.Run(
                        trackInProgressEmpty.copy(paused = true)
                    )
                ), awaitItem()
            )
            viewModel.add(TrackCaptureEvent.StopCapture)
            assertEquals(
                TrackCaptureState(
                    TrackCaptureStatus.Idle
                ), awaitItem()
            )
            viewModel.add(TrackCaptureEvent.StartCapture)
            assertEquals(
                TrackCaptureState(
                    TrackCaptureStatus.Run(trackInProgressEmpty)
                ), awaitItem()
            )
        }
    }
}

package io.mityukov.geo.tracking.feature.track.details

import app.cash.turbine.test
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.core.data.validation.TrackProperties
import io.mityukov.geo.tracking.core.data.validation.TrackValidationResult
import io.mityukov.geo.tracking.core.data.validation.TrackValidatorImpl
import io.mityukov.geo.tracking.feature.track.editing.TrackEditingEvent
import io.mityukov.geo.tracking.feature.track.editing.TrackEditingState
import io.mityukov.geo.tracking.feature.track.editing.TrackEditingViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class TrackEditingViewModelTest {
    private lateinit var tracksRepository: TracksRepository
    private lateinit var viewModel: TrackEditingViewModel

    @Before
    fun setUp() {
        tracksRepository = mock<TracksRepository>()
        viewModel = TrackEditingViewModel(TrackValidatorImpl(), tracksRepository)
    }

    @Test
    fun someTest() = runTest {

        viewModel.stateFlow.test {
            assert(TrackEditingState.Initial == awaitItem())
            viewModel.add(TrackEditingEvent.Save(track))
            assert(TrackEditingState.SaveCompleted == awaitItem())
            verify(tracksRepository).updateTrack(track)
        }
    }

    @Test
    fun someTest2() = runTest {

        viewModel.stateFlow.test {
            assert(TrackEditingState.Initial == awaitItem())
            viewModel.add(
                TrackEditingEvent.Save(
                    track.copy(name = "A".repeat(TrackProperties.MAX_LENGTH_NAME + 1))
                )
            )
            assert(TrackEditingState.SaveFailed(TrackValidationResult.Invalid.Name) == awaitItem())
            verifyNoInteractions(tracksRepository)
        }
    }

    @Test
    fun someTest3() = runTest {

        viewModel.stateFlow.test {
            assert(TrackEditingState.Initial == awaitItem())
            viewModel.add(
                TrackEditingEvent.Save(
                    track.copy(description = "A".repeat(TrackProperties.MAX_LENGTH_DESCRIPTION + 1))
                )
            )
            assert(TrackEditingState.SaveFailed(TrackValidationResult.Invalid.Description) == awaitItem())
            verifyNoInteractions(tracksRepository)
        }
    }
}

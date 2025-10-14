package io.mityukov.geo.tracking.feature.track.details

import app.cash.turbine.test
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.core.data.validation.TrackValidationResult
import io.mityukov.geo.tracking.core.data.validation.TrackValidator
import io.mityukov.geo.tracking.feature.track.editing.TrackEditingEvent
import io.mityukov.geo.tracking.feature.track.editing.TrackEditingState
import io.mityukov.geo.tracking.feature.track.editing.TrackEditingViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class TrackEditingViewModelTest {
    private lateinit var tracksRepository: TracksRepository
    private lateinit var viewModel: TrackEditingViewModel
    private lateinit var mockTrackValidator: TrackValidator


    @Before
    fun setUp() {
        tracksRepository = mock<TracksRepository>()
        mockTrackValidator = mock<TrackValidator>()
        viewModel = TrackEditingViewModel(mockTrackValidator, tracksRepository)
    }

    @Test
    fun saveValidTrackUpdatesTrackInRepository() = runTest {
        `when`(mockTrackValidator.validate(any())).thenReturn(TrackValidationResult.Valid)
        viewModel.stateFlow.test {
            assert(TrackEditingState.Initial == awaitItem())
            viewModel.add(TrackEditingEvent.Save(track))
            assert(TrackEditingState.SaveCompleted == awaitItem())
            verify(tracksRepository).updateTrack(track)
        }
    }

    @Test
    fun saveInvalidTrackProducesTrackValidationResultInvalidName() = runTest {
        `when`(mockTrackValidator.validate(any())).thenReturn(TrackValidationResult.Invalid.Name)
        viewModel.stateFlow.test {
            assert(TrackEditingState.Initial == awaitItem())
            viewModel.add(TrackEditingEvent.Save(track))
            assert(TrackEditingState.SaveFailed(TrackValidationResult.Invalid.Name) == awaitItem())
            verifyNoInteractions(tracksRepository)
        }
    }

    @Test
    fun saveInvalidTrackProducesTrackValidationResultInvalidDescription() = runTest {
        `when`(mockTrackValidator.validate(any())).thenReturn(TrackValidationResult.Invalid.Description)
        viewModel.stateFlow.test {
            assert(TrackEditingState.Initial == awaitItem())
            viewModel.add(TrackEditingEvent.Save(track))
            assert(TrackEditingState.SaveFailed(TrackValidationResult.Invalid.Description) == awaitItem())
            verifyNoInteractions(tracksRepository)
        }
    }
}

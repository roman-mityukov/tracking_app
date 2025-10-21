package io.mityukov.geo.tracking.core.data.repository.track

import app.cash.turbine.test
import io.mityukov.geo.tracking.core.data.repository.RepositoryFailure
import io.mityukov.geo.tracking.core.data.repository.RepositoryResult
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.core.model.track.DetailedTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class TracksRepositoryTest {
    private lateinit var tracksLocalDataSource: TracksLocalDataSource
    private lateinit var tracksRawLocalDataSource: TracksRawLocalDataSource
    private lateinit var tracksRepository: TracksRepository

    @Before
    fun setUp() {
        tracksLocalDataSource = FakeTracksLocalDataSource(mutableListOf(track))
        tracksRawLocalDataSource = mock()
        tracksRepository = TracksRepositoryImpl(
            tracksLocalDataSource = tracksLocalDataSource,
            tracksRawLocalDataSource = tracksRawLocalDataSource,
            coroutineDispatcher = Dispatchers.IO,
        )
    }

    @Test
    fun getAllTracksReturnsTracksFromLocalDataSource() = runTest {
        tracksRepository.readAllTracks().test {
            val tracksList = awaitItem()
            assert(tracksList.size == 1)
            assert(tracksList.first().id == track.id)
            awaitComplete()
        }
    }

    @Test
    fun getTrackReturnsTrackFromLocalDataSource() = runTest {
        tracksRepository.readTrack(track.id).test {
            val localTrack = awaitItem()
            assert(localTrack == track)
            awaitComplete()
        }
    }

    @Test
    fun readDetailedTrackReturnsDetailedTrack() = runTest {
        `when`(tracksRawLocalDataSource.readTrackGeolocations(any())).thenReturn(listOf(Geolocation.empty()))
        val result = tracksRepository.readDetailedTrack(track.id)
        assert(result.isSuccess)
        assert((result as RepositoryResult.Success<DetailedTrack>).data.track == track)
        assert(result.data.geolocations.size == 1)
        assert(result.data.geolocations.first() == Geolocation.empty())
    }

    @Test
    fun readDetailedTrackFailedWithIoException() = runTest {
        `when`(tracksRawLocalDataSource.readTrackGeolocations(any())).doAnswer {
            throw IOException()
        }
        val result = tracksRepository.readDetailedTrack(track.id)
        assert(result.isFailure)
        assert((result as RepositoryResult.Failure<*>).cause == RepositoryFailure.IO)
    }

    @Test
    fun readCapturedTrackGeolocationsReturnsGeolocationsFromTempFile() = runTest {
        `when`(tracksRawLocalDataSource.readCapturedGeolocations()).thenReturn(listOf(Geolocation.empty()))
        val result = tracksRepository.readCapturedTrackGeolocations()
        assert(result.isSuccess)
        assert((result as RepositoryResult.Success<List<Geolocation>>).data.size == 1)
        assert(result.data.first() == Geolocation.empty())
    }

    @Test
    fun readCapturedTrackGeolocationsFailedWithIoException() = runTest {
        `when`(tracksRawLocalDataSource.readCapturedGeolocations()).doAnswer {
            throw IOException()
        }
        val result = tracksRepository.readCapturedTrackGeolocations()
        assert(result.isFailure)
        assert((result as RepositoryResult.Failure<*>).cause == RepositoryFailure.IO)
    }

    @Test
    fun createTrack() = runTest {
        `when`(tracksRawLocalDataSource.writeCapturedGeolocationsAsTrack(any())).thenReturn("someFile")
        tracksRepository.createTrack(trackInProgress)
        val tracks = tracksRepository.readAllTracks().first()
        assert(tracks.size == 2)
    }

    @Test
    fun createTrackFailedWithIoException() = runTest {
        `when`(tracksRawLocalDataSource.writeCapturedGeolocationsAsTrack(any())).doAnswer {
            throw IOException()
        }
        val result = tracksRepository.createTrack(trackInProgress)
        assert(result.isFailure)
        assert((result as RepositoryResult.Failure<*>).cause == RepositoryFailure.IO)
    }

    @Test
    fun createTrackPointWritesToTempFile() = runTest {
        val geolocation = Geolocation.empty()
        tracksRepository.createTrackPoint(geolocation)

        verify(tracksRawLocalDataSource).writeGeolocation(any())
    }

    @Test
    fun createTrackPointFailedWithIoException() = runTest {
        `when`(tracksRawLocalDataSource.writeGeolocation(any())).doAnswer {
            throw IOException()
        }
        val geolocation = Geolocation.empty()
        val result = tracksRepository.createTrackPoint(geolocation)
        assert(result.isFailure)
        assert((result as RepositoryResult.Failure<*>).cause == RepositoryFailure.IO)
    }

    @Test
    fun updateTrack() = runTest {
        val updatedTrackName = "updatedName"
        val track = tracksRepository.readTrack(track.id).first()
        assert(track.name != updatedTrackName)
        tracksRepository.updateTrack(track.copy(name = "updatedName"))
        val updatedTrack = tracksRepository.readTrack(track.id).first()
        assert(updatedTrack.name == updatedTrackName)
    }

    @Test
    fun deleteTrack() = runTest {
        tracksRepository.deleteTrack(track.id)
        val tracks = tracksRepository.readAllTracks().first()
        assert(tracks.all { it.id != track.id })
    }
}

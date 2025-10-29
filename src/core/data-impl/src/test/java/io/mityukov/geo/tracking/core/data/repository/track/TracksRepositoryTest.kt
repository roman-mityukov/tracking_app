package io.mityukov.geo.tracking.core.data.repository.track

import app.cash.turbine.test
import io.mityukov.geo.tracking.core.data.repository.RepositoryFailure
import io.mityukov.geo.tracking.core.data.repository.RepositoryResult
import io.mityukov.geo.tracking.core.data.repository.track.TracksRawLocalDataSourceImpl.Companion.TEMP_FILE_NAME
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
import org.robolectric.RobolectricTestRunner
import java.io.File
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
        val tracksDirectory = File("./")
        val rawDataSource = TracksRawLocalDataSourceImpl(tracksDirectory)
        val repository = TracksRepositoryImpl(
            tracksLocalDataSource = tracksLocalDataSource,
            tracksRawLocalDataSource = rawDataSource,
            coroutineDispatcher = Dispatchers.IO,
        )
        val geolocation = Geolocation.empty()
        repository.createTrackPoint(geolocation)
        repository.createTrackPoint(geolocation)

        val tempFile = File(tracksDirectory, TEMP_FILE_NAME)
        val tempFileText = tempFile.readText()
        tempFile.delete()
        assert(
            tempFileText == "point,0.0,0.0,0.0,0.0,0\n" +
                    "point,0.0,0.0,0.0,0.0,0\n"
        )
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

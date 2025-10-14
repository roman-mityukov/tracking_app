package io.mityukov.geo.tracking.core.data.repository.track

import app.cash.turbine.test
import io.mityukov.geo.tracking.core.common.time.TimeUtils
import io.mityukov.geo.tracking.core.gpx.GpxHelper
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.time.format.DateTimeFormatter

@RunWith(RobolectricTestRunner::class)
class TracksRepositoryTest {
    private lateinit var tracksLocalDataSource: TracksLocalDataSource
    private lateinit var tracksRepository: TracksRepository
    private val tracksDirectory = File("./")
    private val tempFile = File(tracksDirectory, TracksRepositoryImpl.TEMP_TRACK_FILE_NAME)
    private val gpxFile = File(tracksDirectory, track.filePath)
    private val createdGpxFile = File(
        tracksDirectory,
        "${
            TimeUtils.getFormattedLocalFromUTC(
                trackInProgress.start,
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
            )
        }.gpx"
    )

    @Before
    fun setUp() {
        tracksLocalDataSource = FakeTracksLocalDataSource(mutableListOf(track))
        tracksRepository = TracksRepositoryImpl(
            gpxHelper = GpxHelper(),
            tracksLocalDataSource = tracksLocalDataSource,
            tracksDirectory = tracksDirectory,
            coroutineDispatcher = Dispatchers.IO,
        )
    }

    @After
    fun tearDown() {
        tempFile.delete()
        gpxFile.delete()
        createdGpxFile.delete()
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
        gpxFile.writeText(gpxFileContent)
        val detailedTrack = tracksRepository.readDetailedTrack(track.id)
        assert(detailedTrack.track == track)
        assert(detailedTrack.geolocations.size == 2)
    }

    @Test
    fun readCapturedTrackGeolocationsReturnsGeolocationsFromTempFile() = runTest {
        tempFile.writeText(TEMP_FILE_CONTENT)
        val geolocations = tracksRepository.readCapturedTrackGeolocations()
        assert(geolocations.size == 2)
    }

    @Test
    fun createTrack() = runTest {
        tempFile.writeText(TEMP_FILE_CONTENT)
        tracksRepository.createTrack(trackInProgress)
        assert(tempFile.readText() == "")
        val tracks = tracksRepository.readAllTracks().first()
        assert(tracks.size == 2)
        assert(createdGpxFile.exists())
    }

    @Test
    fun createTrackPointWritesToTempFile() = runTest {
        val geolocation = Geolocation.empty()
        tracksRepository.createTrackPoint(geolocation)

        val fileContent = tempFile.readText()
        println(fileContent)
        assert(fileContent == "point,0.0,0.0,0.0,0.0,0\n")
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

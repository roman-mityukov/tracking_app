package io.mityukov.geo.tracking.core.data.repository.track

import io.mityukov.geo.tracking.core.model.geo.Geolocation
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
class TracksRawLocalDataSourceImplTest {
    private lateinit var tracksRawLocalDataSourceImpl: TracksRawLocalDataSourceImpl
    private val tracksDirectory: File = File("./")
    private val inputFileContent = "point,53.84632,86.43543,267.34,1.5,1759973312000\n" +
            "point,53.84645,86.43545,261.51,1.2,1759973313000\n"
    private val outputContent = """
        <?xml version="1.0" encoding="UTF-8"?>
        <gpx version="1.1" creator="Tracking app" xmlns="http://www.topografix.com/GPX/1/1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
          <trk>
            <name>name</name>
            <trkseg>
              <trkpt lat="53.84632" lon="86.43543">
                <ele>267.34</ele>
                <time>2025-10-09T01:28:32Z</time>
                <extensions speed="1.5"/>
              </trkpt>
              <trkpt lat="53.84645" lon="86.43545">
                <ele>261.51</ele>
                <time>2025-10-09T01:28:33Z</time>
                <extensions speed="1.2"/>
              </trkpt>
            </trkseg>
          </trk>
        </gpx>
    """.trimIndent()
    private lateinit var tempFile: File
    private lateinit var outputFile: File
    private val outputFileName: String = "outputFile.gpx"


    @Before
    fun setup() {
        tempFile = File(tracksDirectory, TracksRawLocalDataSourceImpl.TEMP_FILE_NAME)
        outputFile = File(tracksDirectory, outputFileName)
        tracksRawLocalDataSourceImpl = TracksRawLocalDataSourceImpl(tracksDirectory)
    }

    @After
    fun tearDown() {
        tempFile.delete()
        outputFile.delete()
    }

    @Test
    fun testClear() {
        tempFile.writeText(inputFileContent)
        tracksRawLocalDataSourceImpl.clear()
        assert(tempFile.readText() == "")
    }

    @Test
    fun testWriteGeolocation() {
        tracksRawLocalDataSourceImpl.writeGeolocation(Geolocation.empty())
        assert(tempFile.readText() == "point,0.0,0.0,0.0,0.0,0\n")
    }

    @Test
    fun testReadCapturedGeolocations() {
        tempFile.writeText(inputFileContent)
        val geolocations = tracksRawLocalDataSourceImpl.readCapturedGeolocations()
        assert(geolocations.size == 2)

        val firstPoint = geolocations[0]
        assertEquals(53.84632, firstPoint.latitude, 0.00001)
        assertEquals(86.43543, firstPoint.longitude, 0.00001)
        assertEquals(267.34, firstPoint.altitude, 0.01)
        assertEquals(1.5f, firstPoint.speed, 0.1f)
        assertEquals(1759973312000, firstPoint.time)

        val secondPoint = geolocations[1]
        assertEquals(53.84645, secondPoint.latitude, 0.00001)
        assertEquals(86.43545, secondPoint.longitude, 0.00001)
        assertEquals(261.51, secondPoint.altitude, 0.01)
        assertEquals(1.2f, secondPoint.speed, 0.1f)
        assertEquals(1759973313000, secondPoint.time)
    }

    @Test
    fun readTrackGeolocations() {
        outputFile.writeText(outputContent)
        val geolocations = tracksRawLocalDataSourceImpl.readTrackGeolocations(outputFile)
        assert(geolocations.size == 2)

        val firstPoint = geolocations[0]
        assertEquals(53.84632, firstPoint.latitude, 0.00001)
        assertEquals(86.43543, firstPoint.longitude, 0.00001)
        assertEquals(267.34, firstPoint.altitude, 0.01)
        assertEquals(1.5f, firstPoint.speed, 0.1f)
        assertEquals(1759973312000, firstPoint.time)

        val secondPoint = geolocations[1]
        assertEquals(53.84645, secondPoint.latitude, 0.00001)
        assertEquals(86.43545, secondPoint.longitude, 0.00001)
        assertEquals(261.51, secondPoint.altitude, 0.01)
        assertEquals(1.2f, secondPoint.speed, 0.1f)
        assertEquals(1759973313000, secondPoint.time)
    }

    @Test
    fun testWriteCapturedGeolocationsAsTrack() {
        tempFile.writeText(inputFileContent)
        tracksRawLocalDataSourceImpl.writeCapturedGeolocationsAsTrack(outputFileName)
        assert(outputFile.readText() == outputContent)
    }
}

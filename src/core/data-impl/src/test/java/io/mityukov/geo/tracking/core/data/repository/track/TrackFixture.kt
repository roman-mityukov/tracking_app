package io.mityukov.geo.tracking.core.data.repository.track

import android.location.Location
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackInProgress
import io.mityukov.geo.tracking.core.model.track.Track
import kotlin.time.Duration.Companion.seconds

val track = Track(
    id = "trackEntityId",
    description = "description",
    name = "trackEntityName",
    start = 123,
    end = 456,
    distance = 789.3f,
    altitudeUp = 3.3f,
    altitudeDown = 4.1f,
    duration = 123L.seconds,
    sumSpeed = 24.5f,
    minSpeed = 12.1f,
    maxSpeed = 13.2f,
    geolocationCount = 3,
    filePath = "./trackFilePath.gpx",
)

val trackInProgress = TrackInProgress(
    start = 123,
    distance = 789.3f,
    altitudeUp = 3.3f,
    altitudeDown = 4.1f,
    duration = 123L.seconds,
    sumSpeed = 24.5f,
    minSpeed = 12.1f,
    maxSpeed = 13.2f,
    geolocationCount = 3,
    paused = false,
    lastLocation = Location("GPS").apply {
        latitude = 0.0
        longitude = 0.0
        time = 123
    },
)

const val TEMP_FILE_CONTENT = "point,53.84632,86.43543,267.34,1.5,1759973312000\n" +
        "point,53.84645,86.43545,261.51,1.2,1759973313000\n"
val gpxFileContent = """
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

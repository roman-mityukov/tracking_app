@file:Suppress("NestedBlockDepth")

package io.mityukov.geo.tracking.core.data.repository.track

import io.mityukov.geo.tracking.app.AppProps
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackInProgress
import io.mityukov.geo.tracking.core.database.dao.TrackDao
import io.mityukov.geo.tracking.core.database.model.TrackEntity
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.core.model.track.DetailedTrack
import io.mityukov.geo.tracking.core.model.track.Track
import io.mityukov.geo.tracking.di.DispatcherIO
import io.mityukov.geo.tracking.di.TracksDirectory
import io.mityukov.geo.tracking.utils.time.TimeUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.InputStream
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TracksRepositoryImpl @Inject constructor(
    private val trackDao: TrackDao,
    private val trackMapper: TrackMapper,
    @param:TracksDirectory private val tracksDirectory: File,
    @param:DispatcherIO private val coroutineDispatcher: CoroutineDispatcher,
) : TracksRepository {
    private val tempTrackFileName = "temp.gpx"

    override val tracks: Flow<List<Track>> = trackDao.getAllTracks().map {
        it.map {
            trackMapper.trackEntityToDomain(it)
        }
    }

    override suspend fun getTrack(trackId: String): Track = withContext(coroutineDispatcher) {
        val entity = trackDao.getTrack(trackId)
        trackMapper.trackEntityToDomain(entity)
    }

    override suspend fun getDetailedTrack(trackId: String): DetailedTrack =
        withContext(coroutineDispatcher) {
            val trackMetadata = trackDao.getTrack(trackId)
            val file = File(trackMetadata.filePath)
            val inputStream = file.inputStream()
            val geolocations = geolocationsFromGpx(inputStream)
            inputStream.close()
            DetailedTrack(
                data = trackMapper.trackEntityToDomain(trackMetadata),
                geolocations = geolocations,
            )
        }

    override suspend fun getAllGeolocations(): List<Geolocation> =
        withContext(coroutineDispatcher) {
            val trackFile = File(tracksDirectory, tempTrackFileName)
            val listStrings = if (trackFile.exists()) trackFile.readLines() else listOf()
            val geolocations = listStrings.map {
                val parts = it.split(",")
                Geolocation(
                    latitude = parts[1].toDouble(),
                    longitude = parts[2].toDouble(),
                    altitude = parts[3].toDouble(),
                    speed = 0f,
                    time = parts[5].toLong(),
                )
            }
            geolocations
        }

    override suspend fun deleteTrack(trackId: String) = withContext(coroutineDispatcher) {
        val trackEntity = trackDao.getTrack(trackId)
        val file = File(trackEntity.filePath)
        file.delete()
        trackDao.deleteTrack(trackId)
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun insertTrack(trackInProgress: TrackInProgress) =
        withContext(coroutineDispatcher) {
            val trackFile = File(tracksDirectory, tempTrackFileName)

            val gpxFile =
                File(
                    tracksDirectory,
                    "${
                        TimeUtils.getFormattedLocalFromUTC(
                            trackInProgress.start,
                            AppProps.UI_DATE_TIME_FORMATTER
                        )
                    }.gpx"
                )
            val gpxBuilder = StringBuilder()
            // GPX Header
            gpxBuilder.append("""<?xml version="1.0" encoding="UTF-8"?>""")
            gpxBuilder.append("\n<gpx version=\"1.1\" creator=\"Tracking app\" ")
            gpxBuilder.append("xmlns=\"http://www.topografix.com/GPX/1/1\" ")
            gpxBuilder.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ")
            gpxBuilder.append(
                "xsi:schemaLocation=" +
                        "\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n"
            )

            // Track
            gpxBuilder.append("  <trk>\n")
            gpxBuilder.append("    <name>${trackInProgress.start}</name>\n")
            gpxBuilder.append("    <trkseg>\n")

            val listStrings = if (trackFile.exists()) trackFile.readLines() else listOf()
            listStrings.forEach {
                val parts = it.split(",")
                if (parts[0] == "point") {
                    gpxBuilder.append("      <trkpt lat=\"${parts[1]}\" lon=\"${parts[2]}\">\n")
                    gpxBuilder.append("        <ele>${parts[3]}</ele>\n")
                    gpxBuilder.append("        <time>${TimeUtils.getFormattedUtcTime(parts[5].toLong())}</time>\n")
                    gpxBuilder.append("        <extensions speed=\"${parts[4]}\"/>\n")
                    gpxBuilder.append("      </trkpt>\n")
                }
            }

            // Close tags
            gpxBuilder.append("    </trkseg>\n")
            gpxBuilder.append("  </trk>\n")
            gpxBuilder.append("</gpx>")

            gpxFile.writeText(gpxBuilder.toString())

            val track = TrackEntity(
                id = Uuid.random().toString(),
                name = "name",
                start = trackInProgress.start,
                end = System.currentTimeMillis(),
                duration = trackInProgress.duration.inWholeSeconds,
                distance = trackInProgress.distance,
                altitudeUp = trackInProgress.altitudeUp,
                altitudeDown = trackInProgress.altitudeDown,
                sumSpeed = trackInProgress.sumSpeed,
                minSpeed = trackInProgress.minSpeed,
                maxSpeed = trackInProgress.maxSpeed,
                geolocationCount = trackInProgress.geolocationCount,
                filePath = gpxFile.absolutePath
            )
            trackDao.insertTrack(track)

            trackFile.writeText("")
        }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun insertTrackPoint(location: Geolocation) =
        withContext(coroutineDispatcher) {
            val trackFile = File(tracksDirectory, tempTrackFileName)
            trackFile.appendText(
                "point,${location.latitude},${location.longitude}," +
                        "${location.altitude},${location.speed},${location.time}\n"
            )
        }

    private fun geolocationsFromGpx(inputStream: InputStream): List<Geolocation> {
        val points = mutableListOf<Geolocation>()
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(inputStream, null)

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "trkpt") {
                val lat = parser.getAttributeValue(null, "lat").toDouble()
                val lon = parser.getAttributeValue(null, "lon").toDouble()
                var ele: Double? = null
                var time: Long? = null
                var speed: Float? = null

                // Чтение вложенных элементов (ele, time)
                while (!(eventType == XmlPullParser.END_TAG && parser.name == "trkpt")) {
                    if (eventType == XmlPullParser.START_TAG) {
                        when (parser.name) {
                            "ele" -> ele = parser.nextText().toDouble()
                            "time" -> time = TimeUtils.getUtcMilliseconds(parser.nextText())
                            "extensions" -> speed =
                                parser.getAttributeValue(null, "speed").toFloat()
                        }
                    }
                    eventType = parser.next()
                }

                points.add(
                    Geolocation(
                        latitude = lat,
                        longitude = lon,
                        altitude = ele ?: 0.0,
                        speed = speed ?: 0f,
                        time = time ?: 0
                    )
                )
            }
            eventType = parser.next()
        }
        return points
    }
}

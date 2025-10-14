package io.mityukov.geo.tracking.core.data.repository.track

import io.mityukov.geo.tracking.core.common.di.DispatcherIO
import io.mityukov.geo.tracking.core.common.time.TimeUtils
import io.mityukov.geo.tracking.core.data.di.TracksDirectory
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackInProgress
import io.mityukov.geo.tracking.core.gpx.GpxHelper
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.core.model.track.DetailedTrack
import io.mityukov.geo.tracking.core.model.track.Track
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext
import java.io.File
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class TracksRepositoryImpl @Inject constructor(
    private val gpxHelper: GpxHelper,
    private val tracksLocalDataSource: TracksLocalDataSource,
    @param:TracksDirectory private val tracksDirectory: File,
    @param:DispatcherIO private val coroutineDispatcher: CoroutineDispatcher,
) : TracksRepository {
    companion object {
        const val TEMP_TRACK_FILE_NAME = "temp.gpx"
    }

    private val cachedGeolocations = mutableListOf<Geolocation>()

    override fun readAllTracks(): Flow<List<Track>> = tracksLocalDataSource.getAllTracks()

    override fun readTrack(trackId: String): Flow<Track> =
        tracksLocalDataSource.getTrackUpdates(trackId)
            .filterNotNull()

    override suspend fun readDetailedTrack(trackId: String): DetailedTrack =
        withContext(coroutineDispatcher) {
            val track = tracksLocalDataSource.getTrack(trackId)
            val geolocations = gpxHelper.geolocationsFromGpx(File(track.filePath))
            DetailedTrack(
                track = track,
                geolocations = geolocations,
            )
        }

    override suspend fun readCapturedTrackGeolocations(): List<Geolocation> =
        withContext(coroutineDispatcher) {
            if (cachedGeolocations.isEmpty()) {
                val trackFile = File(tracksDirectory, TEMP_TRACK_FILE_NAME)
                val listStrings = if (trackFile.exists()) trackFile.readLines() else listOf()
                cachedGeolocations.addAll(listStrings.map {
                    val parts = it.split(",")
                    Geolocation(
                        latitude = parts[1].toDouble(),
                        longitude = parts[2].toDouble(),
                        altitude = parts[3].toDouble(),
                        speed = 0f,
                        time = parts[5].toLong(),
                    )
                })
            }

            cachedGeolocations
        }

    override suspend fun deleteTrack(trackId: String) = withContext(coroutineDispatcher) {
        val track = tracksLocalDataSource.getTrack(trackId)
        val file = File(track.filePath)
        file.delete()
        tracksLocalDataSource.deleteTrack(trackId)
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createTrack(trackInProgress: TrackInProgress) =
        withContext(coroutineDispatcher) {
            cachedGeolocations.clear()
            val trackFile = File(tracksDirectory, TEMP_TRACK_FILE_NAME)

            val gpxFile =
                File(
                    tracksDirectory,
                    "${
                        TimeUtils.getFormattedLocalFromUTC(
                            trackInProgress.start,
                            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
                        )
                    }.gpx"
                )
            gpxHelper.convertToGpx(trackFile, gpxFile)

            val track = Track(
                id = Uuid.random().toString(),
                name = "Track name",
                description = "",
                start = trackInProgress.start,
                end = System.currentTimeMillis(),
                duration = trackInProgress.duration,
                distance = trackInProgress.distance,
                altitudeUp = trackInProgress.altitudeUp,
                altitudeDown = trackInProgress.altitudeDown,
                sumSpeed = trackInProgress.sumSpeed,
                minSpeed = trackInProgress.minSpeed,
                maxSpeed = trackInProgress.maxSpeed,
                geolocationCount = trackInProgress.geolocationCount,
                filePath = gpxFile.absolutePath
            )
            tracksLocalDataSource.insertTrack(track)

            trackFile.writeText("")
        }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createTrackPoint(location: Geolocation) =
        withContext(coroutineDispatcher) {
            cachedGeolocations.add(location)
            val trackFile = File(tracksDirectory, TEMP_TRACK_FILE_NAME)
            trackFile.appendText(
                "point,${location.latitude},${location.longitude}," +
                        "${location.altitude},${location.speed},${location.time}\n"
            )
        }

    override suspend fun updateTrack(track: Track) =
        withContext(coroutineDispatcher) {
            tracksLocalDataSource.updateTrack(track)
        }
}

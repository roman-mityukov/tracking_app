package io.mityukov.geo.tracking.core.data.repository.track

import io.mityukov.geo.tracking.core.common.di.DispatcherIO
import io.mityukov.geo.tracking.core.common.time.TimeUtils
import io.mityukov.geo.tracking.core.data.di.TracksDirectory
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackInProgress
import io.mityukov.geo.tracking.core.database.dao.TrackDao
import io.mityukov.geo.tracking.core.database.model.TrackEntity
import io.mityukov.geo.tracking.core.gpx.GpxHelper
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.core.model.track.DetailedTrack
import io.mityukov.geo.tracking.core.model.track.Track
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class TracksRepositoryImpl @Inject constructor(
    private val gpxHelper: GpxHelper,
    private val trackDao: TrackDao,
    private val trackMapper: TrackMapper,
    @param:TracksDirectory private val tracksDirectory: File,
    @param:DispatcherIO private val coroutineDispatcher: CoroutineDispatcher,
) : TracksRepository {
    private val tempTrackFileName = "temp.gpx"
    private val cachedGeolocations = mutableListOf<Geolocation>()

    override val tracks: Flow<List<Track>> = trackDao.getAllTracks().map {
        locationFromByteArray(byteArrayOf())
        it.map {
            trackMapper.trackEntityToDomain(it)
        }
    }

    override suspend fun getTrack(trackId: String): Track = withContext(coroutineDispatcher) {
        val entity = trackDao.getTrack(trackId)
        trackMapper.trackEntityToDomain(entity)
    }

    override fun getTrackUpdates(trackId: String): Flow<Track> {
        return trackDao.getTrackUpdates(trackId)
            .filterNotNull()
            .map { entity -> trackMapper.trackEntityToDomain(entity) }
    }

    override suspend fun getDetailedTrack(trackId: String): DetailedTrack =
        withContext(coroutineDispatcher) {
            val trackMetadata = trackDao.getTrack(trackId)
            val geolocations = gpxHelper.geolocationsFromGpx(File(trackMetadata.filePath))
            DetailedTrack(
                track = trackMapper.trackEntityToDomain(trackMetadata),
                geolocations = geolocations,
            )
        }

    override suspend fun getCapturedTrackGeolocations(): List<Geolocation> =
        withContext(coroutineDispatcher) {
            if (cachedGeolocations.isEmpty()) {
                val trackFile = File(tracksDirectory, tempTrackFileName)
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
        val trackEntity = trackDao.getTrack(trackId)
        val file = File(trackEntity.filePath)
        file.delete()
        trackDao.deleteTrack(trackId)
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun insertTrack(trackInProgress: TrackInProgress) =
        withContext(coroutineDispatcher) {
            cachedGeolocations.clear()
            val trackFile = File(tracksDirectory, tempTrackFileName)

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

            val track = TrackEntity(
                id = Uuid.random().toString(),
                name = "Track name",
                description = null,
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
            cachedGeolocations.add(location)
            val trackFile = File(tracksDirectory, tempTrackFileName)
            trackFile.appendText(
                "point,${location.latitude},${location.longitude}," +
                        "${location.altitude},${location.speed},${location.time}\n"
            )
        }

    override suspend fun updateTrack(track: Track) =
        withContext(coroutineDispatcher) {
            trackDao.updateTrack(trackMapper.trackDomainToEntity(track))
        }
}

package io.mityukov.geo.tracking.core.data.repository.track

import io.mityukov.geo.tracking.core.common.di.DispatcherIO
import io.mityukov.geo.tracking.core.common.time.TimeUtils
import io.mityukov.geo.tracking.core.data.repository.RepositoryFailure
import io.mityukov.geo.tracking.core.data.repository.RepositoryResult
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackInProgress
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.core.model.track.DetailedTrack
import io.mityukov.geo.tracking.core.model.track.Track
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class TracksRepositoryImpl @Inject constructor(
    private val tracksLocalDataSource: TracksLocalDataSource,
    private val tracksRawLocalDataSource: TracksRawLocalDataSource,
    @param:DispatcherIO private val coroutineDispatcher: CoroutineDispatcher,
) : TracksRepository {
    private val cachedGeolocations = mutableListOf<Geolocation>()

    override fun readAllTracks(): Flow<List<Track>> = tracksLocalDataSource.getAllTracks()

    override fun readTrack(trackId: String): Flow<Track> =
        tracksLocalDataSource.getTrackUpdates(trackId)
            .filterNotNull()

    override suspend fun readDetailedTrack(trackId: String): RepositoryResult<DetailedTrack> =
        withContext(coroutineDispatcher) {
            runCatchingIOException {
                val track = tracksLocalDataSource.getTrack(trackId)
                val geolocations =
                    tracksRawLocalDataSource.readTrackGeolocations(File(track.filePath))
                DetailedTrack(
                    track = track,
                    geolocations = geolocations,
                )
            }
        }

    override suspend fun readCapturedTrackGeolocations(): RepositoryResult<List<Geolocation>> =
        withContext(coroutineDispatcher) {
            runCatchingIOException {
                if (cachedGeolocations.isEmpty()) {
                    val geolocations = tracksRawLocalDataSource.readCapturedGeolocations()
                    cachedGeolocations.addAll(geolocations)
                }
                cachedGeolocations
            }
        }

    // TODO надо проверять целостность
    override suspend fun deleteTrack(trackId: String): RepositoryResult<Unit> =
        withContext(coroutineDispatcher) {
            runCatchingIOException {
                val track = tracksLocalDataSource.getTrack(trackId)
                val file = File(track.filePath)
                file.delete()
                tracksLocalDataSource.deleteTrack(trackId)
            }
        }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createTrack(trackInProgress: TrackInProgress): RepositoryResult<Unit> =
        withContext(coroutineDispatcher) {
            runCatchingIOException {
                cachedGeolocations.clear()
                val trackFilePath = tracksRawLocalDataSource.writeCapturedGeolocationsAsTrack(
                    "${
                        TimeUtils.getFormattedLocalFromUTC(
                            trackInProgress.start,
                            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
                        )
                    }.gpx"
                )

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
                    filePath = trackFilePath
                )
                tracksLocalDataSource.insertTrack(track)

                tracksRawLocalDataSource.clear()
            }
        }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createTrackPoint(geolocation: Geolocation): RepositoryResult<Unit> =
        withContext(coroutineDispatcher) {
            runCatchingIOException {
                tracksRawLocalDataSource.writeGeolocation(geolocation)
                cachedGeolocations.add(geolocation)
                Unit
            }
        }

    override suspend fun updateTrack(track: Track): RepositoryResult<Unit> =
        withContext(coroutineDispatcher) {
            runCatchingIOException {
                tracksLocalDataSource.updateTrack(track)
            }
        }

    private inline fun <D> runCatchingIOException(block: () -> D): RepositoryResult<D> {
        return try {
            RepositoryResult.Success(block.invoke())
        } catch (_: IOException) {
            RepositoryResult.Failure(RepositoryFailure.IO)
        }
    }
}

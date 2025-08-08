@file:Suppress("TooManyFunctions")
package io.mityukov.geo.tracking.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.mityukov.geo.tracking.core.database.model.TrackActionEntity
import io.mityukov.geo.tracking.core.database.model.TrackEntity
import io.mityukov.geo.tracking.core.database.model.TrackPointEntity
import io.mityukov.geo.tracking.core.database.model.TrackWithPoints
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM track")
    fun getAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM track")
    fun getAllTracksWithPoints(): Flow<List<TrackWithPoints>>

    @Query("SELECT * FROM track WHERE id=:id")
    fun getTrack(id: String): TrackEntity

    @Query("SELECT * FROM track WHERE id=:id")
    fun getTrackWithPoints(id: String): Flow<TrackWithPoints>

    @Query("SELECT * FROM track_point WHERE track_id=:trackId")
    fun getTrackPoints(trackId: String): Flow<List<TrackPointEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTrack(trackEntity: TrackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTrackPoint(trackPointEntity: TrackPointEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTrackAction(trackActionEntity: TrackActionEntity)

    @Query("DELETE FROM track WHERE id=:trackId")
    fun deleteTrack(trackId: String)

    @Query("DELETE FROM track_point WHERE track_id=:trackId")
    fun deleteTrackPoints(trackId: String)

    @Query("DELETE FROM track_action WHERE track_id=:trackId")
    fun deleteTrackActions(trackId: String)
}

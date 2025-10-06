@file:Suppress("TooManyFunctions")

package io.mityukov.geo.tracking.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.mityukov.geo.tracking.core.database.model.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM track")
    fun getAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM track WHERE id=:id")
    fun getTrack(id: String): TrackEntity

    @Query("SELECT * FROM track WHERE id=:id")
    fun getTrackUpdates(id: String): Flow<TrackEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTrack(trackEntity: TrackEntity)

    @Query("DELETE FROM track WHERE id=:trackId")
    fun deleteTrack(trackId: String)

    @Update
    fun updateTrack(trackEntity: TrackEntity)
}

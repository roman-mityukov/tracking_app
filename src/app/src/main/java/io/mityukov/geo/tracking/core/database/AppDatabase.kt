package io.mityukov.geo.tracking.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.mityukov.geo.tracking.core.database.dao.TrackDao
import io.mityukov.geo.tracking.core.database.model.TrackEntity
import io.mityukov.geo.tracking.core.database.model.TrackPointEntity

@Database(
    exportSchema = true,
    entities = [
        TrackEntity::class,
        TrackPointEntity::class,
    ],
    version = 1,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao

    companion object {
        @Volatile
        private var db: AppDatabase? = null
        private val lock = Object()

        fun getInstance(context: Context): AppDatabase {
            return db ?: synchronized(lock) {
                val newInstance = db ?: Room.databaseBuilder(
                    context,
                    AppDatabase::class.java, "database"
                )
                    .build().also { db = it }
                newInstance
            }
        }
    }
}

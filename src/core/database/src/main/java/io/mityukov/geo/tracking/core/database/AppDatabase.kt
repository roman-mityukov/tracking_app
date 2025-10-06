package io.mityukov.geo.tracking.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.mityukov.geo.tracking.core.database.dao.TrackDao
import io.mityukov.geo.tracking.core.database.model.TrackEntity

@Database(
    exportSchema = true,
    entities = [
        TrackEntity::class,
    ],
    version = 2,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao

    fun clear() {
        clearAllTables()
    }

    companion object {
        @Volatile
        private var db: AppDatabase? = null
        private val lock = Any()

        fun getInstance(context: Context): AppDatabase {
            return db ?: synchronized(lock) {
                if (db == null) {
                    db = Room
                        .databaseBuilder(
                            context = context,
                            klass = AppDatabase::class.java,
                            name = "database"
                        )
                        .addMigrations(MIGRATION_1_2)
                        .build()
                }

                return db!!
            }
        }
    }
}

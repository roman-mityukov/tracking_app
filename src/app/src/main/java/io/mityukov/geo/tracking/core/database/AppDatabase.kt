package io.mityukov.geo.tracking.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.mityukov.geo.tracking.core.database.dao.TrackDao
import io.mityukov.geo.tracking.core.database.model.TrackEntity
import io.mityukov.geo.tracking.core.database.model.TrackPointEntity

@Database(
    exportSchema = true,
    entities = [
        TrackEntity::class,
        TrackPointEntity::class,
    ],
    version = 2,
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
                    .addMigrations(MIGRATION_1_2)
                    .build().also { db = it }
                newInstance
            }
        }
    }
}

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE track ADD COLUMN duration INTEGER NOT NULL DEFAULT 0")
        //Создаем временную таблицу с вычисленными duration
        db.execSQL(
            """
            CREATE TEMPORARY TABLE temp_track_duration AS
            SELECT 
                t.id as track_id,
                (MAX(tp.time) - MIN(tp.time)) as duration
            FROM track t
            JOIN track_point tp ON t.id = tp.track_id
            GROUP BY t.id
        """
        )

        //Обновляем основную таблицу
        db.execSQL(
            """
            UPDATE track
            SET duration = (
                SELECT duration 
                FROM temp_track_duration 
                WHERE track_id = track.id
            )
            WHERE EXISTS (
                SELECT 1 FROM temp_track_duration WHERE track_id = track.id
            )
        """
        )

        //Удаляем временную таблицу
        db.execSQL("DROP TABLE temp_track_duration")
    }
}

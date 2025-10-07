package io.mityukov.geo.tracking.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppDatabaseMigrationTest {
    private val dbName = "migration-test"
    private val testId = "someId"
    private val testName = "testName"
    private val testDescription = "some description"
    private val tableName = "track"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Test
    fun testMigrationFromVersion1To2() {
        helper.createDatabase(dbName, 1).apply {
            execSQL(
                "INSERT INTO $tableName (" +
                        "id, " +
                        "name, " +
                        "start, " +
                        "end, " +
                        "duration, " +
                        "distance, " +
                        "altitude_up, " +
                        "altitude_down, " +
                        "sum_speed, " +
                        "min_speed, " +
                        "max_speed, " +
                        "geolocation_count, " +
                        "file_path) " +
                        "VALUES (" +
                        "'$testId', " +
                        "'$testName', " +
                        "1.0, " +
                        "1.0, " +
                        "1.0, " +
                        "1.0, " +
                        "1.0, " +
                        "1.0, " +
                        "1.0, " +
                        "1.0, " +
                        "1.0, " +
                        "1, " +
                        "'filePath')"
            )
            close()
        }

        val dbV2 = helper.runMigrationsAndValidate(dbName, 2, true, MIGRATION_1_2)
        dbV2.query("SELECT * FROM $tableName WHERE id = '$testId'").use { cursor ->
            assert(cursor.count == 1)
            cursor.moveToFirst()
            assert(cursor.getString(cursor.getColumnIndexOrThrow("id")) == testId)
            assert(cursor.getString(cursor.getColumnIndexOrThrow("name")) == testName)
            assert(cursor.isNull(cursor.getColumnIndexOrThrow("description")))
        }

        dbV2.execSQL(
            "UPDATE $tableName SET description = '$testDescription' WHERE id = '$testId'"
        )
        dbV2.query("SELECT * FROM $tableName WHERE id = 'someId'").use { cursor ->
            cursor.moveToFirst()
            assert(cursor.getString(cursor.getColumnIndexOrThrow("description")) == testDescription)
        }
    }
}

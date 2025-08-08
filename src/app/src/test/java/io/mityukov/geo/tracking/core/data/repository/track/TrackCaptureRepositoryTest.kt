package io.mityukov.geo.tracking.core.data.repository.track

import android.content.Context
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.datastore.core.DataStore
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import io.mityukov.geo.tracking.core.data.repository.geo.FakeGeolocationProviderImpl
import io.mityukov.geo.tracking.core.data.repository.settings.app.LocalAppSettings
import io.mityukov.geo.tracking.core.data.repository.settings.app.LocalAppSettingsRepository
import io.mityukov.geo.tracking.core.data.repository.settings.app.proto.ProtoLocalTrackCaptureStatus
import io.mityukov.geo.tracking.core.database.AppDatabase
import io.mityukov.geo.tracking.core.database.dao.TrackDao
import io.mityukov.geo.tracking.core.database.model.TrackEntity
import io.mityukov.geo.tracking.core.datastore.trackCaptureStatusDataStore
import io.mityukov.geo.tracking.test.utils.GeolocationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.time.Duration.Companion.milliseconds

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.VANILLA_ICE_CREAM])
class TrackCaptureRepositoryTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var dataStore: DataStore<ProtoLocalTrackCaptureStatus>
    private lateinit var db: AppDatabase
    private lateinit var dao: TrackDao

    @Before
    fun setUp() {
        dataStore = context.trackCaptureStatusDataStore
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.trackDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `repository starting causes writes to the db `() = runTest {
        val trackId = "asdf"
        val newTrackCaptureStatus = ProtoLocalTrackCaptureStatus
            .newBuilder()
            .setTrackId(trackId)
            .setTrackCaptureEnabled(true)
            .setPaused(false)
            .build()
        dataStore.updateData {
            newTrackCaptureStatus
        }

        dao.insertTrack(TrackEntity(id = trackId, name = "Random name", start = "", end = ""))

        val repositoryUnderTest = TrackCaptureRepositoryImpl(
            dataStore = dataStore,
            trackDao = dao,
            geolocationProvider = FakeGeolocationProviderImpl(mockedGeolocation = GeolocationUtils.mockedGeolocation),
            localAppSettingsRepository = mock<LocalAppSettingsRepository> {
                on { localAppSettings } doReturn listOf(
                    LocalAppSettings(
                        showOnboarding = true,
                        geolocationUpdatesInterval = 5000.milliseconds
                    )
                ).asFlow()
            },
            coroutineDispatcher = Dispatchers.IO,
        )
        repositoryUnderTest.start()
        dao.getTrackWithPoints(trackId).test {
            val track = awaitItem()
            expectThat(track.track.id) {
                isEqualTo(trackId)
            }
            expectThat(track.points.size) {
                isEqualTo(1)
            }
        }
    }
}

package io.mityukov.geo.tracking.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationUpdatesRepository
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationUpdatesRepositoryImpl
import io.mityukov.geo.tracking.core.data.repository.settings.app.LocalAppSettingsRepository
import io.mityukov.geo.tracking.core.data.repository.settings.app.LocalAppSettingsRepositoryImpl
import io.mityukov.geo.tracking.core.data.repository.settings.app.proto.ProtoLocalAppSettings
import io.mityukov.geo.tracking.core.data.repository.settings.app.proto.ProtoLocalTrackCaptureStatus
import io.mityukov.geo.tracking.core.data.repository.track.TrackCaptureService
import io.mityukov.geo.tracking.core.data.repository.track.TrackCaptureServiceImpl
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepositoryImpl
import io.mityukov.geo.tracking.core.database.AppDatabase
import io.mityukov.geo.tracking.core.database.dao.TrackDao
import io.mityukov.geo.tracking.core.datastore.appSettingsDataStore
import io.mityukov.geo.tracking.core.datastore.trackCaptureStatusDataStore
import io.mityukov.geo.tracking.di.AppSettingsDataStore
import io.mityukov.geo.tracking.di.TrackCaptureStatusDataStore

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {
    @Binds
    fun bindLocalAppSettingsRepository(impl: LocalAppSettingsRepositoryImpl): LocalAppSettingsRepository

    @Binds
    fun bindCurrentLocationRepository(impl: GeolocationUpdatesRepositoryImpl): GeolocationUpdatesRepository

    @Binds
    fun bindTrackCaptureService(impl: TrackCaptureServiceImpl): TrackCaptureService

    @Binds
    fun bindTracksRepository(impl: TracksRepositoryImpl): TracksRepository

    companion object {
        @Provides
        @AppSettingsDataStore
        fun provideAppSettingsDataStore(@ApplicationContext context: Context): DataStore<ProtoLocalAppSettings> {
            return context.appSettingsDataStore
        }

        @Provides
        @TrackCaptureStatusDataStore
        fun provideTrackCaptureStatusDataStore(@ApplicationContext context: Context): DataStore<ProtoLocalTrackCaptureStatus> {
            return context.trackCaptureStatusDataStore
        }

        @Provides
        fun providesDb(@ApplicationContext context: Context): AppDatabase {
            return AppDatabase.buildDatabase(context)
        }

        @Provides
        fun providesTrackDao(database: AppDatabase): TrackDao {
            return database.trackDao()
        }
    }
}
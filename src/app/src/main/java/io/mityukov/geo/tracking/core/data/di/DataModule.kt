@file:Suppress("TooManyFunctions")
package io.mityukov.geo.tracking.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.mityukov.geo.tracking.core.data.datastore.proto.ProtoLocalAppSettings
import io.mityukov.geo.tracking.core.data.datastore.proto.ProtoLocalTrackCaptureStatus
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationProvider
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationUpdatesRepository
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationUpdatesRepositoryImpl
import io.mityukov.geo.tracking.core.data.repository.geo.HardwareGeolocationProvider
import io.mityukov.geo.tracking.core.data.repository.settings.app.AppSettingsRepository
import io.mityukov.geo.tracking.core.data.repository.settings.app.AppSettingsRepositoryImpl
import io.mityukov.geo.tracking.core.data.repository.settings.app.LocationSettingsRepository
import io.mityukov.geo.tracking.core.data.repository.settings.app.LocationSettingsRepositoryImpl
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepositoryImpl
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackCaptureStatusProvider
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackCaptureStatusRepository
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackCaptureStatusRepositoryImpl
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackCapturerController
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackCapturerControllerImpl
import io.mityukov.geo.tracking.core.database.AppDatabase
import io.mityukov.geo.tracking.core.database.dao.TrackDao
import io.mityukov.geo.tracking.core.datastore.appSettingsDataStore
import io.mityukov.geo.tracking.core.datastore.trackCaptureStatusDataStore
import io.mityukov.geo.tracking.di.AppSettingsDataStore
import io.mityukov.geo.tracking.di.TrackCaptureStatusDataStore
import io.mityukov.geo.tracking.feature.share.LogSharingService
import io.mityukov.geo.tracking.feature.share.LogSharingServiceImpl
import io.mityukov.geo.tracking.feature.share.TrackShareService
import io.mityukov.geo.tracking.feature.share.TrackShareServiceImpl
import io.mityukov.geo.tracking.utils.permission.PermissionChecker
import io.mityukov.geo.tracking.utils.permission.PermissionCheckerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {
    @Binds
    fun bindLocalAppSettingsRepository(impl: AppSettingsRepositoryImpl): AppSettingsRepository

    @Binds
    fun bindCurrentLocationRepository(impl: GeolocationUpdatesRepositoryImpl): GeolocationUpdatesRepository

    @Binds
    fun bindGeolocationProvider(impl: HardwareGeolocationProvider): GeolocationProvider

    @Binds
    fun bindLocationSettingsRepository(impl: LocationSettingsRepositoryImpl): LocationSettingsRepository

    @Binds
    fun bindPermissionChecker(impl: PermissionCheckerImpl): PermissionChecker

    @Singleton
    @Binds
    fun bindTrackCaptureController(impl: TrackCapturerControllerImpl): TrackCapturerController

    @Singleton
    @Binds
    fun bindTracksRepository(impl: TracksRepositoryImpl): TracksRepository

    @Singleton
    @Binds
    fun bindTrackCaptureStatusRepository(impl: TrackCaptureStatusRepositoryImpl): TrackCaptureStatusRepository

    @Singleton
    @Binds
    fun bindTrackCaptureStatusProvider(impl: TrackCaptureStatusRepositoryImpl): TrackCaptureStatusProvider

    @Binds
    fun bindTrackShareService(impl: TrackShareServiceImpl): TrackShareService

    @Binds
    fun bindLogSharingService(impl: LogSharingServiceImpl): LogSharingService

    companion object {
        @Provides
        @AppSettingsDataStore
        fun provideAppSettingsDataStore(
            @ApplicationContext context: Context
        ): DataStore<ProtoLocalAppSettings> {
            return context.appSettingsDataStore
        }

        @Provides
        @TrackCaptureStatusDataStore
        fun provideTrackCaptureStatusDataStore(
            @ApplicationContext context: Context
        ): DataStore<ProtoLocalTrackCaptureStatus> {
            return context.trackCaptureStatusDataStore
        }

        @Provides
        fun providesDb(@ApplicationContext context: Context): AppDatabase {
            return AppDatabase.getInstance(context)
        }

        @Provides
        fun providesTrackDao(database: AppDatabase): TrackDao {
            return database.trackDao()
        }
    }
}

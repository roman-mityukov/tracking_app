@file:Suppress("TooManyFunctions")

package io.mityukov.geo.tracking.core.data.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.mityukov.geo.tracking.core.data.permission.PermissionChecker
import io.mityukov.geo.tracking.core.data.permission.PermissionCheckerImpl
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationProvider
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationUpdatesRepository
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationUpdatesRepositoryImpl
import io.mityukov.geo.tracking.core.data.repository.geo.HardwareGeolocationProviderImpl
import io.mityukov.geo.tracking.core.data.repository.settings.app.AppSettingsRepository
import io.mityukov.geo.tracking.core.data.repository.settings.app.AppSettingsRepositoryImpl
import io.mityukov.geo.tracking.core.data.repository.settings.app.LocationSettingsRepository
import io.mityukov.geo.tracking.core.data.repository.settings.app.LocationSettingsRepositoryImpl
import io.mityukov.geo.tracking.core.data.repository.track.TracksRawLocalDataSource
import io.mityukov.geo.tracking.core.data.repository.track.TracksRawLocalDataSourceImpl
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepository
import io.mityukov.geo.tracking.core.data.repository.track.TracksRepositoryImpl
import io.mityukov.geo.tracking.core.data.repository.track.capture.LocationChecker
import io.mityukov.geo.tracking.core.data.repository.track.capture.LocationCheckerImpl
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackCaptureStatusProvider
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackCaptureStatusRepository
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackCaptureStatusRepositoryImpl
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackCapturerController
import io.mityukov.geo.tracking.core.data.repository.track.capture.TrackCapturerControllerImpl
import io.mityukov.geo.tracking.core.data.validation.TrackValidator
import io.mityukov.geo.tracking.core.data.validation.TrackValidatorImpl
import java.io.File
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TracksDirectory

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    internal abstract fun bindLocalAppSettingsRepository(
        impl: AppSettingsRepositoryImpl
    ): AppSettingsRepository

    @Binds
    internal abstract fun bindCurrentLocationRepository(
        impl: GeolocationUpdatesRepositoryImpl
    ): GeolocationUpdatesRepository

    @Binds
    internal abstract fun bindGeolocationProvider(
        impl: HardwareGeolocationProviderImpl
    ): GeolocationProvider

    @Binds
    internal abstract fun bindLocationSettingsRepository(
        impl: LocationSettingsRepositoryImpl
    ): LocationSettingsRepository

    @Binds
    internal abstract fun bindPermissionChecker(
        impl: PermissionCheckerImpl
    ): PermissionChecker

    @Singleton
    @Binds
    internal abstract fun bindTrackCaptureController(
        impl: TrackCapturerControllerImpl
    ): TrackCapturerController

    @Singleton
    @Binds
    internal abstract fun bindLocationChecker(impl: LocationCheckerImpl): LocationChecker

    @Singleton
    @Binds
    internal abstract fun bindTracksRepository(
        impl: TracksRepositoryImpl
    ): TracksRepository

    @Binds
    abstract fun bindTracksRawLocalDataSource(
        impl: TracksRawLocalDataSourceImpl
    ): TracksRawLocalDataSource

    @Singleton
    @Binds
    internal abstract fun bindTrackCaptureStatusRepository(
        impl: TrackCaptureStatusRepositoryImpl
    ): TrackCaptureStatusRepository

    @Singleton
    @Binds
    internal abstract fun bindTrackCaptureStatusProvider(
        impl: TrackCaptureStatusRepositoryImpl
    ): TrackCaptureStatusProvider

    @Binds
    internal abstract fun bindTrackValidator(impl: TrackValidatorImpl): TrackValidator

    companion object {
        @Provides
        @TracksDirectory
        fun providesTracksDirectory(@ApplicationContext context: Context): File {
            val directory = File(context.filesDir, "tracks")

            if (directory.exists().not()) {
                val isDirectoryCreated = directory.mkdir()
                if (isDirectoryCreated.not()) {
                    error("Can not create directory with name logs")
                }
            }

            return directory
        }
    }
}

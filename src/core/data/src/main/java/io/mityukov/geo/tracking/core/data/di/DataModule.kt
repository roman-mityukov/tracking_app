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
        impl: HardwareGeolocationProvider
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
    internal abstract fun bindTracksRepository(
        impl: TracksRepositoryImpl
    ): TracksRepository

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

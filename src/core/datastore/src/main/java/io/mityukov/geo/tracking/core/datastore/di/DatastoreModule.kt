package io.mityukov.geo.tracking.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.mityukov.geo.tracking.core.datastore.appSettingsDataStore
import io.mityukov.geo.tracking.core.datastore.proto.ProtoLocalAppSettings
import io.mityukov.geo.tracking.core.datastore.proto.ProtoLocalTrackCaptureStatus
import io.mityukov.geo.tracking.core.datastore.trackCaptureStatusDataStore

@Module
@InstallIn(SingletonComponent::class)
interface DatastoreModule {
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
    }
}
